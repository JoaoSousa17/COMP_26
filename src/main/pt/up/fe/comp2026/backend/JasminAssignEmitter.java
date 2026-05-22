package pt.up.fe.comp2026.backend;

import org.specs.comp.ollir.*;
import org.specs.comp.ollir.inst.*;
import org.specs.comp.ollir.type.BuiltinKind;
import org.specs.comp.ollir.type.BuiltinType;

import java.util.Optional;

import static org.specs.comp.ollir.OperationType.ADD;
import static org.specs.comp.ollir.OperationType.SUB;

/** Handles AssignInstruction generation, including the iinc optimization and new-object deferred store. */
class JasminAssignEmitter {

    private static final String NL = "\n";
    private final JasminGenerator gen;

    JasminAssignEmitter(JasminGenerator gen) {
        this.gen = gen;
    }

    String generate(AssignInstruction assign) {
        var iincCode = tryGenerateIinc(assign);
        if (iincCode.isPresent()) return iincCode.get();

        var code = new StringBuilder();
        var operand = (Operand) assign.getDest();
        var reg = gen.currentMethod.getVarTable().get(operand.getName());

        // new requires new+dup+invokespecial+astore — the JVM verifier does not
        // allow astore on an uninitialized reference. Defer the store until the
        // following invokespecial <init> has run (see JasminCallEmitter.invokeSpecial).
        if (assign.getRhs() instanceof NewInstruction newInst) {
            var className = gen.types.resolveClassName(((Operand) newInst.getCaller()).getName());
            gen.stack.update(+1);
            code.append("new ").append(className).append(NL);
            gen.stack.update(+1);
            code.append("dup").append(NL);
            gen.pendingNewStore = gen.types.getStore(reg) + NL;
            return code.toString();
        }

        code.append(gen.apply(assign.getRhs()));
        gen.stack.update(-1);
        code.append(gen.types.getStore(reg)).append(NL);
        return code.toString();
    }

    /**
     * Tries to replace "dest = dest ± const" with a single iinc instruction.
     * iinc avoids loading/storing the variable and works only on int locals.
     */
    private Optional<String> tryGenerateIinc(AssignInstruction assign) {
        var dest = assign.getDest();
        if (!(dest instanceof Operand destOp)) return Optional.empty();

        var destType = destOp.getType();
        if (!(destType instanceof BuiltinType bt) || bt.getKind() != BuiltinKind.INT32)
            return Optional.empty();

        var rhs = assign.getRhs();
        if (!(rhs instanceof BinaryOpInstruction binOp)) return Optional.empty();

        var opType = binOp.getOperation().getOpType();
        if (opType != ADD && opType != SUB) return Optional.empty();

        var left = binOp.getLeftOperand();
        var right = binOp.getRightOperand();
        String varName = destOp.getName();
        int increment;

        try {
            if (opType == ADD) {
                if (isSameVar(left, varName) && right instanceof LiteralElement litRight)
                    increment = Integer.parseInt(litRight.getLiteral());
                else if (isSameVar(right, varName) && left instanceof LiteralElement litLeft)
                    increment = Integer.parseInt(litLeft.getLiteral());
                else return Optional.empty();
            } else { // SUB: only i = i - const qualifies
                if (isSameVar(left, varName) && right instanceof LiteralElement litRight)
                    increment = -Integer.parseInt(litRight.getLiteral());
                else return Optional.empty();
            }
        } catch (NumberFormatException e) {
            return Optional.empty();
        }

        var desc = gen.currentMethod.getVarTable().get(varName);
        if (desc == null) return Optional.empty();

        return Optional.of("iinc " + desc.getVirtualReg() + " " + increment + NL);
    }

    private boolean isSameVar(Element elem, String varName) {
        return elem instanceof Operand op && op.getName().equals(varName);
    }
}