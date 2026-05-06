package pt.up.fe.comp2026.optimization;

import pt.up.fe.comp.jmm.analysis.table.MethodSymbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp2026.symboltable.JmmSymbolTable;
import pt.up.fe.comp.jmm.analysis.table.type.JmmType;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmArrayType;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmClassType;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmPrimitiveType;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2026.ast.TypeUtils;

import static pt.up.fe.comp2026.jmm.ast.JmmKind.*;

/**
 * Generates OLLIR code from expression JmmNodes.
 */
public class OllirExprGeneratorVisitor extends AJmmVisitor<Void, OllirExprResult> {

    private static final String SPACE = " ";
    private static final String ASSIGN = ":=";
    private static final String END_STMT = ";\n";

    private final JmmSymbolTable table;
    private final TypeUtils types;
    private final OptUtils ollirTypes;

    public MethodSymbol currentMethod;
    public JmmType expectedReturnType = null;

    public OllirExprGeneratorVisitor(SymbolTable table, OptUtils ollirTypes) {
        this.table = (JmmSymbolTable) table;
        this.types = new TypeUtils(table);
        this.ollirTypes = ollirTypes;
    }

    @Override
    protected void buildVisitor() {
        addVisit(INTEGER_LITERAL,         this::visitInteger);
        addVisit(BOOL_LITERAL,            this::visitBool);
        addVisit(VAR_REF_EXPR,            this::visitVarRef);
        addVisit(PAREN_EXPR,              this::visitParen);
        addVisit(BINARY_EXPR,             this::visitBinExpr);
        addVisit(UNARY_EXPR,              this::visitUnaryNot);
        addVisit(PLUS_EXPR,               this::visitUnaryPlus);
        addVisit(MINUS_EXPR,              this::visitUnaryMinus);
        addVisit(PLUS_PLUS_EXPR,          this::visitPreIncrement);
        addVisit(MINUS_MINUS_EXPR,        this::visitPreDecrement);
        addVisit(THIS_EXPR,               this::visitThis);
        addVisit(METHOD_CALL_EXPR,        this::visitMethodCall);
        addVisit(IMPLICIT_THIS_CALL_EXPR, this::visitImplicitThisCall);
        addVisit(NEW_EXPR,                this::visitNewExpr);
        addVisit(LENGTH_EXPR,             this::visitLengthExpr);
        addVisit(ARRAY_LOAD_EXPR,         this::visitArrayLoad);
        addVisit(ARRAY_INITIALIZER,       this::visitArrayInitializer);
        addVisit(NEW_ARRAY_EXPR,          this::visitNewArrayExpr);
    }

    // ── Literals ─────────────────────────────────────────────────────────────

    private OllirExprResult visitInteger(JmmNode node, Void unused) {
        return new OllirExprResult(node.get("value") + ".i32");
    }

    private OllirExprResult visitBool(JmmNode node, Void unused) {
        String val = node.get("value").equals("true") ? "1" : "0";
        return new OllirExprResult(val + ".bool");
    }

    // ── Variables ────────────────────────────────────────────────────────────

    private OllirExprResult visitVarRef(JmmNode node, Void unused) {
        String name = node.get("name");
        JmmType type = types.getExprType(node, currentMethod);

        if (type == null) {
            return new OllirExprResult(ollirTypes.sanitizeId(name));
        }

        String ollirType = ollirTypes.toOllirType(type);

        if (isField(name)) {
            String className = table.getClassName();
            StringBuilder computation = new StringBuilder();
            String tmp = ollirTypes.nextTemp() + ollirType;
            computation.append(tmp).append(SPACE)
                    .append(ASSIGN).append(ollirType).append(SPACE)
                    .append("getfield(this.").append(className).append(", ")
                    .append(ollirTypes.sanitizeId(name)).append(ollirType)
                    .append(")").append(ollirType).append(END_STMT);
            return new OllirExprResult(tmp, computation);
        }

        return new OllirExprResult(ollirTypes.sanitizeId(name) + ollirType);
    }

    private OllirExprResult visitThis(JmmNode node, Void unused) {
        return new OllirExprResult("this." + table.getClassName());
    }

    // ── Parentheses ──────────────────────────────────────────────────────────

    private OllirExprResult visitParen(JmmNode node, Void unused) {
        return visit(node.getChild(0));
    }

    // ── Binary expressions ───────────────────────────────────────────────────

    private OllirExprResult visitBinExpr(JmmNode node, Void unused) {
        String op = node.get("op");

        if (op.equals("&&")) {
            return visitAndExpr(node);
        }
        if (op.equals("||")) {
            return visitOrExpr(node);
        }

        var lhs = visit(node.getChild(0));
        var rhs = visit(node.getChild(1));

        StringBuilder computation = new StringBuilder();
        computation.append(lhs.getComputation());
        computation.append(rhs.getComputation());

        JmmType resType = types.getExprType(node, currentMethod);
        String resOllirType = ollirTypes.toOllirType(resType);
        String tmp = ollirTypes.nextTemp() + resOllirType;

        JmmType operandType = types.getExprType(node.getChild(0), currentMethod);
        String opOllirType = ollirTypes.toOllirType(operandType);

        computation.append(tmp).append(SPACE)
                .append(ASSIGN).append(resOllirType).append(SPACE)
                .append(lhs.getCode()).append(SPACE)
                .append(jmmOpToOllir(op)).append(opOllirType).append(SPACE)
                .append(rhs.getCode()).append(END_STMT);

        return new OllirExprResult(tmp, computation);
    }

    /**
     * Short-circuit AND: a && b
     *
     * tmp := 0.bool;
     * if (a) goto andCheck_;
     * goto andEnd_;
     * andCheck_:
     * if (b) goto andTrue_;
     * goto andEnd_;
     * andTrue_:
     * tmp := 1.bool;
     * andEnd_:
     */
    private OllirExprResult visitAndExpr(JmmNode node) {
        var lhs = visit(node.getChild(0));
        var rhs = visit(node.getChild(1));

        String labelCheck = ollirTypes.nextLabel("andCheck_");
        String labelTrue  = ollirTypes.nextLabel("andTrue_");
        String labelEnd   = ollirTypes.nextLabel("andEnd_");
        String tmp = ollirTypes.nextTemp() + ".bool";

        StringBuilder computation = new StringBuilder();

        // Evaluate lhs
        computation.append(lhs.getComputation());

        // Start as false
        computation.append(tmp).append(SPACE).append(ASSIGN).append(".bool 0.bool").append(END_STMT);

        // If lhs is true, go check rhs
        computation.append("if (").append(lhs.getCode()).append(") goto ").append(labelCheck).append(END_STMT);
        computation.append("goto ").append(labelEnd).append(END_STMT);

        // Check rhs
        computation.append(labelCheck).append(":\n");
        computation.append(rhs.getComputation());
        computation.append("if (").append(rhs.getCode()).append(") goto ").append(labelTrue).append(END_STMT);
        computation.append("goto ").append(labelEnd).append(END_STMT);

        // Both true
        computation.append(labelTrue).append(":\n");
        computation.append(tmp).append(SPACE).append(ASSIGN).append(".bool 1.bool").append(END_STMT);

        computation.append(labelEnd).append(":\n");

        return new OllirExprResult(tmp, computation);
    }

    /**
     * Short-circuit OR: a || b
     *
     * tmp := 1.bool;          // assume true
     * if (a) goto orEnd_;     // short-circuit: a is true → done
     * if (b) goto orEnd_;     // b is true → done
     * tmp := 0.bool;          // both false
     * orEnd_:
     */
    private OllirExprResult visitOrExpr(JmmNode node) {
        var lhs = visit(node.getChild(0));
        var rhs = visit(node.getChild(1));

        String labelEnd = ollirTypes.nextLabel("orEnd_");
        String tmp = ollirTypes.nextTemp() + ".bool";

        StringBuilder computation = new StringBuilder();

        // Evaluate lhs
        computation.append(lhs.getComputation());

        // Start as true (optimistic)
        computation.append(tmp).append(SPACE).append(ASSIGN).append(".bool 1.bool").append(END_STMT);

        // If lhs is true, short-circuit to end (result stays true)
        computation.append("if (").append(lhs.getCode()).append(") goto ").append(labelEnd).append(END_STMT);

        // Evaluate rhs
        computation.append(rhs.getComputation());

        // If rhs is true, short-circuit to end (result stays true)
        computation.append("if (").append(rhs.getCode()).append(") goto ").append(labelEnd).append(END_STMT);

        // Both false — set result to false
        computation.append(tmp).append(SPACE).append(ASSIGN).append(".bool 0.bool").append(END_STMT);

        computation.append(labelEnd).append(":\n");

        return new OllirExprResult(tmp, computation);
    }

    // ── Unary NOT ────────────────────────────────────────────────────────────

    private OllirExprResult visitUnaryNot(JmmNode node, Void unused) {
        var operand = visit(node.getChild(0));
        StringBuilder computation = new StringBuilder();
        computation.append(operand.getComputation());

        String tmp = ollirTypes.nextTemp() + ".bool";
        computation.append(tmp).append(SPACE)
                .append(ASSIGN).append(".bool !.bool ")
                .append(operand.getCode()).append(END_STMT);

        return new OllirExprResult(tmp, computation);
    }

    // ── Unary arithmetic ─────────────────────────────────────────────────────

    private OllirExprResult visitUnaryPlus(JmmNode node, Void unused) {
        return visit(node.getChild(0));
    }

    private OllirExprResult visitUnaryMinus(JmmNode node, Void unused) {
        var operand = visit(node.getChild(0));
        StringBuilder computation = new StringBuilder();
        computation.append(operand.getComputation());

        String tmp = ollirTypes.nextTemp() + ".i32";
        computation.append(tmp).append(SPACE)
                .append(ASSIGN).append(".i32 ")
                .append("0.i32 -.i32 ").append(operand.getCode())
                .append(END_STMT);

        return new OllirExprResult(tmp, computation);
    }

    private OllirExprResult visitPreIncrement(JmmNode node, Void unused) {
        var operand = visit(node.getChild(0));
        StringBuilder computation = new StringBuilder();
        computation.append(operand.getComputation());

        String tmp = ollirTypes.nextTemp() + ".i32";
        computation.append(tmp).append(SPACE)
                .append(ASSIGN).append(".i32 ")
                .append(operand.getCode()).append(" +.i32 1.i32")
                .append(END_STMT);

        return new OllirExprResult(tmp, computation);
    }

    private OllirExprResult visitPreDecrement(JmmNode node, Void unused) {
        var operand = visit(node.getChild(0));
        StringBuilder computation = new StringBuilder();
        computation.append(operand.getComputation());

        String tmp = ollirTypes.nextTemp() + ".i32";
        computation.append(tmp).append(SPACE)
                .append(ASSIGN).append(".i32 ")
                .append(operand.getCode()).append(" -.i32 1.i32")
                .append(END_STMT);

        return new OllirExprResult(tmp, computation);
    }

    // ── Method calls ─────────────────────────────────────────────────────────

    private OllirExprResult visitMethodCall(JmmNode node, Void unused) {
        String methodName = node.get("name");
        JmmNode receiverNode = node.getChild(0);

        StringBuilder computation = new StringBuilder();

        boolean isStatic = isStaticCall(receiverNode);

        StringBuilder argsCode = new StringBuilder();
        for (int i = 1; i < node.getNumChildren(); i++) {
            var arg = visit(node.getChild(i));
            computation.append(arg.getComputation());
            argsCode.append(", ").append(arg.getCode());
        }

        JmmType retType = types.getExprType(node, currentMethod);
        if (retType == null) retType = expectedReturnType;
        String retOllirType = retType != null ? ollirTypes.toOllirType(retType) : ".V";
        boolean isVoid = ".V".equals(retOllirType);

        String callExpr;
        if (isStatic) {
            String className = getStaticReceiverClassName(receiverNode);
            callExpr = "invokestatic(" + className
                    + ", \"" + methodName + "\"" + argsCode + ")" + retOllirType;
        } else {
            var receiver = visit(receiverNode);
            computation.insert(0, receiver.getComputation());
            callExpr = "invokevirtual(" + receiver.getCode()
                    + ", \"" + methodName + "\"" + argsCode + ")" + retOllirType;
        }

        if (isVoid) {
            computation.append(callExpr).append(END_STMT);
            return new OllirExprResult("", computation);
        } else {
            String tmp = ollirTypes.nextTemp() + retOllirType;
            computation.append(tmp).append(SPACE).append(ASSIGN).append(retOllirType)
                    .append(SPACE).append(callExpr).append(END_STMT);
            return new OllirExprResult(tmp, computation);
        }
    }

    private OllirExprResult visitImplicitThisCall(JmmNode node, Void unused) {
        String methodName = node.get("name");
        StringBuilder computation = new StringBuilder();

        boolean isStaticMethod = table.getMethods(methodName).stream()
                .anyMatch(MethodSymbol::isStatic);

        StringBuilder argsCode = new StringBuilder();
        for (int i = 0; i < node.getNumChildren(); i++) {
            var arg = visit(node.getChild(i));
            computation.append(arg.getComputation());
            argsCode.append(", ").append(arg.getCode());
        }

        JmmType retType = types.getExprType(node, currentMethod);
        if (retType == null) retType = expectedReturnType;
        String retOllirType = retType != null ? ollirTypes.toOllirType(retType) : ".V";
        boolean isVoid = ".V".equals(retOllirType);

        String callExpr;
        if (isStaticMethod) {
            String className = table.getClassName();
            callExpr = "invokestatic(" + className
                    + ", \"" + methodName + "\"" + argsCode + ")" + retOllirType;
        } else {
            String receiverCode = "this." + table.getClassName();
            callExpr = "invokevirtual(" + receiverCode
                    + ", \"" + methodName + "\"" + argsCode + ")" + retOllirType;
        }

        if (isVoid) {
            computation.append(callExpr).append(END_STMT);
            return new OllirExprResult("", computation);
        } else {
            String tmp = ollirTypes.nextTemp() + retOllirType;
            computation.append(tmp).append(SPACE).append(ASSIGN).append(retOllirType)
                    .append(SPACE).append(callExpr).append(END_STMT);
            return new OllirExprResult(tmp, computation);
        }
    }

    // ── Object creation ──────────────────────────────────────────────────────

    private OllirExprResult visitNewExpr(JmmNode node, Void unused) {
        String className = node.get("name");
        StringBuilder computation = new StringBuilder();

        String objType = "." + className;
        String tmp = ollirTypes.nextTemp() + objType;

        computation.append(tmp).append(SPACE)
                .append(ASSIGN).append(objType).append(SPACE)
                .append("new(").append(className).append(")").append(objType).append(END_STMT);

        StringBuilder argsCode = new StringBuilder();
        for (int i = 0; i < node.getNumChildren(); i++) {
            var arg = visit(node.getChild(i));
            computation.append(arg.getComputation());
            argsCode.append(", ").append(arg.getCode());
        }

        computation.append("invokespecial(").append(tmp)
                .append(", \"<init>\"").append(argsCode).append(").V").append(END_STMT);

        return new OllirExprResult(tmp, computation);
    }

    // ── Array operations ─────────────────────────────────────────────────────

    private OllirExprResult visitLengthExpr(JmmNode node, Void unused) {
        var target = visit(node.getChild(0));
        StringBuilder computation = new StringBuilder();
        computation.append(target.getComputation());

        String tmp = ollirTypes.nextTemp() + ".i32";
        computation.append(tmp).append(SPACE).append(ASSIGN).append(".i32 ")
                .append("arraylength(").append(target.getCode()).append(").i32").append(END_STMT);

        return new OllirExprResult(tmp, computation);
    }

    private OllirExprResult visitArrayLoad(JmmNode node, Void unused) {
        // Para a[i][j], o AST tem: ArrayLoadExpr -> [array, index1, index2]
        // Precisamos de "flatten": primeiro lê a[i] (devolve int[]), depois lê resultado[j]

        int numIndices = node.getNumChildren() - 1;

        // Resultado inicial: o próprio array (filho 0)
        var current = visit(node.getChild(0));
        StringBuilder computation = new StringBuilder();
        computation.append(current.getComputation());

        JmmType currentType = types.getExprType(node.getChild(0), currentMethod);

        for (int i = 1; i <= numIndices; i++) {
            var index = visit(node.getChild(i));
            computation.append(index.getComputation());

            if (!(currentType instanceof JmmArrayType arr)) break;

            // Após um acesso, o tipo resultante perde uma dimensão
            JmmType elemType = arr.dimension() == 1
                    ? arr.itemType()
                    : new JmmArrayType(arr.itemType(), arr.dimension() - 1);

            String elemOllirType = ollirTypes.toOllirType(elemType);
            String tmp = ollirTypes.nextTemp() + elemOllirType;

            computation.append(tmp).append(SPACE).append(ASSIGN).append(elemOllirType).append(SPACE)
                    .append(current.getCode()).append("[").append(index.getCode()).append("]")
                    .append(elemOllirType).append(END_STMT);

            current = new OllirExprResult(tmp, "");
            currentType = elemType;
        }

        return new OllirExprResult(current.getCode(), computation);
    }

    private OllirExprResult visitNewArrayExpr(JmmNode node, Void unused) {
        StringBuilder computation = new StringBuilder();
        StringBuilder sizeArgs = new StringBuilder();
        for (int i = 0; i < node.getNumChildren(); i++) {
            var sizeExpr = visit(node.getChild(i));
            computation.append(sizeExpr.getComputation());
            if (i > 0) sizeArgs.append(", ");
            sizeArgs.append(sizeExpr.getCode());
        }

        JmmType arrType = types.getExprType(node, currentMethod);
        String arrOllirType = ollirTypes.toOllirType(arrType);
        String tmp = ollirTypes.nextTemp() + arrOllirType;
        computation.append(tmp).append(SPACE).append(ASSIGN).append(arrOllirType).append(SPACE)
                .append("new(array, ").append(sizeArgs).append(")").append(arrOllirType).append(END_STMT);

        return new OllirExprResult(tmp, computation);
    }

    private OllirExprResult visitArrayInitializer(JmmNode node, Void unused) {
        int size = node.getNumChildren();
        StringBuilder computation = new StringBuilder();

        String arr = ollirTypes.nextTemp() + ".array.i32";
        computation.append(arr).append(SPACE).append(ASSIGN).append(".array.i32 ")
                .append("new(array, ").append(size).append(".i32).array.i32").append(END_STMT);

        for (int i = 0; i < size; i++) {
            var elem = visit(node.getChild(i));
            computation.append(elem.getComputation());
            computation.append(arr).append("[").append(i).append(".i32].i32")
                    .append(SPACE).append(ASSIGN).append(".i32 ").append(elem.getCode()).append(END_STMT);
        }

        return new OllirExprResult(arr, computation);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private boolean isField(String name) {
        if (currentMethod != null) {
            if (currentMethod.getParameter(name).isPresent()) return false;
            if (currentMethod.getLocalVariable(name).isPresent()) return false;
        }
        return table.getField(name).isPresent();
    }

    private boolean isStaticCall(JmmNode receiverNode) {
        if (!VAR_REF_EXPR.check(receiverNode)) return false;
        String name = receiverNode.get("name");
        if (currentMethod != null) {
            if (currentMethod.getParameter(name).isPresent()) return false;
            if (currentMethod.getLocalVariable(name).isPresent()) return false;
        }
        if (table.getField(name).isPresent()) return false;
        return table.getImportNames().contains(name)
                || table.isImplicitImport(name)
                || name.equals(table.getClassName());
    }

    private String getStaticReceiverClassName(JmmNode receiverNode) {
        String name = receiverNode.get("name");
        if (name.equals(table.getClassName())) {
            return table.getClassName();
        }
        return name;
    }

    private String jmmOpToOllir(String op) {
        return op;
    }
}
