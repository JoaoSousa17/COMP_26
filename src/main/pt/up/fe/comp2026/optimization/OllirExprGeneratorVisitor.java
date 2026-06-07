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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
     */
    private OllirExprResult visitAndExpr(JmmNode node) {
        var lhs = visit(node.getChild(0));
        var rhs = visit(node.getChild(1));

        String labelCheck = ollirTypes.nextLabel("andCheck_");
        String labelTrue  = ollirTypes.nextLabel("andTrue_");
        String labelEnd   = ollirTypes.nextLabel("andEnd_");
        String tmp = ollirTypes.nextTemp() + ".bool";

        StringBuilder computation = new StringBuilder();

        computation.append(lhs.getComputation());
        computation.append(tmp).append(SPACE).append(ASSIGN).append(".bool 0.bool").append(END_STMT);
        computation.append("if (").append(lhs.getCode()).append(") goto ").append(labelCheck).append(END_STMT);
        computation.append("goto ").append(labelEnd).append(END_STMT);

        computation.append(labelCheck).append(":\n");
        computation.append(rhs.getComputation());
        computation.append("if (").append(rhs.getCode()).append(") goto ").append(labelTrue).append(END_STMT);
        computation.append("goto ").append(labelEnd).append(END_STMT);

        computation.append(labelTrue).append(":\n");
        computation.append(tmp).append(SPACE).append(ASSIGN).append(".bool 1.bool").append(END_STMT);

        computation.append(labelEnd).append(":\n");

        return new OllirExprResult(tmp, computation);
    }

    /**
     * Short-circuit OR: a || b
     */
    private OllirExprResult visitOrExpr(JmmNode node) {
        var lhs = visit(node.getChild(0));
        var rhs = visit(node.getChild(1));

        String labelEnd = ollirTypes.nextLabel("orEnd_");
        String tmp = ollirTypes.nextTemp() + ".bool";

        StringBuilder computation = new StringBuilder();

        computation.append(lhs.getComputation());
        computation.append(tmp).append(SPACE).append(ASSIGN).append(".bool 1.bool").append(END_STMT);
        computation.append("if (").append(lhs.getCode()).append(") goto ").append(labelEnd).append(END_STMT);
        computation.append(rhs.getComputation());
        computation.append("if (").append(rhs.getCode()).append(") goto ").append(labelEnd).append(END_STMT);
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

        List<String> paramFqns = getImportedMethodParamFqns(receiverNode, methodName, node.getNumChildren() - 1);

        StringBuilder argsCode = new StringBuilder();
        for (int i = 1; i < node.getNumChildren(); i++) {
            var argNode = node.getChild(i);
            var arg = visit(argNode);
            computation.append(arg.getComputation());
            String argCode = arg.getCode();
            if (paramFqns != null && i - 1 < paramFqns.size()) {
                String paramFqn = paramFqns.get(i - 1);
                if (paramFqn != null) {
                    String argFqn = resolveClassTypeFqn(types.getExprType(argNode, currentMethod));
                    if (!paramFqn.equals(argFqn)) {
                        argCode = retypeArgCode(argCode, paramFqn);
                    }
                }
            }
            argsCode.append(", ").append(argCode);
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
        int numIndices = node.getNumChildren() - 1;

        var current = visit(node.getChild(0));
        StringBuilder computation = new StringBuilder();
        computation.append(current.getComputation());

        JmmType currentType = types.getExprType(node.getChild(0), currentMethod);

        for (int i = 1; i <= numIndices; i++) {
            var index = visit(node.getChild(i));
            computation.append(index.getComputation());

            if (!(currentType instanceof JmmArrayType arr)) break;

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

        // Collect all size expressions
        int numDimensions = node.getNumChildren();
        String[] sizeCodeArr = new String[numDimensions];
        for (int i = 0; i < numDimensions; i++) {
            var sizeExpr = visit(node.getChild(i));
            computation.append(sizeExpr.getComputation());
            sizeCodeArr[i] = sizeExpr.getCode();
        }

        // Determine the base element type from the declared array type.
        // For `new int[2][3]`, getExprType may return JmmArrayType(int, 2) or JmmArrayType(int, 1).
        // We need to produce a type with exactly `numDimensions` array wrappers over the primitive base.
        JmmType declaredType = types.getExprType(node, currentMethod);

        // Walk down to the innermost non-array type (the base element type)
        JmmType baseType = declaredType;
        while (baseType instanceof JmmArrayType arr) {
            baseType = arr.itemType();
        }

        // Re-wrap with exactly numDimensions levels of array
        JmmType arrType = buildArrayType(baseType, numDimensions);
        String arrOllirType = ollirTypes.toOllirType(arrType);

        // Build args string: all size args comma-separated
        StringBuilder sizeArgs = new StringBuilder();
        for (int i = 0; i < numDimensions; i++) {
            if (i > 0) sizeArgs.append(", ");
            sizeArgs.append(sizeCodeArr[i]);
        }

        String tmp = ollirTypes.nextTemp() + arrOllirType;
        computation.append(tmp).append(SPACE).append(ASSIGN).append(arrOllirType).append(SPACE)
                .append("new(array, ").append(sizeArgs).append(")").append(arrOllirType).append(END_STMT);

        return new OllirExprResult(tmp, computation);
    }

    /**
     * Builds a JmmArrayType with the given number of dimensions over the base type.
     * e.g. buildArrayType(int, 2) → JmmArrayType(int, 2)  →  .array.array.i32
     */
    private JmmType buildArrayType(JmmType baseType, int dimensions) {
        if (dimensions <= 0) return baseType;
        return new JmmArrayType(baseType, dimensions);
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

    /**
     * For calls on imported classes, returns the FQN of each parameter type so that
     * argument operands can be retyped to match the declared parameter (not the
     * concrete expression type). Returns null when the receiver is not a known
     * imported class or the method is not found via reflection.
     * Primitive parameters map to null (no retyping needed — OLLIR type is already correct).
     */
    private List<String> getImportedMethodParamFqns(JmmNode receiverNode, String methodName, int argCount) {
        JmmType receiverType = types.getExprType(receiverNode, currentMethod);
        if (!(receiverType instanceof JmmClassType classType)) return null;

        String simpleName = classType.name();
        String fqn = table.getImportedFullyQualifiedName(simpleName)
                .or(() -> table.getImplicitImport(simpleName).map(st -> st.getFullyQualifiedName()))
                .orElse(null);
        if (fqn == null) return null;

        try {
            Class<?> clazz = Class.forName(fqn);
            for (var m : clazz.getMethods()) {
                if (m.getName().equals(methodName) && m.getParameterCount() == argCount) {
                    return Arrays.stream(m.getParameterTypes())
                            .map(p -> (p.isPrimitive() || p.isArray()) ? null : p.getName())
                            .collect(Collectors.toList());
                }
            }
        } catch (ClassNotFoundException ignored) {}
        return null;
    }

    private String retypeArgCode(String code, String fqn) {
        int dot = code.indexOf('.');
        String varPart = dot < 0 ? code : code.substring(0, dot);
        int lastDot = fqn.lastIndexOf('.');
        String simpleName = lastDot >= 0 ? fqn.substring(lastDot + 1) : fqn;
        return varPart + "." + simpleName;
    }

    /** Returns the fully-qualified class name for a class type, or null if not a class type. */
    private String resolveClassTypeFqn(JmmType type) {
        if (!(type instanceof JmmClassType classType)) return null;
        String name = classType.name();
        return table.getImportedFullyQualifiedName(name)
                .or(() -> table.getImplicitImport(name).map(st -> st.getFullyQualifiedName()))
                .orElseGet(() -> name.equals(table.getClassName()) ? table.getFullyQualifiedName() : name);
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
