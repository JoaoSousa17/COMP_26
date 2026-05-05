package pt.up.fe.comp2026.optimization;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;
import pt.up.fe.comp.jmm.ast.PostorderJmmVisitor;

import static pt.up.fe.comp2026.jmm.ast.JmmKind.*;

public class ConstantFoldingVisitor extends PostorderJmmVisitor<Void, Void> {
    private boolean changed = false;

    public ConstantFoldingVisitor() {
        setDefaultValue(() -> null);
        buildVisitor();
    }

    @Override
    protected void buildVisitor() {
        addVisit(BINARY_EXPR, this::foldBinaryExpr);
    }

    public boolean hasChanged() { return changed; }

    private Void foldBinaryExpr(JmmNode node, Void unused) {
        JmmNode left = node.getChild(0);
        JmmNode right = node.getChild(1);
        String op = node.get("op");

        boolean leftIsInt = left.isInstance(INTEGER_LITERAL);
        boolean rightIsInt = right.isInstance(INTEGER_LITERAL);
        boolean leftIsBool = left.isInstance(BOOL_LITERAL);
        boolean rightIsBool = right.isInstance(BOOL_LITERAL);

        // Integer arithmetic and comparison
        if (leftIsInt && rightIsInt) {
            int li = Integer.parseInt(left.get("value"));
            int ri = Integer.parseInt(right.get("value"));

            switch (op) {
                case "+" -> replace(node, intLiteral(li + ri));
                case "-" -> replace(node, intLiteral(li - ri));
                case "*" -> replace(node, intLiteral(li * ri));
                case "/" -> {
                    if (ri != 0) replace(node, intLiteral(li / ri));
                }
                case "%" -> {
                    if (ri != 0) replace(node, intLiteral(li % ri));
                }
                case "<" -> replace(node, boolLiteral(li < ri));
                case "<=" -> replace(node, boolLiteral(li <= ri));
                case ">" -> replace(node, boolLiteral(li > ri));
                case ">=" -> replace(node, boolLiteral(li >= ri));
                case "==" -> replace(node, boolLiteral(li == ri));
                case "!=" -> replace(node, boolLiteral(li != ri));
            }
        }

        // Boolean logical operations
        if (leftIsBool && rightIsBool) {
            boolean lb = Boolean.parseBoolean(left.get("value"));
            boolean rb = Boolean.parseBoolean(right.get("value"));

            switch (op) {
                case "&&" -> replace(node, boolLiteral(lb && rb));
                case "||" -> replace(node, boolLiteral(lb || rb));
                case "==" -> replace(node, boolLiteral(lb == rb));
                case "!=" -> replace(node, boolLiteral(lb != rb));
            }
        }
        return null;
    }

    private void replace(JmmNode original, JmmNode replacement) {
        original.replace(replacement);
    }

    private JmmNode intLiteral(int value) {
        var node = new JmmNodeImpl(INTEGER_LITERAL);
        node.put("value", String.valueOf(value));
        return node;
    }

    private JmmNode boolLiteral(boolean value) {
        var node = new JmmNodeImpl(BOOL_LITERAL);
        node.put("value", String.valueOf(value));
        return node;
    }
}
