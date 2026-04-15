package pt.up.fe.comp2026.analysis.statements;

import pt.up.fe.comp.jmm.analysis.table.MethodSymbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.analysis.table.type.JmmType;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmPrimitiveType;
import pt.up.fe.comp2026.analysis.AnalysisVisitorWithTable;
import pt.up.fe.comp2026.jmm.ast.JmmKind;

/**
 * Analysis pass that checks that the conditions of if, while, do-while,
 * and for statements are of type boolean.
 */
public class ConditionAnalysisPass extends AnalysisVisitorWithTable {

    /** The method currently being visited, used for condition type resolution. */
    private MethodSymbol currentMethod;

    public ConditionAnalysisPass(SymbolTable table) {
        super(table);
        buildVisitor();
    }

    @Override
    protected void buildVisitor() {
        addVisit(JmmKind.METHOD_DECL,    this::visitMethodDecl);
        addVisit(JmmKind.IF_STMT,        this::visitIfStmt);
        addVisit(JmmKind.IF_ELSE_STMT,   this::visitIfElseStmt);
        addVisit(JmmKind.WHILE_STMT,     this::visitWhileStmt);
        addVisit(JmmKind.DO_WHILE_STMT,  this::visitDoWhileStmt);
        addVisit(JmmKind.FOR_STMT,       this::visitForStmt);
    }

    /** Tracks the current method being analysed for scoped type resolution. */
    private Void visitMethodDecl(JmmNode method, SymbolTable table) {
        var signature = types.getMethodDeclSignature(method);
        currentMethod = this.table.getMethod(signature).orElse(null);
        return null;
    }

    /** Checks that the condition of an if statement is boolean (child 0 is the condition). */
    private Void visitIfStmt(JmmNode ifStmt, SymbolTable table) {
        checkCondition(ifStmt, "if");
        return null;
    }

    /** Checks that the condition of an if-else statement is boolean (child 0 is the condition). */
    private Void visitIfElseStmt(JmmNode ifElseStmt, SymbolTable table) {
        checkCondition(ifElseStmt, "if");
        return null;
    }

    /** Checks that the condition of a while statement is boolean (child 0 is the condition). */
    private Void visitWhileStmt(JmmNode whileStmt, SymbolTable table) {
        checkCondition(whileStmt, "while");
        return null;
    }

    /**
     * Checks that the condition of a do-while statement is boolean.
     * Grammar: {@code DO stmt WHILE '(' expr ')' ';'} — the condition is the last child.
     */
    private Void visitDoWhileStmt(JmmNode doWhileStmt, SymbolTable table) {
        if (doWhileStmt.getNumChildren() < 2) return null;
        JmmNode condition = doWhileStmt.getChild(doWhileStmt.getNumChildren() - 1);
        checkConditionNode(condition, "do-while");
        return null;
    }

    /**
     * Checks that the condition of a for statement is boolean.
     * Grammar: {@code FOR '(' forInit? ';' forCond? ';' forUpdate? ')' stmt} —
     * the condition expression is wrapped inside a ForCond child node.
     */
    private Void visitForStmt(JmmNode forStmt, SymbolTable table) {
        forStmt.getChildren().stream()
                .filter(c -> JmmKind.FOR_COND.check(c))
                .findFirst()
                .ifPresent(forCond -> {
                    if (forCond.getNumChildren() > 0) {
                        checkConditionNode(forCond.getChild(0), "for");
                    }
                });
        return null;
    }

    /** Extracts and checks the condition from child 0 of a control-flow statement node. */
    private void checkCondition(JmmNode stmt, String stmtType) {
        if (stmt.getNumChildren() == 0) return;
        checkConditionNode(stmt.getChild(0), stmtType);
    }

    /**
     * Verifies that a condition expression resolves to boolean.
     * Skips the check if the type cannot be resolved.
     */
    private void checkConditionNode(JmmNode condition, String stmtType) {
        JmmType conditionType = types.getExprType(condition, currentMethod);
        if (conditionType == null) return; // unresolved type — skip conservatively

        if (!conditionType.equals(JmmPrimitiveType.BOOLEAN)) {
            addReport(newError(condition,
                    "The " + stmtType + " condition must be of boolean type, but got '" + conditionType + "'"));
        }
    }
}
