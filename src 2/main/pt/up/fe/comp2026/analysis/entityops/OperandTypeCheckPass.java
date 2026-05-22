package pt.up.fe.comp2026.analysis.entityops;

import pt.up.fe.comp.jmm.analysis.table.MethodSymbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.type.JmmType;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmPrimitiveType;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2026.analysis.AnalysisVisitorWithTable;
import pt.up.fe.comp2026.jmm.ast.JmmKind;

/**
 * Analysis pass that validates operand types for unary and binary expressions.
 *
 * Rules enforced:
 *   - Arithmetic and relational operators ({@code +, -, *, /, %, <, >, <=, >=}) require int operands.
 *   - Logical operators ({@code &&, ||}) require boolean operands.
 *   - Equality operators ({@code ==, !=}) require both operands to be the same type.
 *   - The logical negation operator ({@code !}) requires a boolean operand.
 *   - Prefix increment/decrement ({@code ++, --}) and unary {@code +/-} require an int operand.
 */
public class OperandTypeCheckPass extends AnalysisVisitorWithTable {

    /** The method currently being visited, used for type resolution. */
    private MethodSymbol currentMethod;

    public OperandTypeCheckPass(SymbolTable table) {
        super(table);
        buildVisitor();
    }

    @Override
    protected void buildVisitor() {
        addVisit(JmmKind.METHOD_DECL,      this::visitMethodDecl);
        addVisit(JmmKind.BINARY_EXPR,      this::visitBinaryExpr);
        addVisit(JmmKind.UNARY_EXPR,       this::visitUnaryExpr);
        addVisit(JmmKind.PLUS_PLUS_EXPR,   this::visitIntUnaryExpr);
        addVisit(JmmKind.MINUS_MINUS_EXPR, this::visitIntUnaryExpr);
        addVisit(JmmKind.PLUS_EXPR,        this::visitIntUnaryExpr);
        addVisit(JmmKind.MINUS_EXPR,       this::visitIntUnaryExpr);
    }

    /** Tracks the current method being analysed for scoped type resolution. */
    private Void visitMethodDecl(JmmNode method, SymbolTable table) {
        currentMethod = this.table.getMethod(types.getMethodDeclSignature(method)).orElse(null);
        return null;
    }

    /**
     * Validates operand types for a binary expression according to the operator:
     *   - Arithmetic/relational: both operands must be int.
     *   - Logical: both operands must be boolean.
     *   - Equality: both operands must be the same type.
     */
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

    /** Validates that the operand of the {@code !} operator is of type boolean. */
    private Void visitUnaryExpr(JmmNode expr, SymbolTable table) {
        JmmType t = types.getExprType(expr.getChild(0), currentMethod);
        if (t != null && !t.equals(JmmPrimitiveType.BOOLEAN))
            addReport(newError(expr.getChild(0), "Operand of '!' must be boolean, got '" + t + "'"));
        return null;
    }

    /** Validates that the operand of {@code ++}, {@code --}, unary {@code +}, or unary {@code -} is of type int. */
    private Void visitIntUnaryExpr(JmmNode expr, SymbolTable table) {
        JmmNode operand = expr.getChild(0);
        JmmType t = types.getExprType(operand, currentMethod);
        if (t != null && !t.equals(JmmPrimitiveType.INT)) {
            String op = String.valueOf(expr.getKind());
            addReport(newError(operand, "Operand of '" + op + "' must be int, got '" + t + "'"));
        }
        return null;
    }
}
