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

    String invokeStatic(InvokeStaticInstruction invoke) {
        var code = new StringBuilder();

        for (var arg : invoke.getArguments()) code.append(gen.apply(arg));

        var className = gen.types.resolveClassName(((Operand) invoke.getCaller()).getName());
        gen.stack.update(-invoke.getArguments().size());
        code.append("invokestatic ").append(buildMethodRef(className, invoke)).append(NL);

        if (!isVoidType(invoke.getReturnType())) {
            if (invoke.isIsolated()) {
                // Return value has no consumer — pop it to keep stack balanced.
                code.append("pop").append(NL);
                gen.stack.update(-1);
            } else {
                gen.stack.update(+1);
            }
        }

        return code.toString();
    }

    String invokeVirtual(InvokeVirtualInstruction invoke) {
        var code = new StringBuilder();

        code.append(gen.apply(invoke.getCaller()));
        for (var arg : invoke.getArguments()) code.append(gen.apply(arg));

        var className = gen.types.resolveClassName(((ClassType) invoke.getCaller().getType()).getName());
        gen.stack.update(-(1 + invoke.getArguments().size()));
        code.append("invokevirtual ").append(buildMethodRef(className, invoke)).append(NL);

        if (!isVoidType(invoke.getReturnType())) {
            if (invoke.isIsolated()) {
                code.append("pop").append(NL);
                gen.stack.update(-1);
            } else {
                gen.stack.update(+1);
            }
        }

        return code.toString();
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
        var params = invoke.getArguments().stream()
                .map(arg -> gen.types.getTypeDescriptor(arg.getType()))
                .collect(Collectors.joining());
        var ret = gen.types.getTypeDescriptor(invoke.getReturnType());
        return classname + "/" + methodName + "(" + params + ")" + ret;
    }

    private String getMethodName(CallInstruction invoke) {
        return ((LiteralElement) invoke.getMethodName()).getLiteral().replace("\"", "");
    }

    private boolean isVoidType(Type type) {
        return type instanceof BuiltinType bt && bt.getKind() == BuiltinKind.VOID;
    }
}