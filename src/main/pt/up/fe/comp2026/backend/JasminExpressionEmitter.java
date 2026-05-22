package pt.up.fe.comp2026.backend;

import org.specs.comp.ollir.*;
import org.specs.comp.ollir.inst.*;
import pt.up.fe.specs.util.exceptions.NotImplementedException;

import static org.specs.comp.ollir.OperationType.*;

/** Generates code for expressions: operand loads, literals, arithmetic, comparisons, and logical NOT. */
class JasminExpressionEmitter {

    private static final String NL = "\n";
    private final JasminGenerator gen;

    JasminExpressionEmitter(JasminGenerator gen) {
        this.gen = gen;
    }

    String singleOp(SingleOpInstruction singleOp) {
        return gen.apply(singleOp.getSingleOperand());
    }

    String literal(LiteralElement literal) {
        gen.stack.update(+1);
        var literalStr = literal.getLiteral();

        try {
            int value = Integer.parseInt(literalStr);
            if (value == -1) return "iconst_m1" + NL;
            if (value >= 0 && value <= 5) return "iconst_" + value + NL;
            if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) return "bipush " + value + NL;
            if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) return "sipush " + value + NL;
        } catch (NumberFormatException ignored) {}

        return "ldc " + literalStr + NL;
    }

    String operand(Operand operand) {
        var reg = gen.currentMethod.getVarTable().get(operand.getName());
        gen.stack.update(+1);
        return gen.types.getLoad(reg) + NL;
    }

    String binaryOp(BinaryOpInstruction binaryOp) {
        var opType = binaryOp.getOperation().getOpType();

        if (gen.types.isComparisonOp(opType)) {
            return comparison(binaryOp);
        }

        var code = new StringBuilder();
        code.append(gen.apply(binaryOp.getLeftOperand()));
        code.append(gen.apply(binaryOp.getRightOperand()));
        gen.stack.update(-1);

        var typePrefix = gen.types.getTypePrefix(binaryOp.getOperation().getTypeInfo());

        var op = switch (opType) {
            case ADD -> "add";
            case SUB -> "sub";
            case MUL -> "mul";
            case DIV -> "div";
            case REM -> "rem";
            case SHL -> "shl";
            case SHR -> "shr";
            case SHRR -> "ushr";
            case XOR -> "xor";
            case BITWISE_AND -> "and";
            case BITWISE_OR -> "or";
            default -> throw new NotImplementedException(opType);
        };

        code.append(typePrefix).append(op).append(NL);
        return code.toString();
    }

    /**
     * Emits operand loads and a conditional branch to {@code trueLabel}.
     * Optimizes to single-operand branch instructions when one side is the literal 0.
     * Package-private so JasminGenerator can call it from generateOpCond.
     */
    String emitComparisonBranch(BinaryOpInstruction binOp, String trueLabel) {
        var left = binOp.getLeftOperand();
        var right = binOp.getRightOperand();
        var opType = binOp.getOperation().getOpType();
        var sb = new StringBuilder();

        if (gen.types.isZeroLiteral(right)) {
            sb.append(gen.apply(left));
            gen.stack.update(-1);
            sb.append(gen.types.zeroCompareBranch(opType, false)).append(" ").append(trueLabel).append(NL);
        } else if (gen.types.isZeroLiteral(left)) {
            sb.append(gen.apply(right));
            gen.stack.update(-1);
            sb.append(gen.types.zeroCompareBranch(opType, true)).append(" ").append(trueLabel).append(NL);
        } else {
            sb.append(gen.apply(left));
            sb.append(gen.apply(right));
            gen.stack.update(-2);
            sb.append(gen.types.twoValueCompareBranch(opType)).append(" ").append(trueLabel).append(NL);
        }
        return sb.toString();
    }

    private String comparison(BinaryOpInstruction binaryOp) {
        var thenLabel = gen.utils.nextLabel("cmpTrue_");
        var endLabel = gen.utils.nextLabel("cmpEnd_");

        var code = new StringBuilder();
        code.append(emitComparisonBranch(binaryOp, thenLabel));
        gen.stack.update(+1);

        code.append("iconst_0").append(NL);
        code.append("goto ").append(endLabel).append(NL);
        code.append(thenLabel).append(":").append(NL);
        code.append("iconst_1").append(NL);
        code.append(endLabel).append(":").append(NL);

        return code.toString();
    }

    String unaryOp(UnaryOpInstruction unaryOp) {
        var opType = unaryOp.getOperation().getOpType();

        if (opType != LOGICAL_NOT) throw new NotImplementedException(opType);

        var code = new StringBuilder();
        code.append(gen.apply(unaryOp.getOperand()));
        code.append("iconst_1").append(NL);
        gen.stack.update(+1);
        code.append("ixor").append(NL);
        gen.stack.update(-1);
        return code.toString();
    }
}