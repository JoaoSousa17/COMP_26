package pt.up.fe.comp2026.symboltable;

import pt.up.fe.comp.jmm.analysis.table.MethodSymbol;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Visibility;
import pt.up.fe.comp.jmm.analysis.table.reflection.Importer;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmPrimitiveType;
import pt.up.fe.comp.jmm.analysis.table.type.JmmType;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmArrayType;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmClassType;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp.jmm.utils.Attributes;
import pt.up.fe.comp2026.ast.NodeUtils;
import pt.up.fe.comp2026.ast.TypeUtils;
import pt.up.fe.comp2026.jmm.ast.JmmAttributes;
import pt.up.fe.comp2026.jmm.ast.JmmKind;
import pt.up.fe.specs.util.SpecsCheck;

import java.util.*;

import static pt.up.fe.comp2026.jmm.ast.JmmKind.*;

public class JmmSymbolTableBuilder {

    private final JmmNode root;
    private final Importer importer;
    public String className;
    private final List<Report> reports;
    private final List<String> imports;
    private final Map<String, String> declaredClasses;

    /**
     * Only build() can create new instances, this ensures that each instance is used only once,
     * and we do not have to worry about "cleaning state".
     */
    private JmmSymbolTableBuilder(JmmNode root) {
        this.root = root;
        reports = new ArrayList<>();
        imports = new ArrayList<>();
        declaredClasses = new HashMap<>();
        this.importer = Importer.fromThisClassPath();
    }

    private static Report newError(JmmNode node, String message) {
        return Report.newError(
                Stage.SEMANTIC,
                NodeUtils.getLine(node),
                NodeUtils.getColumn(node),
                message,
                null);
    }

    private String resolveClassName(String simpleName) {
        return imports.stream()
                .filter(imp -> imp.endsWith("." + simpleName))
                .findFirst()
                .orElse(null);
    }

    public static SymbolTableBuilderResult build(JmmNode root) {
        return new JmmSymbolTableBuilder(root).buildInternal();
    }

    private SymbolTableBuilderResult buildInternal() {

        // Extract Package
        var packageDecl = root.getChildren(JmmKind.PACKAGE_DECL).getFirst();
        var packagePathList = packageDecl.getObjectAsList("path", String.class);
        var packagePath = String.join(".", packagePathList);

        var classDecl = root.getObject("classNode", JmmNode.class);
        SpecsCheck.checkArgument(JmmKind.CLASS_DECL.check(classDecl), () -> "Expected a class declaration: " + classDecl);

        this.className = classDecl.get("name");
        var fullyQualifiedName = packagePath + "." + className;

        if (declaredClasses.containsKey(className)) {
            reports.add(newError(root, "'" + className + "' is already defined in this compilation unit"));
        }
        declaredClasses.put(className, fullyQualifiedName);

        // Extract Imports
        for (var importNode : root.getChildren(JmmKind.IMPORT_DECL)) {
            var pathList = importNode.getObjectAsList("path", String.class);
            var qualifiedName = String.join(".", pathList);

            if (!imports.contains(qualifiedName)) { // ignore duplicates
                imports.add(qualifiedName);
            }

            // Validate that the imported class actually exists on the classpath
            if (importer.getSymbolTableOf(qualifiedName).isEmpty()) {
                reports.add(newError(importNode, "Cannot resolve import '" + qualifiedName + "'"));
            }
        }

        // Extract Super Class
        // Grammar: classDecl : CLASS name=ID (EXTENDS superName=ID)? '{' ... '}'
        String superQualifiedName = null;
        var superNameOpt = classDecl.getOptional("superName");

        if (superNameOpt.isPresent()) {
            var superName = superNameOpt.get();

            // verification 1: a class cannot extend itself
            if (superName.equals(this.className)) {
                reports.add(newError(classDecl, "Class '" + className + "' cannot extend itself"));
            }

            // verification 2: solve simple name -> FQN via explicit imports
            superQualifiedName = resolveClassName(superName);

            // verification 3: if not found in explicit imports, check implicit (java.lang.*)
            if (superQualifiedName == null) {
                if (importer.isImplicitImport(superName)) {
                    superQualifiedName = importer.tryImplicitImport(superName)
                            .map(SymbolTable::getFullyQualifiedName)
                            .orElse(superName);
                } else {
                    reports.add(newError(classDecl, "Class '" + superName + "' is not imported"));
                }
            }
        }

        // Extract Fields
        // Grammar: fieldDecl : typeNode=type name=ID ('=' expr)? ';'
        var fields = buildFields(classDecl, fullyQualifiedName);

        // Extract Methods
        var methods = buildMethods(classDecl, fullyQualifiedName);

        // Build Symbol Table
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

    private List<MethodSymbol> buildMethods(JmmNode classDecl, String classFullyQualifiedName) {
        // Grammar: classMember : fieldDecl | methodDecl
        // So we need to iterate through CLASS_MEMBER nodes and find those containing METHOD_DECL
        var methods = new ArrayList<MethodSymbol>();

        for (var classMember : classDecl.getChildren(JmmKind.CLASS_MEMBER)) {
            // Each CLASS_MEMBER contains either a FIELD_DECL or METHOD_DECL
            var methodDeclOpt = classMember.getChildren(JmmKind.METHOD_DECL);

            if (!methodDeclOpt.isEmpty()) {
                // This CLASS_MEMBER is a method declaration
                var methodDecl = methodDeclOpt.get(0);
                methods.add(buildMethod(methodDecl, classFullyQualifiedName));
            }
        }

        return methods;
    }

    private MethodSymbol buildMethod(JmmNode method, String classFullyQualifiedName) {
        var methodName = method.get("name");

        // 1. Return Type
        // Grammar: methodDecl : ... returnType=type name=ID ...
        var returnTypeNode = method.getObject("returnType", JmmNode.class);
        var returnType = buildType(returnTypeNode, classFullyQualifiedName);

        // 2. Parameters — check for duplicates
        var paramNodes = method.getChildren(JmmKind.PARAM);
        var params = new ArrayList<Symbol>();
        var paramNames = new HashSet<String>();
        for (var param : paramNodes) {
            var paramTypeNode = param.getObject("typeNode", JmmNode.class);
            var paramType = buildType(paramTypeNode, classFullyQualifiedName);
            var paramName = param.get("name");
            if (!paramNames.add(paramName)) {
                reports.add(newError(param, "Duplicate parameter '" + paramName + "' in method '" + methodName + "'"));
            }
            params.add(new Symbol(paramType, paramName));
        }

        // 3. Local Variables — check for duplicates and collision with params
        // Grammar: varDecl : typeNode=type name=ID ';'
        var varDeclNodes = method.getChildren(JmmKind.VAR_DECL);
        var locals = new ArrayList<Symbol>();
        var localNames = new HashSet<String>();
        for (var varDecl : varDeclNodes) {
            var varTypeNode = varDecl.getObject("typeNode", JmmNode.class);
            var varType = buildType(varTypeNode, classFullyQualifiedName);
            var varName = varDecl.get("name");
            if (paramNames.contains(varName)) {
                reports.add(newError(varDecl, "Local variable '" + varName + "' has same name as a parameter in method '" + methodName + "'"));
            } else if (!localNames.add(varName)) {
                reports.add(newError(varDecl, "Duplicate local variable '" + varName + "' in method '" + methodName + "'"));
            }
            locals.add(new Symbol(varType, varName));
        }

        // 4. Visibility
        // Grammar: visibility=(PUBLIC | PRIVATE | PROTECTED)?
        var visibilityOpt = method.getOptional("visibility");
        var visibility = visibilityOpt.map(s -> switch (s.toLowerCase()) {
            case "private" -> Visibility.PRIVATE;
            case "protected" -> Visibility.PROTECTED;
            case "public" -> Visibility.PUBLIC;
            default -> Visibility.PACKAGE_PROTECTED;
        }).orElse(Visibility.PACKAGE_PROTECTED);

        // 5. Static flag
        // Grammar: (STATIC {$isStatic=true;})?
        var isStatic = method.getBoolean(JmmAttributes.METHOD_DECL.IS_STATIC, false);

        // 6. Create MethodSymbol
        return new MethodSymbol(methodName, returnType, params, locals, isStatic, visibility);
    }

    private List<Symbol> buildFields(JmmNode classDecl, String classFullyQualifiedName) {
        // Grammar: classMember : fieldDecl | methodDecl
        // So we need to iterate through CLASS_MEMBER nodes and find those containing FIELD_DECL
        var fields = new ArrayList<Symbol>();

        for (var classMember : classDecl.getChildren(JmmKind.CLASS_MEMBER)) {
            // Each CLASS_MEMBER contains either a FIELD_DECL or METHOD_DECL
            var fieldDeclOpt = classMember.getChildren(JmmKind.FIELD_DECL);

            if (!fieldDeclOpt.isEmpty()) {
                // This CLASS_MEMBER is a field declaration
                var fieldDecl = fieldDeclOpt.getFirst();
                var typeNode = fieldDecl.getObject("typeNode", JmmNode.class);
                var type = buildType(typeNode, classFullyQualifiedName);
                var name = fieldDecl.get("name");
                fields.add(new Symbol(type, name));
            }
        }

        return fields;
    }

    private JmmType buildType(JmmNode typeNode, String classFullyQualifiedName) {
        var name = typeNode.get("name");

        // calc n of dimensions
        var dimsList = typeNode.getObjectAsList("dims", String.class);
        int dimensions = dimsList.size() / 2;

        JmmType baseType;

        // try as primitive type
        var primitive = JmmPrimitiveType.fromString(name);
        if (primitive.isPresent()) {
            baseType = primitive.get();
        } else { // is a type class - solve FQN from collected imports
            boolean isImported = imports.stream()
                    .anyMatch(imp -> imp.endsWith("." + name))
                    || importer.isImplicitImport(name);

            // solve FQN
            String fqn;
            if (name.equals(this.className)) {
                // It's a reference to the current class being defined
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
