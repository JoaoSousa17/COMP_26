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
 * 2. If a matching constructor (no-arg by default in Java--) exists in the ST, argument
 *    types are checked; if the ST is unavailable the check is skipped conservatively.
 *
 * Note: Java-- core only requires {@code new X()} (no-arg). The "New With Arguments"
 * extension (new X(a, b)) is also handled here transparently.
 */
public class InstantiationValidationPass extends AnalysisVisitorWithTable {

    /** The method currently being visited, used for type resolution of arguments. */
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

    /** Tracks the current method being analysed for scoped type resolution. */
    private Void visitMethodDecl(JmmNode method, SymbolTable table) {
        var sig = types.getMethodDeclSignature(method);
        currentMethod = this.table.getMethod(sig).orElse(null);
        return null;
    }

    /**
     * Validates a {@code new X(args...)} expression.
     *
     * Checks that X is a known class (current or imported), then verifies
     * argument types against the matching constructor in the symbol table.
     * If the symbol table for X is unavailable, argument type checking is skipped.
     */
    private Void visitNewExpr(JmmNode newExpr, SymbolTable table) {
        String className = newExpr.get("name");

        // 1. Class must be the current class or explicitly imported
        boolean isCurrentClass = className.equals(this.table.getClassName())
                || className.equals(this.table.getFullyQualifiedName());

        boolean isImported = this.table.getImportNames().contains(className)
                || this.table.isImplicitImport(className);

        if (!isCurrentClass && !isImported) {
            addReport(newError(newExpr,
                    "Cannot instantiate class '" + className + "': class is not imported"));
            return null;
        }

        // 2. Argument type checking (best-effort)
        List<JmmNode> argNodes = newExpr.getChildren();

        if (argNodes.isEmpty()) {
            // No-arg constructor — always structurally valid
            return null;
        }

        // Resolve the symbol table for the instantiated class
        var stOpt = this.table.getImportedSymbolTable(className);
        if (stOpt.isEmpty()) stOpt = this.table.getImplicitImport(className);

        if (stOpt.isEmpty()) {
            // Cannot resolve ST for the class — skip argument type checks conservatively
            return null;
        }

        // Find a constructor (represented as "<init>") with matching arity
        var st = stOpt.get();
        var constructors = st.getMethods("<init>").stream()
                .filter(m -> m.parameters().size() == argNodes.size())
                .toList();

        if (constructors.isEmpty()) {
            addReport(newError(newExpr,
                    "No constructor found in '" + className
                            + "' that accepts " + argNodes.size() + " argument(s)"));
            return null;
        }

        // Verify each argument type against the first matching constructor
        var constructor = constructors.get(0);
        var params = constructor.parameters();

        for (int i = 0; i < params.size(); i++) {
            var expected = params.get(i).type();
            var actual   = types.getExprType(argNodes.get(i), currentMethod);

            if (actual == null) continue; // unresolved — skip conservatively

            if (!types.isTypeCompatible(expected, actual)) {
                addReport(newError(argNodes.get(i),
                        "Constructor argument " + (i + 1) + " of '" + className
                                + "': expected '" + expected + "', got '" + actual + "'"));
            }
        }

        return null;
    }
}