package pt.up.fe.comp2026.backend;

import org.specs.comp.ollir.*;
import org.specs.comp.ollir.inst.*;
import org.specs.comp.ollir.type.BuiltinKind;
import org.specs.comp.ollir.type.BuiltinType;
import org.specs.comp.ollir.type.ClassType;
import org.specs.comp.ollir.type.Type;

import java.util.stream.Collectors;

/**
 * Handles method invocations (invokestatic, invokevirtual, invokespecial)
 * and class field access (getfield, putfield).
 */
class JasminCallEmitter {

    private static final String NL = "\n";
    private final JasminGenerator gen;

    JasminCallEmitter(JasminGenerator gen) {
        this.gen = gen;
    }

    String getField(GetFieldInstruction getField) {
        var code = new StringBuilder();
        code.append(gen.apply(getField.getObject()));

        var ownerClass = gen.ollirResult.getOllirClass().getClassFullyQualifiedName().replace('.', '/');
        var fieldName = getField.getField().getName();
        var fieldDescriptor = gen.types.getTypeDescriptor(getField.getFieldType());
        code.append("getfield ").append(ownerClass).append("/").append(fieldName).append(" ").append(fieldDescriptor).append(NL);

        return code.toString();
    }

    String putField(PutFieldInstruction putField) {
        var code = new StringBuilder();

        code.append(gen.apply(putField.getObject()));
        code.append(gen.apply(putField.getValue()));
        gen.stack.update(-2);

        var ownerClass = gen.ollirResult.getOllirClass().getClassFullyQualifiedName().replace('.', '/');
        var fieldName = putField.getField().getName();
        var fieldDescriptor = gen.types.getTypeDescriptor(putField.getField().getType());

        code.append("putfield ").append(ownerClass).append("/").append(fieldName).append(" ").append(fieldDescriptor).append(NL);

        return code.toString();
    }

    /**
     * Generates invokestatic. When {@code forAssign} is true, the return value is left on
     * the stack for the caller (AssignEmitter) to store — the isolated pop is suppressed.
     */
    String invokeStatic(InvokeStaticInstruction invoke, boolean forAssign) {
        var code = new StringBuilder();

        for (var arg : invoke.getArguments()) code.append(gen.apply(arg));

        var className = gen.types.resolveClassName(((Operand) invoke.getCaller()).getName());
        gen.stack.update(-invoke.getArguments().size());
        code.append("invokestatic ").append(buildMethodRef(className, invoke)).append(NL);

        if (!isVoidType(invoke.getReturnType())) {
            if (!forAssign && invoke.isIsolated()) {
                // Standalone call whose return value is discarded.
                code.append("pop").append(NL);
                gen.stack.update(-1);
            } else {
                gen.stack.update(+1);
            }
        }

        return code.toString();
    }

    /** Convenience overload for standalone (non-assign) context. */
    String invokeStatic(InvokeStaticInstruction invoke) {
        return invokeStatic(invoke, false);
    }

    /**
     * Generates invokevirtual. When {@code forAssign} is true, the return value is left on
     * the stack for the caller (AssignEmitter) to store — the isolated pop is suppressed.
     */
    String invokeVirtual(InvokeVirtualInstruction invoke, boolean forAssign) {
        var code = new StringBuilder();

        code.append(gen.apply(invoke.getCaller()));
        for (var arg : invoke.getArguments()) code.append(gen.apply(arg));

        var className = gen.types.resolveClassName(((ClassType) invoke.getCaller().getType()).getName());
        gen.stack.update(-(1 + invoke.getArguments().size()));
        code.append("invokevirtual ").append(buildMethodRef(className, invoke)).append(NL);

        if (!isVoidType(invoke.getReturnType())) {
            if (!forAssign && invoke.isIsolated()) {
                // Standalone call whose return value is discarded.
                code.append("pop").append(NL);
                gen.stack.update(-1);
            } else {
                gen.stack.update(+1);
            }
        }

        return code.toString();
    }

    /** Convenience overload for standalone (non-assign) context. */
    String invokeVirtual(InvokeVirtualInstruction invoke) {
        return invokeVirtual(invoke, false);
    }

    String invokeSpecial(InvokeSpecialInstruction invoke) {
        var code = new StringBuilder();

        // When a 'new' assignment left its dup on the stack, that uninitialized ref
        // is already the invokespecial target — skip loading the caller operand.
        if (gen.pendingNewStore == null) {
            code.append(gen.apply(invoke.getCaller()));
        }

        for (var arg : invoke.getArguments()) code.append(gen.apply(arg));

        String className;
        if (invoke.getSuperClass().isPresent()) {
            className = gen.types.resolveClassName(invoke.getSuperClass().get());
        } else {
            className = gen.types.resolveClassName(((ClassType) invoke.getCaller().getType()).getName());
        }

        gen.stack.update(-(1 + invoke.getArguments().size()));
        code.append("invokespecial ").append(buildMethodRef(className, invoke)).append(NL);

        // Now the object is initialized — emit the deferred astore.
        if (gen.pendingNewStore != null) {
            gen.stack.update(-1);
            code.append(gen.pendingNewStore);
            gen.pendingNewStore = null;
        }

        return code.toString();
    }

    private String buildMethodRef(String classname, CallInstruction invoke) {
        var methodName = getMethodName(invoke);
        var ret = gen.types.getTypeDescriptor(invoke.getReturnType());

        // Try to resolve parameter descriptors from the actual JVM method signature via reflection.
        // This is necessary when an argument's OLLIR type is a subtype of the declared parameter type
        // (e.g., passing 'this' of type Foo where the method expects Auxi or Object).
        // Using the OLLIR argument type would produce a wrong descriptor and a NoSuchMethodError.
        String params = resolveParamDescriptors(classname, methodName, invoke);

        return classname + "/" + methodName + "(" + params + ")" + ret;
    }

    /**
     * Resolves parameter type descriptors for the given method, preferring the actual JVM
     * method signature over OLLIR argument types. Falls back to OLLIR types if reflection fails.
     */
    private String resolveParamDescriptors(String classpath, String methodName, CallInstruction invoke) {
        var args = invoke.getArguments();
        if (args.isEmpty()) return "";

        // Convert JVM path format to class name for Class.forName
        String fqn = classpath.replace('/', '.');
        try {
            Class<?> clazz = Class.forName(fqn);
            int argCount = args.size();

            // Find a method with matching name and arg count
            var candidates = java.util.Arrays.stream(clazz.getMethods())
                    .filter(m -> m.getName().equals(methodName) && m.getParameterCount() == argCount)
                    .toList();

            if (candidates.size() == 1) {
                // Unambiguous match — use its parameter descriptors
                return java.util.Arrays.stream(candidates.get(0).getParameterTypes())
                        .map(this::javaClassToDescriptor)
                        .collect(Collectors.joining());
            }
            // Multiple overloads or no match: fall through to OLLIR types
        } catch (ClassNotFoundException ignored) {
            // Class not on classpath (e.g. user-defined class) — use OLLIR types
        }

        // Default: use OLLIR argument types
        return args.stream()
                .map(arg -> gen.types.getTypeDescriptor(arg.getType()))
                .collect(Collectors.joining());
    }

    /** Converts a Java reflection Class to a JVM type descriptor string. */
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
