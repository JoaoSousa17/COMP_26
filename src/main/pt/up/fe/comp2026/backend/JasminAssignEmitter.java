package pt.up.fe.comp2026.backend;

import org.specs.comp.ollir.*;
import org.specs.comp.ollir.inst.*;
import org.specs.comp.ollir.type.ArrayType;
import org.specs.comp.ollir.type.BuiltinKind;
import org.specs.comp.ollir.type.BuiltinType;
import org.specs.comp.ollir.type.ClassType;
import org.specs.comp.ollir.type.Type;

import java.util.Optional;

import static org.specs.comp.ollir.OperationType.ADD;
import static org.specs.comp.ollir.OperationType.SUB;

class JasminAssignEmitter {

    private static final String NL = "\n";
    private final JasminGenerator gen;

    JasminAssignEmitter(JasminGenerator gen) {
        this.gen = gen;
    }

    String generate(AssignInstruction assign) {
        // iinc optimisation
        var iincCode = tryGenerateIinc(assign);
        if (iincCode.isPresent()) return iincCode.get();

        var dest = assign.getDest();

        // Array store: arr[idx] = value
        if (dest instanceof ArrayOperand arrayDest) {
            return generateArrayStore(arrayDest, assign.getRhs());
        }

        var operand = (Operand) dest;
        var reg = gen.currentMethod.getVarTable().get(operand.getName());
        var rhs = assign.getRhs();
        var code = new StringBuilder();

        // new array: new(array, size)
        if (rhs instanceof NewInstruction newInst
                && "array".equals(((Operand) newInst.getCaller()).getName())) {
            return generateNewArray(newInst, reg);
        }

        // new object: deferred store until invokespecial <init>
        if (rhs instanceof NewInstruction newInst) {
            var className = gen.types.resolveClassName(((Operand) newInst.getCaller()).getName());
            gen.stack.update(+1);
            code.append("new ").append(className).append(NL);
            gen.stack.update(+1);
            code.append("dup").append(NL);
            gen.pendingNewStore = gen.types.getStore(reg) + NL;
            return code.toString();
        }

        // array load: dest = arr[idx]
        if (rhs instanceof SingleOpInstruction singleOp
                && singleOp.getSingleOperand() instanceof ArrayOperand arrOp) {
            return generateArrayLoad(arrOp, reg);
        }

        // arraylength instruction (dedicated OLLIR instruction type)
        if (rhs instanceof ArrayLengthInstruction arrLen) {
            return generateArrayLengthInst(arrLen, reg);
        }

        // Calls as RHS: use forAssign=true (no spurious pop)
        if (rhs instanceof InvokeVirtualInstruction ivInvoke) {
            code.append(gen.callEmitter.invokeVirtual(ivInvoke, true));
        } else if (rhs instanceof InvokeStaticInstruction isInvoke) {
            code.append(gen.callEmitter.invokeStatic(isInvoke, true));
        } else {
            code.append(gen.apply(rhs));
        }

        gen.stack.update(-1);
        code.append(gen.types.getStore(reg)).append(NL);
        return code.toString();
    }

    // ── Array helpers ─────────────────────────────────────────────────────────

    private String generateNewArray(NewInstruction newInst, Descriptor reg) {
        var code = new StringBuilder();
        var args = newInst.getArguments();
        if (args.isEmpty()) {
            code.append("iconst_0").append(NL);
            gen.stack.update(+1);
        } else {
            code.append(gen.apply(args.get(0)));  // push size (+1)
        }

        Type arrType = newInst.getReturnType();
        Type elemType = getElementType(arrType);

        if (isPrimitiveInt(elemType)) {
            code.append("newarray int").append(NL);
        } else if (isPrimitiveBool(elemType)) {
            code.append("newarray boolean").append(NL);
        } else {
            String elemClass = resolveElementClassName(elemType);
            code.append("anewarray ").append(elemClass).append(NL);
        }
        // newarray: pops size, pushes ref → net 0; then store → -1
        gen.stack.update(-1);
        code.append(gen.types.getStore(reg)).append(NL);
        return code.toString();
    }

    private String generateArrayLoad(ArrayOperand arrOp, Descriptor reg) {
        var code = new StringBuilder();
        var arrDesc = gen.currentMethod.getVarTable().get(arrOp.getName());
        code.append(gen.types.getLoad(arrDesc)).append(NL);
        gen.stack.update(+1);
        var indexOp = arrOp.getIndexOperands().get(0);
        code.append(gen.apply(indexOp));  // +1
        // iaload/aaload: pops ref+idx, pushes element → net -1 from +2
        code.append(arrayLoadInst(arrOp.getType())).append(NL);
        gen.stack.update(-1);  // net of load instruction: -2+1 = -1
        gen.stack.update(-1);  // store
        code.append(gen.types.getStore(reg)).append(NL);
        return code.toString();
    }

    private String generateArrayStore(ArrayOperand arrayDest, Instruction rhsInst) {
        var code = new StringBuilder();
        var arrDesc = gen.currentMethod.getVarTable().get(arrayDest.getName());
        code.append(gen.types.getLoad(arrDesc)).append(NL);
        gen.stack.update(+1);
        var indexOp = arrayDest.getIndexOperands().get(0);
        code.append(gen.apply(indexOp));  // +1
        // load value
        if (rhsInst instanceof SingleOpInstruction singleOp) {
            code.append(gen.apply(singleOp.getSingleOperand()));
        } else {
            code.append(gen.apply(rhsInst));
        }
        // +1 for value; iastore/aastore: pops ref+idx+value → -3
        code.append(arrayStoreInst(arrayDest.getType())).append(NL);
        gen.stack.update(-3);
        return code.toString();
    }

    /** ArrayLengthInstruction: arraylength JVM instruction */
    private String generateArrayLengthInst(ArrayLengthInstruction arrLen, Descriptor reg) {
        var code = new StringBuilder();
        code.append(gen.apply(arrLen.getArray()));  // load array ref (+1)
        code.append("arraylength").append(NL);       // pops ref, pushes length (net 0)
        gen.stack.update(-1);  // store
        code.append(gen.types.getStore(reg)).append(NL);
        return code.toString();
    }

    // ── Type utilities ────────────────────────────────────────────────────────

    private String arrayLoadInst(Type type) {
        Type elem = getElementType(type);
        if (isPrimitiveInt(elem) || isPrimitiveBool(elem)) return "iaload";
        return "aaload";
    }

    private String arrayStoreInst(Type type) {
        Type elem = getElementType(type);
        if (isPrimitiveInt(elem) || isPrimitiveBool(elem)) return "iastore";
        return "aastore";
    }

    private Type getElementType(Type type) {
        if (type instanceof ArrayType at) return at.getElementType();
        return type;
    }

    private boolean isPrimitiveInt(Type t) {
        return t instanceof BuiltinType bt && bt.getKind() == BuiltinKind.INT32;
    }

    private boolean isPrimitiveBool(Type t) {
        return t instanceof BuiltinType bt && bt.getKind() == BuiltinKind.BOOLEAN;
    }

    private String resolveElementClassName(Type elemType) {
        if (elemType instanceof ClassType ct) return gen.types.resolveClassName(ct.getName());
        return gen.types.resolveClassName(elemType.toString());
    }

    // ── iinc optimisation ─────────────────────────────────────────────────────

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
            } else {
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
