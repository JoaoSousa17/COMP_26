package pt.up.fe.comp2026.analysis.calls;

import pt.up.fe.comp.jmm.analysis.table.MethodSymbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2026.analysis.AnalysisVisitorWithTable;
import pt.up.fe.comp2026.jmm.ast.JmmKind;

import java.util.List;

/**
 * Analysis pass for section 3.1.4 - Calls (Instantiation sub-item).
 *
 * Validates {@code new X()} expressions:
 * 1. The class X must be imported (or be the current class).
 * 2. If a matching constructor (by arg count) exists, accept it.
 *    We are deliberately lenient on argument types for imported classes
 *    because our type system cannot perfectly model all Java type conversions
 *    (e.g. String vs char[], autoboxing, etc.).
 */
public class InstantiationValidationPass extends AnalysisVisitorWithTable {

    private MethodSymbol currentMethod;

    public InstantiationValidationPass(SymbolTable table) {
        super(table);
        buildVisitor();
    }

    @Override
    protected void buildVisitor() {
        addVisit(JmmKind.METHOD_DECL, this::visitMethodDecl);
        addVisit(JmmKind.NEW_EXPR,    this::visitNewExpr);
    }

    private Void visitMethodDecl(JmmNode method, SymbolTable table) {
        var sig = types.getMethodDeclSignature(method);
        currentMethod = this.table.getMethod(sig).orElse(null);
        return null;
    }

    private Void visitNewExpr(JmmNode newExpr, SymbolTable table) {
        String className = newExpr.get("name");

        // --- 1. Class must be imported or be the current class ---
        boolean isCurrentClass = className.equals(this.table.getClassName())
                || className.equals(this.table.getFullyQualifiedName());

        boolean isImported = this.table.getImportNames().contains(className)
                || this.table.isImplicitImport(className);

        if (!isCurrentClass && !isImported) {
            addReport(newError(newExpr,
                    "Cannot instantiate class '" + className + "': class is not imported"));
            return null;
        }

        List<JmmNode> argNodes = newExpr.getChildren();

        // --- 2. For the current class, check via symbol table ---
        if (isCurrentClass) {
            String fqn = this.table.getImportedFullyQualifiedName(className).orElse(className);
            var stOpt = this.table.getImportedSymbolTable(fqn);
            if (stOpt.isEmpty()) stOpt = this.table.getImportedSymbolTable(className);
            if (stOpt.isEmpty()) stOpt = this.table.getImplicitImport(className);

            if (stOpt.isPresent()) {
                var st = stOpt.get();
                var allConstructors = st.getMethods("<init>");

                if (!allConstructors.isEmpty()) {
                    var matching = allConstructors.stream()
                            .filter(m -> m.parameters().size() == argNodes.size())
                            .toList();

                    if (matching.isEmpty()) {
                        int expected = allConstructors.get(0).parameters().size();
                        addReport(newError(newExpr,
                                "No constructor found in '" + className + "' that accepts "
                                        + argNodes.size() + " argument(s), expected " + expected));
                        return null;
                    }

                    // Check arg types against symbol table
                    var constructor = matching.get(0);
                    var params = constructor.parameters();
                    for (int i = 0; i < params.size(); i++) {
                        var expected = params.get(i).type();
                        var actual = types.getExprType(argNodes.get(i), currentMethod);
                        if (actual == null) continue;
                        if (!types.isTypeCompatible(expected, actual)) {
                            addReport(newError(argNodes.get(i),
                                    "Constructor argument " + (i + 1) + " of '" + className
                                            + "': expected '" + expected + "', got '" + actual + "'"));
                        }
                    }
                }
            }
            return null;
        }

        // --- 3. For imported classes, use reflection but only check arg COUNT ---
        // We skip per-argument type checking for imported/implicit classes because
        // our type system cannot model all Java type relationships (e.g. String vs char[],
        // autoboxing, widening conversions, etc.). If a constructor with the right
        // number of arguments exists, we accept the call conservatively.
        String fqn = this.table.getImportedFullyQualifiedName(className).orElse(className);
        try {
            Class<?> clazz = Class.forName(fqn);
            var ctors = java.util.Arrays.stream(clazz.getConstructors())
                    .filter(c -> c.getParameterCount() == argNodes.size())
                    .toList();

            if (ctors.isEmpty() && !argNodes.isEmpty()) {
                // Check if there are any constructors at all
                var allCtors = clazz.getConstructors();
                if (allCtors.length > 0) {
                    // There are constructors, but none match the arg count
                    // Only report an error if ALL constructors have a different count
                    // and no varargs constructor could match
                    boolean anyVarargs = java.util.Arrays.stream(allCtors)
                            .anyMatch(c -> c.isVarArgs());
                    if (!anyVarargs) {
                        int expected = allCtors[0].getParameterCount();
                        addReport(newError(newExpr,
                                "Constructor of '" + className + "' expects " + expected
                                        + " argument(s) but got " + argNodes.size()));
                    }
                }
            }
            // If ctors is non-empty (arg count matches at least one overload), accept without
            // further type checking — our type system is too coarse for exact Java type matching.
        } catch (ClassNotFoundException e) {
            // Class not on classpath — try symbol table as fallback
            var stOpt = this.table.getImportedSymbolTable(fqn);
            if (stOpt.isEmpty()) stOpt = this.table.getImplicitImport(className);
            if (stOpt.isEmpty()) return null; // Cannot resolve — skip conservatively

            var st = stOpt.get();
            var allConstructors = st.getMethods("<init>");
            if (!allConstructors.isEmpty()) {
                var matching = allConstructors.stream()
                        .filter(m -> m.parameters().size() == argNodes.size())
                        .toList();
                if (matching.isEmpty()) {
                    int expected = allConstructors.get(0).parameters().size();
                    addReport(newError(newExpr,
                            "No constructor found in '" + className + "' that accepts "
                                    + argNodes.size() + " argument(s), expected " + expected));
                }
            }
        }

        return null;
    }
}