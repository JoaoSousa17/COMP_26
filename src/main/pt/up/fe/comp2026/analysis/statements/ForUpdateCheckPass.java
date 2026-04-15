package pt.up.fe.comp2026.analysis.statements;

import pt.up.fe.comp.jmm.analysis.table.MethodSymbol;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.type.JmmType;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2026.analysis.AnalysisVisitorWithTable;
import pt.up.fe.comp2026.jmm.ast.JmmKind;

/**
 * Analysis pass that validates the init and update clauses of for-loop statements.
 *
 * Rules enforced:
 *   - A for-loop with an init clause must also have an update clause.
 *   - The type of the expression in the update clause must be compatible with
 *     the declared type of the variable being assigned.
 *   - The type of the expression in the init clause must be compatible with
 *     the declared type of the variable being assigned.
 */
public class ForUpdateCheckPass extends AnalysisVisitorWithTable {

    /** The method currently being visited, used for variable type resolution. */
    private MethodSymbol currentMethod;

    public ForUpdateCheckPass(SymbolTable table) {
        super(table);
        buildVisitor();
    }

    @Override
    protected void buildVisitor() {
        addVisit(JmmKind.METHOD_DECL, this::visitMethodDecl);
        addVisit(JmmKind.FOR_STMT,    this::visitForStmt);
    }

    /** Tracks the current method being analysed for scoped type resolution. */
    private Void visitMethodDecl(JmmNode method, SymbolTable table) {
        var sig = types.getMethodDeclSignature(method);
        currentMethod = this.table.getMethod(sig).orElse(null);
        return null;
    }

    /**
     * Validates the init and update clauses of a for-loop.
     * Reports a syntactic error if an init is present without an update,
     * then type-checks both clauses independently.
     */
    private Void visitForStmt(JmmNode forStmt, SymbolTable table) {
        boolean hasForInit = forStmt.getChildren().stream()
                .anyMatch(c -> JmmKind.FOR_INIT.check(c));
        boolean hasForUpdate = forStmt.getChildren().stream()
                .anyMatch(c -> JmmKind.FOR_UPDATE.check(c));

        // A for-loop with an init clause must also declare an update clause
        if (hasForInit && !hasForUpdate) {
            addReport(Report.newError(
                    Stage.SYNTATIC,
                    forStmt.getLine(),
                    forStmt.getColumn(),
                    "For loop with initialization must have an update assignment",
                    null));
            return null;
        }

        // Type-check the update assignment
        if (hasForUpdate) {
            var forUpdateOpt = forStmt.getChildren().stream()
                    .filter(c -> JmmKind.FOR_UPDATE.check(c))
                    .findFirst();

            forUpdateOpt.ifPresent(forUpdate -> {
                if (forUpdate.getNumChildren() > 0) {
                    JmmNode assignment = forUpdate.getChild(0);
                    if (JmmKind.ASSIGNMENT.check(assignment)) {
                        String varName = assignment.get("name");
                        JmmNode rhsExpr = assignment.getChild(0);

                        JmmType lhsType = resolveVarType(varName);
                        JmmType rhsType = types.getExprType(rhsExpr, currentMethod);

                        if (lhsType != null && rhsType != null
                                && !types.isTypeCompatible(lhsType, rhsType)) {
                            addReport(newError(assignment,
                                    "Type mismatch in for update: cannot assign '"
                                            + rhsType + "' to '" + lhsType + "'"));
                        }
                    }
                }
            });
        }

        // Type-check the init assignment
        if (hasForInit) {
            var forInitOpt = forStmt.getChildren().stream()
                    .filter(c -> JmmKind.FOR_INIT.check(c))
                    .findFirst();

            forInitOpt.ifPresent(forInit -> {
                if (forInit.getNumChildren() > 0) {
                    JmmNode assignment = forInit.getChild(0);
                    if (JmmKind.ASSIGNMENT.check(assignment)) {
                        String varName = assignment.get("name");
                        JmmNode rhsExpr = assignment.getChild(0);

                        JmmType lhsType = resolveVarType(varName);
                        JmmType rhsType = types.getExprType(rhsExpr, currentMethod);

                        if (lhsType != null && rhsType != null
                                && !types.isTypeCompatible(lhsType, rhsType)) {
                            addReport(newError(assignment,
                                    "Type mismatch in for init: cannot assign '"
                                            + rhsType + "' to '" + lhsType + "'"));
                        }
                    }
                }
            });
        }

        return null;
    }

    /**
     * Resolves the type of a variable by searching, in order:
     * the current method's parameters, the current method's locals, and class fields.
     */
    private JmmType resolveVarType(String name) {
        if (currentMethod != null) {
            var p = currentMethod.getParameter(name);
            if (p.isPresent()) return p.get().type();
            var l = currentMethod.getLocalVariable(name);
            if (l.isPresent()) return l.get().type();
        }
        var f = this.table.getField(name);
        return f.map(Symbol::type).orElse(null);
    }
}
