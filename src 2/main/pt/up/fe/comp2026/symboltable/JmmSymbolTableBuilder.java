package pt.up.fe.comp2026.symboltable;

import pt.up.fe.comp.jmm.analysis.table.MethodSymbol;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Visibility;
import pt.up.fe.comp.jmm.analysis.table.reflection.Importer;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmPrimitiveType;
import pt.up.fe.comp.jmm.analysis.table.type.JmmType;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmArrayType;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmClassType;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2026.ast.NodeUtils;
import pt.up.fe.comp2026.ast.TypeUtils;
import pt.up.fe.comp2026.jmm.ast.JmmAttributes;
import pt.up.fe.comp2026.jmm.ast.JmmKind;
import pt.up.fe.specs.util.SpecsCheck;

import java.util.*;

import static pt.up.fe.comp2026.jmm.ast.JmmKind.*;

/**
 * Builds a {@link JmmSymbolTable} from the root AST node of a Java-- program.
 *
 * Traverses the AST to extract:
 *   - The package declaration and class fully qualified name.
 *   - Import declarations (with resolution validation).
 *   - The superclass name (with import validation).
 *   - Class fields and their types.
 *   - Method signatures, parameters, local variables, visibility, and static flags.
 *
 * Errors encountered during construction (duplicate names, unresolved imports, etc.)
 * are collected as {@link Report} objects rather than thrown as exceptions.
 */
public class JmmSymbolTableBuilder {

    private final JmmNode root;
    private final Importer importer;
    public String className;
    private final List<Report> reports;
    private final List<String> imports;
    private final Map<String, String> declaredClasses;

    private JmmSymbolTableBuilder(JmmNode root) {
        this.root = root;
        reports = new ArrayList<>();
        imports = new ArrayList<>();
        declaredClasses = new HashMap<>();
        this.importer = Importer.fromThisClassPath();
    }

    /** Creates a semantic error report anchored to the given AST node. */
    private static Report newError(JmmNode node, String message) {
        return Report.newError(
                Stage.SEMANTIC,
                NodeUtils.getLine(node),
                NodeUtils.getColumn(node),
                message,
                null);
    }

    /**
     * Looks up the FQN of a simple class name by scanning the import list.
     * Returns {@code null} if no matching import is found.
     */
    private String resolveClassName(String simpleName) {
        return imports.stream()
                .filter(imp -> imp.endsWith("." + simpleName))
                .findFirst()
                .orElse(null);
    }

    /**
     * Entry point: builds the symbol table from the given root AST node.
     *
     * @param root the root node of the parsed program.
     * @return a result containing the symbol table and any error reports.
     */
    public static SymbolTableBuilderResult build(JmmNode root) {
        return new JmmSymbolTableBuilder(root).buildInternal();
    }

    /**
     * Performs the full symbol table construction pass over the AST.
     * Resolves the package, class name, imports, superclass, fields, and methods.
     */
    private SymbolTableBuilderResult buildInternal() {

        // Resolve package path and fully qualified class name
        var packageDecl = root.getChildren(JmmKind.PACKAGE_DECL).getFirst();
        var packagePathList = packageDecl.getObjectAsList("path", String.class);
        var packagePath = String.join(".", packagePathList);

        var classDecl = root.getObject("classNode", JmmNode.class);
        SpecsCheck.checkArgument(JmmKind.CLASS_DECL.check(classDecl),
                () -> "Expected a class declaration: " + classDecl);

        this.className = classDecl.get("name");
        var fullyQualifiedName = packagePath + "." + className;

        if (declaredClasses.containsKey(className)) {
            reports.add(newError(root, "'" + className + "' is already defined in this compilation unit"));
        }
        declaredClasses.put(className, fullyQualifiedName);

        // Process import declarations
        for (var importNode : root.getChildren(JmmKind.IMPORT_DECL)) {
            var pathList = importNode.getObjectAsList("path", String.class);
            var qualifiedName = String.join(".", pathList);

            if (!imports.contains(qualifiedName)) {
                imports.add(qualifiedName);
            }

            // Report unresolvable imports only when:
            //   1. The importer has no symbol table for the FQN, AND
            //   2. The class is not an implicit import, AND
            //   3. The class cannot be found on any available classloader
            if (importer.getSymbolTableOf(qualifiedName).isEmpty()
                    && !importer.isImplicitImport(pathList.get(pathList.size() - 1))
                    && !classExistsOnAnyClassLoader(qualifiedName)) {
                reports.add(newError(importNode, "Cannot resolve import '" + qualifiedName + "'"));
            }
        }

        // Resolve superclass name, if present
        String superQualifiedName = null;
        var superNameOpt = classDecl.getOptional("superName");

        if (superNameOpt.isPresent()) {
            var superName = superNameOpt.get();

            if (superName.equals(this.className)) {
                reports.add(newError(classDecl, "Class '" + className + "' cannot extend itself"));
            }

            superQualifiedName = resolveClassName(superName);

            if (superQualifiedName == null) {
                if (importer.isImplicitImport(superName)) {
                    // Resolve FQN from implicit import
                    superQualifiedName = importer.tryImplicitImport(superName)
                            .map(st -> st.getFullyQualifiedName())
                            .orElse(superName);
                } else {
                    reports.add(newError(classDecl, "Class '" + superName + "' is not imported"));
                }
            }
        }

        var fields  = buildFields(classDecl, fullyQualifiedName);
        var methods = buildMethods(classDecl, fullyQualifiedName);

        var symbolTable = new JmmSymbolTable(
                imports,
                fullyQualifiedName,
                superQualifiedName,
                fields,
                methods,
                importer
        );

        return new SymbolTableBuilderResult(symbolTable, reports);
    }

    /**
     * Checks whether a class with the given FQN can be loaded from any available classloader.
     * Tries, in order: thread context, system, this class's loader, and the importer's loader.
     *
     * @param qualifiedName the fully qualified class name to search for.
     * @return {@code true} if the class is found on at least one classloader.
     */
    private boolean classExistsOnAnyClassLoader(String qualifiedName) {
        ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        if (tcl != null && classExistsOn(tcl, qualifiedName)) return true;

        ClassLoader scl = ClassLoader.getSystemClassLoader();
        if (scl != null && classExistsOn(scl, qualifiedName)) return true;

        ClassLoader ccl = JmmSymbolTableBuilder.class.getClassLoader();
        if (ccl != null && classExistsOn(ccl, qualifiedName)) return true;

        try {
            ClassLoader importerCl = importer.getClass().getClassLoader();
            if (importerCl != null && classExistsOn(importerCl, qualifiedName)) return true;
        } catch (Exception ignored) {}

        return false;
    }

    /** Returns true if the given classloader can load the named class without initialising it. */
    private boolean classExistsOn(ClassLoader cl, String qualifiedName) {
        try {
            Class.forName(qualifiedName, false, cl);
            return true;
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            return false;
        }
    }

    /** Collects all method declarations from a class declaration node. */
    private List<MethodSymbol> buildMethods(JmmNode classDecl, String classFullyQualifiedName) {
        var methods = new ArrayList<MethodSymbol>();

        for (var classMember : classDecl.getChildren(JmmKind.CLASS_MEMBER)) {
            var methodDeclOpt = classMember.getChildren(JmmKind.METHOD_DECL);
            if (!methodDeclOpt.isEmpty()) {
                var methodDecl = methodDeclOpt.get(0);
                methods.add(buildMethod(methodDecl, classFullyQualifiedName));
            }
        }

        return methods;
    }

    /**
     * Builds a {@link MethodSymbol} from a method declaration AST node.
     * Extracts the return type, parameters (checking for duplicates), local variables
     * (checking for shadowing and duplicates), visibility, and static flag.
     */
    private MethodSymbol buildMethod(JmmNode method, String classFullyQualifiedName) {
        var methodName = method.get("name");

        var returnTypeNode = method.getObject("returnType", JmmNode.class);
        var returnType = buildType(returnTypeNode, classFullyQualifiedName);

        // Build parameter list, reporting duplicate names
        var paramNodes = method.getChildren(JmmKind.PARAM);
        var params = new ArrayList<Symbol>();
        var paramNames = new HashSet<String>();
        for (var param : paramNodes) {
            var paramTypeNode = param.getObject("typeNode", JmmNode.class);
            var paramType = buildType(paramTypeNode, classFullyQualifiedName);
            var paramName = param.get("name");
            if (!paramNames.add(paramName)) {
                reports.add(newError(param,
                        "Duplicate parameter '" + paramName + "' in method '" + methodName + "'"));
            }
            params.add(new Symbol(paramType, paramName));
        }

        // Build local variable list, reporting shadowing and duplicates
        var varDeclNodes = method.getChildren(JmmKind.VAR_DECL);
        var locals = new ArrayList<Symbol>();
        var localNames = new HashSet<String>();
        for (var varDecl : varDeclNodes) {
            var varTypeNode = varDecl.getObject("typeNode", JmmNode.class);
            var varType = buildType(varTypeNode, classFullyQualifiedName);
            var varName = varDecl.get("name");
            if (paramNames.contains(varName)) {
                reports.add(newError(varDecl,
                        "Local variable '" + varName + "' has same name as a parameter in method '" + methodName + "'"));
            } else if (!localNames.add(varName)) {
                reports.add(newError(varDecl,
                        "Duplicate local variable '" + varName + "' in method '" + methodName + "'"));
            }
            locals.add(new Symbol(varType, varName));
        }

        // Resolve visibility modifier; default to package-protected
        var visibilityOpt = method.getOptional("visibility");
        var visibility = visibilityOpt.map(s -> switch (s.toLowerCase()) {
            case "private"   -> Visibility.PRIVATE;
            case "protected" -> Visibility.PROTECTED;
            case "public"    -> Visibility.PUBLIC;
            default          -> Visibility.PACKAGE_PROTECTED;
        }).orElse(Visibility.PACKAGE_PROTECTED);

        var isStatic = method.getBoolean(JmmAttributes.METHOD_DECL.IS_STATIC, false);

        return new MethodSymbol(methodName, returnType, params, locals, isStatic, visibility);
    }

    /** Collects all field declarations from a class declaration node. */
    private List<Symbol> buildFields(JmmNode classDecl, String classFullyQualifiedName) {
        var fields = new ArrayList<Symbol>();

        for (var classMember : classDecl.getChildren(JmmKind.CLASS_MEMBER)) {
            var fieldDeclOpt = classMember.getChildren(JmmKind.FIELD_DECL);
            if (!fieldDeclOpt.isEmpty()) {
                var fieldDecl = fieldDeclOpt.get(0);
                var typeNode = fieldDecl.getObject("typeNode", JmmNode.class);
                var type = buildType(typeNode, classFullyQualifiedName);
                var name = fieldDecl.get("name");
                fields.add(new Symbol(type, name));
            }
        }

        return fields;
    }

    /**
     * Converts a type AST node into a {@link JmmType}.
     * Handles primitives, array dimensions, and class types (resolving FQNs
     * from the import list or using the current class name directly).
     */
    private JmmType buildType(JmmNode typeNode, String classFullyQualifiedName) {
        var name = typeNode.get("name");
        var dimsList = typeNode.getObjectAsList("dims", String.class);
        int dimensions = dimsList.size() / 2;

        JmmType baseType;

        var primitive = JmmPrimitiveType.fromString(name);
        if (primitive.isPresent()) {
            baseType = primitive.get();
        } else {
            boolean isImported = imports.stream().anyMatch(imp -> imp.endsWith("." + name))
                    || importer.isImplicitImport(name);

            // Prefer the current class's FQN; otherwise look up via imports
            String fqn;
            if (name.equals(this.className)) {
                fqn = classFullyQualifiedName;
            } else {
                fqn = imports.stream()
                        .filter(imp -> imp.endsWith("." + name))
                        .findFirst()
                        .orElse(name);
            }

            baseType = JmmClassType.ofInstance(fqn, isImported);
        }

        return dimensions > 0 ? new JmmArrayType(baseType, dimensions) : baseType;
    }
}
