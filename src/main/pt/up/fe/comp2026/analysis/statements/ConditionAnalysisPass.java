package pt.up.fe.comp2026.analysis.statements;

import pt.up.fe.comp.jmm.analysis.table.MethodSymbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.analysis.table.type.JmmType;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmPrimitiveType;
import pt.up.fe.comp2026.analysis.AnalysisVisitorWithTable;
import pt.up.fe.comp2026.jmm.ast.JmmKind;

/**
 * Analysis pass to check that if, while, for and do-while conditions are of boolean type.
 */
public class ConditionAnalysisPass extends AnalysisVisitorWithTable {

    private MethodSymbol currentMethod;

    public ConditionAnalysisPass(SymbolTable table) {
        super(table);
        buildVisitor();
    }

    @Override
    protected void buildVisitor() {
        addVisit(JmmKind.METHOD_DECL, this::visitMethodDecl);
        addVisit(JmmKind.IF_STMT, this::visitIfStmt);
        addVisit(JmmKind.IF_ELSE_STMT, this::visitIfElseStmt);
        addVisit(JmmKind.WHILE_STMT, this::visitWhileStmt);
        addVisit(JmmKind.DO_WHILE_STMT, this::visitDoWhileStmt);
        addVisit(JmmKind.FOR_STMT, this::visitForStmt);
    }

    private Void visitMethodDecl(JmmNode method, SymbolTable table) {
        var signature = types.getMethodDeclSignature(method);
        currentMethod = this.table.getMethod(signature).orElse(null);
        return null;
    }

    private Void visitIfStmt(JmmNode ifStmt, SymbolTable table) {
        checkConditionAt(ifStmt, "if", 0);
        return null;
    }

    private Void visitIfElseStmt(JmmNode ifElseStmt, SymbolTable table) {
        checkConditionAt(ifElseStmt, "if", 0);
        return null;
    }

    private Void visitWhileStmt(JmmNode whileStmt, SymbolTable table) {
        checkConditionAt(whileStmt, "while", 0);
        return null;
    }

    private Void visitDoWhileStmt(JmmNode doWhileStmt, SymbolTable table) {
        checkConditionAt(doWhileStmt, "do-while", 1);
        return null;
    }

    private Void visitForStmt(JmmNode forStmt, SymbolTable table) {
        // FOR_COND is an optional child — find it by kind, not by index
        forStmt.getChildren().stream()
                .filter(c -> JmmKind.FOR_COND.check(c))
                .findFirst()
                .ifPresent(forCond -> {
                    if (forCond.getNumChildren() > 0) {
                        JmmNode condition = forCond.getChild(0);
                        JmmType conditionType = types.getExprType(condition, currentMethod);
                        if (conditionType != null && !conditionType.equals(JmmPrimitiveType.BOOLEAN)) {
                            addReport(newError(condition,
                                    "The for condition must be of boolean type, but got '" + conditionType + "'"));
                        }
                    }
                });
        return null;
    }

    private void checkConditionAt(JmmNode stmt, String stmtType, int conditionIndex) {
        if (stmt.getNumChildren() <= conditionIndex) {
            return; // no condition node
        }

        JmmNode condition = stmt.getChild(conditionIndex);
        JmmType conditionType = types.getExprType(condition, currentMethod);

        if (conditionType == null) {
            return; // unknown type - skip check
        }

        if (!conditionType.equals(JmmPrimitiveType.BOOLEAN)) {
            addReport(newError(condition, "The " + stmtType + " condition must be of boolean type, but got '" + conditionType + "'"));
        }
    }
}