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
 *
 * Returns an {@link OllirExprResult}:
 *   - computation : OLLIR statements needed before the value is usable
 *   - code        : the OLLIR operand for the result (e.g. "tmp0.i32")
 */
public class OllirExprGeneratorVisitor extends AJmmVisitor<Void, OllirExprResult> {

    private static final String SPACE = " ";
    private static final String ASSIGN = ":=";
    private static final String END_STMT = ";\n";

    private final JmmSymbolTable table;
    private final TypeUtils types;
    private final OptUtils ollirTypes;

    /** Set by OllirGeneratorVisitor when entering each method. */
    public MethodSymbol currentMethod;

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

    /**
     * Variable reference. If the name resolves to a class field (not a local/param),
     * emit a getfield instruction and return a fresh tmp.
     */
    private OllirExprResult visitVarRef(JmmNode node, Void unused) {
        String name = node.get("name");
        JmmType type = types.getExprType(node, currentMethod);
        String ollirType = ollirTypes.toOllirType(type);

        // Check if it's a field (not found as local or param)
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
        String className = table.getClassName();
        return new OllirExprResult("this." + className);
    }

    // ── Parentheses ──────────────────────────────────────────────────────────

    private OllirExprResult visitParen(JmmNode node, Void unused) {
        return visit(node.getChild(0));
    }

    // ── Binary expressions ───────────────────────────────────────────────────

    private OllirExprResult visitBinExpr(JmmNode node, Void unused) {
        String op = node.get("op");

        // && must be compiled as short-circuit (CondBranch), not a BinaryOp
        if (op.equals("&&")) {
            return visitAndExpr(node);
        }

        var lhs = visit(node.getChild(0));
        var rhs = visit(node.getChild(1));

        StringBuilder computation = new StringBuilder();
        computation.append(lhs.getComputation());
        computation.append(rhs.getComputation());

        JmmType resType = types.getExprType(node, currentMethod);
        String resOllirType = ollirTypes.toOllirType(resType);
        String tmp = ollirTypes.nextTemp() + resOllirType;

        // OLLIR operator token uses the operand type, not the result type
        JmmType operandType = types.getExprType(node.getChild(0), currentMethod);
        String opOllirType = ollirTypes.toOllirType(operandType);
        String ollirOp = jmmOpToOllir(op);

        computation.append(tmp).append(SPACE)
                .append(ASSIGN).append(resOllirType).append(SPACE)
                .append(lhs.getCode()).append(SPACE)
                .append(ollirOp).append(opOllirType).append(SPACE)
                .append(rhs.getCode()).append(END_STMT);

        return new OllirExprResult(tmp, computation);
    }

    /**
     * Short-circuit AND:
     *
     *   tmp = 0.bool;
     *   if (!lhs) goto andEnd;
     *   if (!rhs) goto andEnd;
     *   tmp = 1.bool;
     *   andEnd:
     */
    private OllirExprResult visitAndExpr(JmmNode node) {
        var lhs = visit(node.getChild(0));
        var rhs = visit(node.getChild(1));

        String labelEnd = ollirTypes.nextTemp("andEnd_");
        String tmp = ollirTypes.nextTemp() + ".bool";

        StringBuilder computation = new StringBuilder();
        computation.append(lhs.getComputation());
        computation.append(rhs.getComputation());

        // Default result: false
        computation.append(tmp).append(SPACE).append(ASSIGN).append(".bool 0.bool").append(END_STMT);

        // if lhs is false → skip to end
        computation.append("if (!.bool ").append(lhs.getCode()).append(") goto ").append(labelEnd).append(END_STMT);
        // if rhs is false → skip to end
        computation.append("if (!.bool ").append(rhs.getCode()).append(") goto ").append(labelEnd).append(END_STMT);

        // Both true
        computation.append(tmp).append(SPACE).append(ASSIGN).append(".bool 1.bool").append(END_STMT);

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

    // ── Method calls ─────────────────────────────────────────────────────────

    private OllirExprResult visitMethodCall(JmmNode node, Void unused) {
        String methodName = node.get("name");
        JmmNode receiverNode = node.getChild(0);

        var receiver = visit(receiverNode);
        StringBuilder computation = new StringBuilder();
        computation.append(receiver.getComputation());

        boolean isStatic = isStaticCall(receiverNode);
        String invokeKind = isStatic ? "invokestatic" : "invokevirtual";

        StringBuilder argsCode = new StringBuilder();
        for (int i = 1; i < node.getNumChildren(); i++) {
            var arg = visit(node.getChild(i));
            computation.append(arg.getComputation());
            argsCode.append(", ").append(arg.getCode());
        }

        JmmType retType = types.getExprType(node, currentMethod);
        String retOllirType = retType != null
                ? ollirTypes.toOllirType(retType)
                : ".V";
        boolean isVoid = ".V".equals(retOllirType);

        String callExpr = invokeKind + "(" + receiver.getCode()
                + ", \"" + methodName + "\"" + argsCode + ")" + retOllirType;

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

        String className = table.getClassName();
        String receiverCode = "this." + className;

        StringBuilder argsCode = new StringBuilder();
        for (int i = 0; i < node.getNumChildren(); i++) {
            var arg = visit(node.getChild(i));
            computation.append(arg.getComputation());
            argsCode.append(", ").append(arg.getCode());
        }

        JmmType retType = types.getExprType(node, currentMethod);
        String retOllirType = retType != null ? ollirTypes.toOllirType(retType) : ".V";
        boolean isVoid = ".V".equals(retOllirType);

        String callExpr = "invokevirtual(" + receiverCode
                + ", \"" + methodName + "\"" + argsCode + ")" + retOllirType;

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
        var array = visit(node.getChild(0));
        var index = visit(node.getChild(1));
        StringBuilder computation = new StringBuilder();
        computation.append(array.getComputation());
        computation.append(index.getComputation());

        JmmType elemType = types.getExprType(node, currentMethod);
        String elemOllirType = ollirTypes.toOllirType(elemType);
        String tmp = ollirTypes.nextTemp() + elemOllirType;

        computation.append(tmp).append(SPACE).append(ASSIGN).append(elemOllirType).append(SPACE)
                .append(array.getCode()).append("[").append(index.getCode()).append("]")
                .append(elemOllirType).append(END_STMT);

        return new OllirExprResult(tmp, computation);
    }

    private OllirExprResult visitNewArrayExpr(JmmNode node, Void unused) {
        var sizeExpr = visit(node.getChild(0));
        StringBuilder computation = new StringBuilder();
        computation.append(sizeExpr.getComputation());

        String tmp = ollirTypes.nextTemp() + ".array.i32";
        computation.append(tmp).append(SPACE).append(ASSIGN).append(".array.i32 ")
                .append("new(array, ").append(sizeExpr.getCode()).append(").array.i32").append(END_STMT);

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

    /**
     * Returns true if {@code name} is a class field (not a local variable or parameter
     * of the current method).
     */
    private boolean isField(String name) {
        if (currentMethod != null) {
            if (currentMethod.getParameter(name).isPresent()) return false;
            if (currentMethod.getLocalVariable(name).isPresent()) return false;
        }
        return table.getField(name).isPresent();
    }

    /**
     * Returns true if the receiver of a method call should use invokestatic.
     * A VarRefExpr that resolves to an imported class name (not a variable) is static.
     */
    private boolean isStaticCall(JmmNode receiverNode) {
        if (!VAR_REF_EXPR.check(receiverNode)) return false;
        String name = receiverNode.get("name");
        // If it is not a local/param/field, treat as class name → static
        if (currentMethod != null) {
            if (currentMethod.getParameter(name).isPresent()) return false;
            if (currentMethod.getLocalVariable(name).isPresent()) return false;
        }
        if (table.getField(name).isPresent()) return false;
        return table.getImportNames().contains(name) || table.isImplicitImport(name);
    }

    /**
     * Maps a JMM operator symbol to its OLLIR keyword.
     * OLLIR uses the same symbols for arithmetic; for comparison, it uses the same symbols too.
     * The OLLIR parser recognises: +, -, *, /, <, &&, ||, ==, !=, <=, >=
     */
    private String jmmOpToOllir(String op) {
        return op; // OLLIR uses the same operator symbols as JMM
    }
}

