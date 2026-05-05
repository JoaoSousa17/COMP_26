package pt.up.fe.comp2026.analysis.entityops;

import pt.up.fe.comp.jmm.analysis.table.MethodSymbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.type.JmmType;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmPrimitiveType;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2026.analysis.AnalysisVisitorWithTable;
import pt.up.fe.comp2026.jmm.ast.JmmKind;

public class OperandTypeCheckPass extends AnalysisVisitorWithTable {
    private MethodSymbol currentMethod;

    public OperandTypeCheckPass(SymbolTable table) {
        super(table);
        buildVisitor();
    }

    @Override
    protected void buildVisitor() {
        addVisit(JmmKind.METHOD_DECL, this::visitMethodDecl);
        addVisit(JmmKind.BINARY_EXPR, this::visitBinaryExpr);
        addVisit(JmmKind.UNARY_EXPR,  this::visitUnaryExpr);
        addVisit(JmmKind.PLUS_EXPR, this::visitUnaryArithExpr);
        addVisit(JmmKind.MINUS_EXPR, this::visitUnaryArithExpr);
        addVisit(JmmKind.PLUS_PLUS_EXPR, this::visitUnaryArithExpr);
        addVisit(JmmKind.MINUS_MINUS_EXPR, this::visitUnaryArithExpr);
    }

    private Void visitMethodDecl(JmmNode method, SymbolTable table) {
        currentMethod =
                this.table.getMethod(types.getMethodDeclSignature(method)).orElse(null);
        return null;
    }

    private Void visitBinaryExpr(JmmNode expr, SymbolTable table) {
        String op = expr.get("op");
        JmmType left  = types.getExprType(expr.getChild(0), currentMethod);
        JmmType right = types.getExprType(expr.getChild(1), currentMethod);
        if (left == null || right == null) return null;

        switch (op) {
            case "+", "-", "*", "/", "%", "<", ">", "<=", ">=" -> {
                if (!left.equals(JmmPrimitiveType.INT))
                    addReport(newError(expr.getChild(0), "Operand of '" + op + "' must be int, got '" + left + "'"));
                if (!right.equals(JmmPrimitiveType.INT))
                    addReport(newError(expr.getChild(1), "Operand of '" + op + "' must be int, got '" + right + "'"));
            }
            case "&&", "||" -> {
                if (!left.equals(JmmPrimitiveType.BOOLEAN))
                    addReport(newError(expr.getChild(0), "Operand of '" + op + "' must be boolean, got '" + left + "'"));
                if (!right.equals(JmmPrimitiveType.BOOLEAN))
                    addReport(newError(expr.getChild(1), "Operand of '" + op + "' must be boolean, got '" + right + "'"));
            }
            case "==", "!=" -> {
                if (!left.equals(right))
                    addReport(newError(expr, "Operands of '" + op + "' must be same type, got '" + left + "' and '" + right + "'"));
            }
        }
        return null;
    }

    private Void visitUnaryExpr(JmmNode expr, SymbolTable table) {
        JmmType t = types.getExprType(expr.getChild(0), currentMethod);
        if (t != null && !t.equals(JmmPrimitiveType.BOOLEAN))
            addReport(newError(expr.getChild(0), "Operand of '!' must be boolean, got '" + t + "'"));
        return null;
    }

    private Void visitUnaryArithExpr(JmmNode expr, SymbolTable table) {
        String op = expr.get("op");
        JmmType t = types.getExprType(expr.getChild(0), currentMethod);
        if (t != null && !t.equals(JmmPrimitiveType.INT))
            addReport(newError(expr.getChild(0), "Operand of '" + op + "' must be int, got '" + t + "'"));

        return null;
    }

}
