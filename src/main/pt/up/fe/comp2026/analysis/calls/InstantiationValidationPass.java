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

    // ------------------------------------------------------------------ //
    //  Scope tracking
    // ------------------------------------------------------------------ //

    private Void visitMethodDecl(JmmNode method, SymbolTable table) {
        var sig = types.getMethodDeclSignature(method);
        currentMethod = this.table.getMethod(sig).orElse(null);
        return null;
    }

    // ------------------------------------------------------------------ //
    //  new X(args...)
    // ------------------------------------------------------------------ //

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

        // --- 2. Argument type checking via symbol table ---
        List<JmmNode> argNodes = newExpr.getChildren();

        // Resolve simple name to FQN if needed, then look up the symbol table
        String fqn = this.table.getImportedFullyQualifiedName(className).orElse(className);
        var stOpt = this.table.getImportedSymbolTable(fqn);
        if (stOpt.isEmpty()) stOpt = this.table.getImportedSymbolTable(className);
        if (stOpt.isEmpty()) stOpt = this.table.getImplicitImport(className);

        if (stOpt.isEmpty()) {
            // Cannot resolve ST — skip argument type checks conservatively
            return null;
        }

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

            var constructor = matching.get(0);
            var params = constructor.parameters();
            for (int i = 0; i < params.size(); i++) {
                var expected = params.get(i).type();
                var actual   = types.getExprType(argNodes.get(i), currentMethod);
                if (actual == null) continue;
                if (!types.isTypeCompatible(expected, actual)) {
                    addReport(newError(argNodes.get(i),
                            "Constructor argument " + (i + 1) + " of '" + className
                                    + "': expected '" + expected + "', got '" + actual + "'"));
                }
            }
        }

        return null;
    }
}