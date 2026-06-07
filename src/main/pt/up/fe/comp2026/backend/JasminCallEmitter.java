package pt.up.fe.comp2026.backend;

import org.specs.comp.ollir.*;
import org.specs.comp.ollir.inst.*;
import org.specs.comp.ollir.type.BuiltinKind;
import org.specs.comp.ollir.type.BuiltinType;
import org.specs.comp.ollir.type.ClassType;
import org.specs.comp.ollir.type.Type;

import java.util.stream.Collectors;

class JasminCallEmitter {

    private static final String NL = "\n";
    private final JasminGenerator gen;

    JasminCallEmitter(JasminGenerator gen) {
        this.gen = gen;
    }

    String getField(GetFieldInstruction getField) {
        var code = new StringBuilder();
        code.append(gen.apply(getField.getObject())); // loads object ref: +1

        var ownerClass = gen.ollirResult.getOllirClass().getClassFullyQualifiedName().replace('.', '/');
        var fieldName = getField.getField().getName();
        var fieldDescriptor = gen.types.getTypeDescriptor(getField.getFieldType());
        code.append("getfield ").append(ownerClass).append("/").append(fieldName)
                .append(" ").append(fieldDescriptor).append(NL);
        // getfield: pops ref (-1), pushes value (+1) → net 0 on top of the +1 from load
        // Total effect: stack +1 (object load already accounted for, getfield net 0)

        return code.toString();
    }

    String putField(PutFieldInstruction putField) {
        var code = new StringBuilder();

        code.append(gen.apply(putField.getObject())); // +1
        code.append(gen.apply(putField.getValue()));  // +1
        gen.stack.update(-2); // putfield consumes both

        var ownerClass = gen.ollirResult.getOllirClass().getClassFullyQualifiedName().replace('.', '/');
        var fieldName = putField.getField().getName();
        var fieldDescriptor = gen.types.getTypeDescriptor(putField.getField().getType());
        code.append("putfield ").append(ownerClass).append("/").append(fieldName)
                .append(" ").append(fieldDescriptor).append(NL);

        return code.toString();
    }

    String invokeStatic(InvokeStaticInstruction invoke, boolean forAssign) {
        var code = new StringBuilder();

        for (var arg : invoke.getArguments()) code.append(gen.apply(arg));

        var className = gen.types.resolveClassName(((Operand) invoke.getCaller()).getName());
        gen.stack.update(-invoke.getArguments().size()); // pops all args
        String ref = buildMethodRef(className, invoke);
        code.append("invokestatic ").append(ref).append(NL);

        boolean actuallyVoid = ref.endsWith(")V");
        if (!actuallyVoid) {
            gen.stack.update(+1); // return value pushed
            if (isVoidType(invoke.getReturnType()) || (!forAssign && invoke.isIsolated())) {
                code.append("pop").append(NL);
                gen.stack.update(-1);
            }
        }

        return code.toString();
    }

    String invokeStatic(InvokeStaticInstruction invoke) {
        return invokeStatic(invoke, false);
    }

    String invokeVirtual(InvokeVirtualInstruction invoke, boolean forAssign) {
        var code = new StringBuilder();

        code.append(gen.apply(invoke.getCaller()));
        for (var arg : invoke.getArguments()) code.append(gen.apply(arg));

        var className = gen.types.resolveClassName(((ClassType) invoke.getCaller().getType()).getName());
        gen.stack.update(-(1 + invoke.getArguments().size())); // pops receiver + args
        String ref = buildMethodRef(className, invoke);
        code.append("invokevirtual ").append(ref).append(NL);

        boolean actuallyVoid = ref.endsWith(")V");
        if (!actuallyVoid) {
            gen.stack.update(+1); // return value pushed
            if (isVoidType(invoke.getReturnType()) || (!forAssign && invoke.isIsolated())) {
                code.append("pop").append(NL);
                gen.stack.update(-1);
            }
        }

        return code.toString();
    }

    String invokeVirtual(InvokeVirtualInstruction invoke) {
        return invokeVirtual(invoke, false);
    }

    String invokeSpecial(InvokeSpecialInstruction invoke) {
        var code = new StringBuilder();

        if (gen.pendingNewStore == null) {
            code.append(gen.apply(invoke.getCaller())); // load receiver
        }

        for (var arg : invoke.getArguments()) code.append(gen.apply(arg));

        String className = invoke.getSuperClass().isPresent()
            ? gen.types.resolveClassName(invoke.getSuperClass().get())
            : gen.types.resolveClassName(((ClassType) invoke.getCaller().getType()).getName());

        gen.stack.update(-(1 + invoke.getArguments().size())); // pops receiver + args
        code.append("invokespecial ").append(buildMethodRef(className, invoke)).append(NL);
        // invokespecial always void (constructors) — no return value

        if (gen.pendingNewStore != null) {
            gen.stack.update(-1); // store the initialized object ref
            code.append(gen.pendingNewStore);
            gen.pendingNewStore = null;
        }

        return code.toString();
    }

    private String buildMethodRef(String classname, CallInstruction invoke) {
        var methodName = getMethodName(invoke);
        String params = resolveParamDescriptors(classname, methodName, invoke);
        String ret = resolveReturnDescriptor(classname, methodName, invoke);
        return classname + "/" + methodName + "(" + params + ")" + ret;
    }

    /**
     * Resolves the JVM return descriptor for a method, using reflection to override
     * the OLLIR return type when OLLIR says void but the real method returns a value.
     * This happens when a call result is discarded (expression statement).
     * Walks the class hierarchy (including the superclass from the OLLIR class unit)
     * when the receiver class is not on the classpath (e.g. the currently-compiled class).
     */
    private String resolveReturnDescriptor(String classname, String methodName, CallInstruction invoke) {
        String ollirRet = gen.types.getTypeDescriptor(invoke.getReturnType());
        if (!ollirRet.equals("V")) return ollirRet;

        String fqn = classname.replace('/', '.');
        int argCount = invoke.getArguments().size();
        String found = findReturnDescriptor(fqn, methodName, argCount);
        return found != null ? found : "V";
    }

    private String findReturnDescriptor(String fqn, String methodName, int argCount) {
        try {
            Class<?> clazz = Class.forName(fqn);
            var candidates = java.util.Arrays.stream(clazz.getMethods())
                    .filter(m -> m.getName().equals(methodName) && m.getParameterCount() == argCount)
                    .toList();
            if (candidates.size() == 1) {
                Class<?> retType = candidates.get(0).getReturnType();
                if (retType != void.class) return javaClassToDescriptor(retType);
            }
        } catch (ClassNotFoundException ignored) {
            // Class not on classpath — try superclass from the OLLIR class unit
            var ollirClass = gen.ollirResult.getOllirClass();
            if (fqn.equals(ollirClass.getClassFullyQualifiedName()) && ollirClass.getSuperClass() != null) {
                String superPath = gen.types.resolveClassName(ollirClass.getSuperClass());
                String superFqn = superPath.replace('/', '.');
                if (!superFqn.equals(fqn)) return findReturnDescriptor(superFqn, methodName, argCount);
            }
        }
        return null;
    }

    private String resolveParamDescriptors(String classpath, String methodName, CallInstruction invoke) {
        var args = invoke.getArguments();
        if (args.isEmpty()) return "";

        String fqn = classpath.replace('/', '.');
        try {
            Class<?> clazz = Class.forName(fqn);
            int argCount = args.size();

            var candidates = java.util.Arrays.stream(clazz.getMethods())
                    .filter(m -> m.getName().equals(methodName) && m.getParameterCount() == argCount)
                    .toList();

            if (candidates.size() == 1) {
                return java.util.Arrays.stream(candidates.get(0).getParameterTypes())
                        .map(this::javaClassToDescriptor)
                        .collect(Collectors.joining());
            }
        } catch (ClassNotFoundException ignored) {}

        return args.stream()
                .map(arg -> gen.types.getTypeDescriptor(arg.getType()))
                .collect(Collectors.joining());
    }

    private String javaClassToDescriptor(Class<?> clazz) {
        if (clazz == void.class)    return "V";
        if (clazz == int.class)     return "I";
        if (clazz == boolean.class) return "Z";
        if (clazz == long.class)    return "J";
        if (clazz == double.class)  return "D";
        if (clazz == float.class)   return "F";
        if (clazz == byte.class)    return "B";
        if (clazz == short.class)   return "S";
        if (clazz == char.class)    return "C";
        if (clazz.isArray()) return "[" + javaClassToDescriptor(clazz.getComponentType());
        return "L" + clazz.getName().replace('.', '/') + ";";
    }

    private String getMethodName(CallInstruction invoke) {
        return ((LiteralElement) invoke.getMethodName()).getLiteral().replace("\"", "");
    }

    private boolean isVoidType(Type type) {
        return type instanceof BuiltinType bt && bt.getKind() == BuiltinKind.VOID;
    }
}
