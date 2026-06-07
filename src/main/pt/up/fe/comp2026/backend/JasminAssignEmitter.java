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
        var iincCode = tryGenerateIinc(assign);
        if (iincCode.isPresent()) return iincCode.get();

        var dest = assign.getDest();

        if (dest instanceof ArrayOperand arrayDest) {
            return generateArrayStore(arrayDest, assign.getRhs());
        }

        var operand = (Operand) dest;
        var reg = gen.getDescriptor(operand.getName());
        var rhs = assign.getRhs();
        var code = new StringBuilder();

        if (rhs instanceof NewInstruction newInst
                && "array".equals(((Operand) newInst.getCaller()).getName())) {
            return generateNewArray(newInst, reg);
        }

        if (rhs instanceof NewInstruction newInst) {
            var className = gen.types.resolveClassName(((Operand) newInst.getCaller()).getName());
            gen.stack.update(+1);
            code.append("new ").append(className).append(NL);
            gen.stack.update(+1);
            code.append("dup").append(NL);
            gen.pendingNewStore = gen.types.getStore(reg) + NL;
            return code.toString();
        }

        if (rhs instanceof SingleOpInstruction singleOp
                && singleOp.getSingleOperand() instanceof ArrayOperand arrOp) {
            return generateArrayLoad(arrOp, reg);
        }

        if (rhs instanceof ArrayLengthInstruction arrLen) {
            return generateArrayLengthInst(arrLen, reg);
        }

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
        Type arrType = newInst.getReturnType();

        if (arrType instanceof ArrayType at && at.getNumDimensions() > 1) {
            for (Element arg : args) {
                code.append(gen.apply(arg));
            }
            String arrayDesc = gen.types.getTypeDescriptor(arrType);
            code.append("multianewarray ").append(arrayDesc).append(" ").append(args.size()).append(NL);
            gen.stack.update(-args.size() + 1);
        } else {
            if (args.isEmpty()) {
                code.append("iconst_0").append(NL);
                gen.stack.update(+1);
            } else {
                code.append(gen.apply(args.get(0)));
            }

            Type elemType = getElementType(arrType);

            if (isPrimitiveInt(elemType)) {
                code.append("newarray int").append(NL);
            } else if (isPrimitiveBool(elemType)) {
                code.append("newarray boolean").append(NL);
            } else {
                String elemClass = resolveElementClassName(elemType);
                code.append("anewarray ").append(elemClass).append(NL);
            }
        }
        // newarray/multianewarray pushes ref; then store: -1
        gen.stack.update(-1);
        code.append(gen.types.getStore(reg)).append(NL);
        return code.toString();
    }

    private String generateArrayLoad(ArrayOperand arrOp, Descriptor reg) {
        var code = new StringBuilder();
        var arrDesc = gen.getDescriptor(arrOp.getName());
        code.append(gen.types.getLoad(arrDesc)).append(NL);
        gen.stack.update(+1); // load array ref

        var indexOps = arrOp.getIndexOperands();

        // For multidimensional arrays, chain aaload for all but the last index
        for (int i = 0; i < indexOps.size() - 1; i++) {
            code.append(gen.apply(indexOps.get(i))); // push index (+1)
            code.append("aaload").append(NL);          // pops ref+idx, pushes sub-array ref: net -1
            gen.stack.update(-1);
        }

        // Final index load
        code.append(gen.apply(indexOps.get(indexOps.size() - 1))); // push final index (+1)
        code.append(arrayLoadInst(arrOp.getType())).append(NL);     // pops ref+idx, pushes value: net -1
        gen.stack.update(-1);

        // Store result
        gen.stack.update(-1);
        code.append(gen.types.getStore(reg)).append(NL);
        return code.toString();
    }

    private String generateArrayStore(ArrayOperand arrayDest, Instruction rhsInst) {
        var code = new StringBuilder();
        var arrDesc = gen.getDescriptor(arrayDest.getName());
        code.append(gen.types.getLoad(arrDesc)).append(NL);
        gen.stack.update(+1); // load array ref

        var indexOps = arrayDest.getIndexOperands();

        // For multidimensional arrays, chain aaload to reach the target sub-array
        for (int i = 0; i < indexOps.size() - 1; i++) {
            code.append(gen.apply(indexOps.get(i))); // push index (+1)
            code.append("aaload").append(NL);          // pops ref+idx, pushes sub-array ref: net -1
            gen.stack.update(-1);
        }

        // Push final index
        code.append(gen.apply(indexOps.get(indexOps.size() - 1))); // +1

        // Push value to store
        if (rhsInst instanceof SingleOpInstruction singleOp) {
            code.append(gen.apply(singleOp.getSingleOperand())); // +1
        } else {
            code.append(gen.apply(rhsInst)); // +1
        }

        // iastore/aastore: pops ref+idx+value → -3
        code.append(arrayStoreInst(arrayDest.getType())).append(NL);
        gen.stack.update(-3);
        return code.toString();
    }

    private String generateArrayLengthInst(ArrayLengthInstruction arrLen, Descriptor reg) {
        var code = new StringBuilder();
        code.append(gen.apply(arrLen.getOperands().get(0))); // load array ref (+1)
        code.append("arraylength").append(NL);                // pops ref, pushes length: net 0
        gen.stack.update(-1);                                 // store
        code.append(gen.types.getStore(reg)).append(NL);
        return code.toString();
    }

    // ── Type utilities ────────────────────────────────────────────────────────

    private String arrayLoadInst(Type type) {
        if (type instanceof BuiltinType bt) {
            if (bt.getKind() == BuiltinKind.INT32 || bt.getKind() == BuiltinKind.BOOLEAN) return "iaload";
        }
        return "aaload";
    }

    private String arrayStoreInst(Type type) {
        if (type instanceof BuiltinType bt) {
            if (bt.getKind() == BuiltinKind.INT32 || bt.getKind() == BuiltinKind.BOOLEAN) return "iastore";
        }
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

        // iinc only supports -128 to 127
        if (increment < -128 || increment > 127) return Optional.empty();

        var desc = gen.getDescriptor(varName);
        if (desc == null) return Optional.empty();

        return Optional.of("iinc " + desc.getVirtualReg() + " " + increment + NL);
    }

    private boolean isSameVar(Element elem, String varName) {
        if (!(elem instanceof Operand op)) return false;
        String opName = op.getName();
        // Normalize by stripping quotes for comparison
        String strippedVar = stripQuotes(varName);
        String strippedOp = stripQuotes(opName);
        return strippedOp.equals(strippedVar);
    }

    private String stripQuotes(String name) {
        if (name != null && name.startsWith("\"") && name.endsWith("\"") && name.length() >= 2) {
            return name.substring(1, name.length() - 1);
        }
        return name;
    }
}
