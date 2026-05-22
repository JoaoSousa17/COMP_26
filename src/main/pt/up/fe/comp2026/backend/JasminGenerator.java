package pt.up.fe.comp2026.backend;

import org.specs.comp.ollir.*;
import org.specs.comp.ollir.inst.*;
import org.specs.comp.ollir.tree.TreeNode;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp2026.optimization.OptUtils;
import pt.up.fe.specs.util.classmap.FunctionClassMap;
import pt.up.fe.specs.util.utilities.StringLines;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Generates Jasmin code from an OllirResult.
 * <p>
 * One JasminGenerator instance per OllirResult.
 */
public class JasminGenerator {

    private static final String NL = "\n";
    private static final String TAB = "   ";

    final OllirResult ollirResult;

    private final List<Report> reports;

    private String code;

    Method currentMethod;

    final StackTracker stack = new StackTracker();

    String pendingNewStore;

    final JasminUtils types;
    OptUtils utils;
    private final FunctionClassMap<TreeNode, String> generators;
    private final JasminAssignEmitter assignEmitter = new JasminAssignEmitter(this);
    private final JasminCallEmitter callEmitter = new JasminCallEmitter(this);
    private final JasminExpressionEmitter exprEmitter = new JasminExpressionEmitter(this);

    public JasminGenerator(OllirResult ollirResult) {
        this.ollirResult = ollirResult;

        reports = new ArrayList<>();
        code = null;
        currentMethod = null;
        pendingNewStore = null;

        types = new JasminUtils(ollirResult);
        utils = null;
        this.generators = new FunctionClassMap<>();
        generators.put(ClassUnit.class, this::generateClassUnit);
        generators.put(Method.class, this::generateMethod);
        generators.put(AssignInstruction.class, assignEmitter::generate);
        generators.put(SingleOpInstruction.class, exprEmitter::singleOp);
        generators.put(LiteralElement.class, exprEmitter::literal);
        generators.put(Operand.class, exprEmitter::operand);
        generators.put(BinaryOpInstruction.class, exprEmitter::binaryOp);
        generators.put(ReturnInstruction.class, this::generateReturn);
        generators.put(GotoInstruction.class, this::generateGoto);
        generators.put(OpCondInstruction.class, this::generateOpCond);
        generators.put(SingleOpCondInstruction.class, this::generateSingleOpCond);
        generators.put(UnaryOpInstruction.class, exprEmitter::unaryOp);
        generators.put(GetFieldInstruction.class, callEmitter::getField);
        generators.put(PutFieldInstruction.class, callEmitter::putField);
        generators.put(InvokeStaticInstruction.class, callEmitter::invokeStatic);
        generators.put(InvokeVirtualInstruction.class, callEmitter::invokeVirtual);
        generators.put(InvokeSpecialInstruction.class, callEmitter::invokeSpecial);
    }


    String apply(TreeNode node) {
        return generators.apply(node);
    }

    public List<Report> getReports() {
        return reports;
    }

    public String build() {

        // This way, build is idempotent
        if (code == null) {
            code = apply(ollirResult.getOllirClass());
        }

        return code;
    }

    private String generateClassUnit(ClassUnit classUnit) {
        var code = new StringBuilder();

        var nameWithPackage = ollirResult.getOllirClass().getClassFullyQualifiedName().replace('.', '/');
        code.append(".class public ").append(nameWithPackage).append(NL).append(NL);

        String superClassName = classUnit.getSuperClass();
        String fullSuperClass =  (superClassName == null || superClassName.equals("Object"))
                ? "java/lang/Object"
                : types.resolveClassName(superClassName);

        code.append(".super ").append(fullSuperClass).append(NL);

        for (var field : classUnit.getFields()) {
            code.append(generateField(field));
        }

        // generate a single constructor method
        var defaultConstructor = """
                ;default constructor
                .method public <init>()V
                    aload_0
                    invokespecial %s/<init>()V
                    return
                .end method
                """.formatted(fullSuperClass);
        code.append(defaultConstructor);

        for (var method : ollirResult.getOllirClass().getMethods()) {
            if (method.isConstructMethod()) {
                continue;
            }

            code.append(apply(method));
        }

        return code.toString();
    }

    private String generateField(Field field) {
        var modifier = types.getModifier(field.getFieldAccessModifier());
        var staticMod = field.isStaticField() ? "static " : "";
        var name = field.getFieldName();
        var desciptor = types.getTypeDescriptor(field.getFieldType());
        return ".field " + modifier + staticMod + "'" + name + "'" + " " + desciptor + NL;
    }

    private String generateMethod(Method method) {
        currentMethod = method;
        stack.reset();
        utils = new OptUtils(null);

        var code = new StringBuilder();

        var modifier = types.getModifier(method.getMethodAccessModifier());

        var staticMod = method.isStaticMethod() ? "static " : "";

        var methodName = method.getMethodName();

        var params = method.getParams().stream()
                .map(p -> types.getTypeDescriptor(p.getType()))
                .collect(Collectors.joining());

        var returnType = types.getTypeDescriptor(method.getReturnType());

        code.append("\n.method ").append(modifier)
                .append(staticMod)
                .append(methodName).append("(").append(params).append(")").append(returnType).append(NL);


        var bodyCode = new StringBuilder();
        for (var inst : method.getInstructions()) {
            for (var label : currentMethod.getLabels(inst)) {
                bodyCode.append(label).append(":").append(NL);
            }
            for (var line : StringLines.getLines(apply(inst))) {
                if (line.isBlank()) continue;
                if (line.endsWith(":")) {
                    bodyCode.append(line).append(NL);
                } else {
                    bodyCode.append(TAB).append(line).append(NL);
                }
            }
        }

        // Add limits
        int maxReg = method.getVarTable().values().stream()
                .mapToInt(Descriptor::getVirtualReg)
                .max()
                .orElse(method.isStaticMethod() ? -1 : 0);
        int limitLocals = maxReg + 1;

        code.append(TAB).append(".limit stack ").append(stack.getMax()).append(NL);
        code.append(TAB).append(".limit locals ").append(limitLocals).append(NL);

        code.append(TAB).append(bodyCode);

        code.append(".end method\n");
        currentMethod = null;
        return code.toString();
    }

    private String generateReturn(ReturnInstruction returnInst) {
        var code = new StringBuilder();

        var returnType = returnInst.getReturnType();

        var typePrefix = types.getTypePrefix(returnType);

        returnInst.getOperand().ifPresent(op -> {
            code.append(apply(op));
            stack.update(-1);
        });

        code.append(typePrefix).append("return").append(NL);

        return code.toString();
    }

    private String generateGoto(GotoInstruction gotoInst) {
        return "goto " + gotoInst.getLabel() + NL;
    }

    private String generateSingleOpCond(SingleOpCondInstruction condInst) {
        var code = new StringBuilder();
        code.append(apply(condInst.getCondition().getSingleOperand()));
        stack.update(-1);
        code.append("ifne ").append(condInst.getLabel()).append(NL);
        return code.toString();
    }

    private String generateOpCond(OpCondInstruction condInst) {
        var code = new StringBuilder();
        var condition = condInst.getCondition();

        if (condition instanceof BinaryOpInstruction binOp) {
            code.append(exprEmitter.emitComparisonBranch(binOp, condInst.getLabel()));
        } else if (condition instanceof UnaryOpInstruction unaryOp) {
            code.append(apply(unaryOp.getOperand()));
            stack.update(-1);
            code.append("ifeq ").append(condInst.getLabel()).append(NL);
        }

        return code.toString();
    }

}