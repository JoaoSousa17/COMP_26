package pt.up.fe.comp2026.analysis.calls;

import pt.up.fe.comp.jmm.analysis.table.MethodSymbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.type.JmmType;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmArrayType;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmClassType;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmPrimitiveType;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2026.analysis.AnalysisVisitorWithTable;
import pt.up.fe.comp2026.jmm.ast.JmmKind;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

/**
 * Analysis pass that validates method calls in Java-- programs.
 *
 * Handles three call forms:
 *   - {@code expr.method(args)} — explicit receiver (MethodCallExpr)
 *   - {@code method(args)}      — implicit this receiver (ImplicitThisCallExpr)
 *
 * For each call, verifies:
 *   1. The receiver type is a class type.
 *   2. The receiver class is imported (or is the current class).
 *   3. A method with the given name exists.
 *   4. The argument count matches.
 *   5. The argument types are compatible.
 *
 * For imported classes not resolved via the symbol table, validation is
 * attempted through Java reflection. If the class is not on the classpath,
 * the check is skipped conservatively.
 */
public class CallValidationPass extends AnalysisVisitorWithTable {

    /** The method currently being visited, used for type resolution of local variables. */
    private MethodSymbol currentMethod;

    public CallValidationPass(SymbolTable table) {
        super(table);
        buildVisitor();
    }

    @Override
    protected void buildVisitor() {
        addVisit(JmmKind.METHOD_DECL,             this::visitMethodDecl);
        addVisit(JmmKind.METHOD_CALL_EXPR,        this::visitMethodCallExpr);
        addVisit(JmmKind.IMPLICIT_THIS_CALL_EXPR, this::visitImplicitThisCallExpr);
    }

    /** Tracks the current method being analysed for scoped type resolution. */
    private Void visitMethodDecl(JmmNode method, SymbolTable table) {
        var sig = types.getMethodDeclSignature(method);
        currentMethod = this.table.getMethod(sig).orElse(null);
        return null;
    }

    /**
     * Validates an explicit method call {@code expr.method(args)}.
     * Resolves the receiver type and delegates to {@link #validateCall}.
     */
    private Void visitMethodCallExpr(JmmNode callExpr, SymbolTable table) {
        String methodName = callExpr.get("name");
        JmmNode receiverExpr = callExpr.getChild(0);
        List<JmmNode> argNodes = callExpr.getChildren().subList(1, callExpr.getNumChildren());

        JmmType receiverType = types.getExprType(receiverExpr, currentMethod);
        if (receiverType == null) return null;

        if (!(receiverType instanceof JmmClassType classType)) {
            addReport(newError(callExpr,
                    "Cannot call method '" + methodName + "' on non-class type '" + receiverType + "'"));
            return null;
        }

        String receiverFqn = classType.name();
        validateCall(callExpr, receiverFqn, methodName, argNodes);
        return null;
    }

    /**
     * Validates an implicit-this method call {@code method(args)}.
     * Uses the current class as the receiver and delegates to {@link #validateCall}.
     */
    private Void visitImplicitThisCallExpr(JmmNode callExpr, SymbolTable table) {
        String methodName = callExpr.get("name");
        List<JmmNode> argNodes = callExpr.getChildren();
        validateCall(callExpr, this.table.getFullyQualifiedName(), methodName, argNodes);
        return null;
    }

    /**
     * Core call validation logic.
     * Checks import visibility, then dispatches to either reflection-based
     * validation (external classes) or symbol-table-based validation (current class).
     *
     * @param errorNode   AST node to attach error reports to.
     * @param receiverFqn Fully qualified name of the receiver class.
     * @param methodName  Name of the method being called.
     * @param argNodes    Argument expression nodes.
     */
    private void validateCall(JmmNode errorNode, String receiverFqn,
                              String methodName, List<JmmNode> argNodes) {
        String simpleName = simpleNameOf(receiverFqn);
        boolean isCurrentClass = typeNamesMatch(receiverFqn, this.table.getFullyQualifiedName());

        // Ensure the receiver class is known (imported or current)
        if (!isCurrentClass) {
            boolean imported = this.table.getImportNames().contains(simpleName)
                    || this.table.isImplicitImport(simpleName);
            if (!imported) {
                addReport(newError(errorNode, "Class '" + simpleName + "' is not imported"));
                return;
            }
        }

        // For imported classes not in the symbol table, fall back to reflection
        if (!isCurrentClass) {
            String fqn = this.table.getImportedFullyQualifiedName(simpleName).orElse(receiverFqn);
            validateViaReflection(errorNode, fqn, simpleName, methodName, argNodes);
            return;
        }

        // Current class — validate using the symbol table
        Optional<MethodSymbol> found = findMethodByName(receiverFqn, methodName);
        if (found.isEmpty()) {
            Optional<SymbolTable> stOpt = resolveSymbolTable(receiverFqn);
            if (stOpt.isPresent()) {
                addReport(newError(errorNode,
                        "Cannot find method '" + methodName + "' in class '" + simpleName + "'"));
            }
            return;
        }

        Optional<MethodSymbol> exact = findMethodByNameAndArgCount(receiverFqn, methodName, argNodes.size());
        if (exact.isEmpty()) {
            int expected = found.get().parameters().size();
            addReport(newError(errorNode,
                    "Method '" + methodName + "' expects " + expected
                            + " argument(s), but got " + argNodes.size()));
            return;
        }
        checkArguments(errorNode, exact.get(), argNodes);
    }

    /**
     * Validates a call against a class loaded via Java reflection.
     * Reports an error if no overload matches by name, argument count, or argument types.
     * Skipped conservatively if the class is not on the classpath.
     */
    private void validateViaReflection(JmmNode errorNode, String fqn, String simpleName,
                                       String methodName, List<JmmNode> argNodes) {
        try {
            Class<?> clazz = Class.forName(fqn);
            var allMethods = Arrays.stream(clazz.getMethods())
                    .filter(m -> m.getName().equals(methodName))
                    .toList();

            if (allMethods.isEmpty()) {
                addReport(newError(errorNode,
                        "Cannot find method '" + methodName + "' in class '" + simpleName + "'"));
                return;
            }

            var countMatch = allMethods.stream()
                    .filter(m -> m.getParameterCount() == argNodes.size())
                    .toList();

            if (countMatch.isEmpty()) {
                int expected = allMethods.get(0).getParameterCount();
                addReport(newError(errorNode,
                        "Method '" + methodName + "' expects " + expected
                                + " argument(s), but got " + argNodes.size()));
                return;
            }

            // Accept if any overload is fully type-compatible
            for (Method m : countMatch) {
                if (argsCompatibleWithReflection(argNodes, m.getParameterTypes())) return;
            }

            // No overload matched — report type mismatch on the first candidate
            Method m = countMatch.get(0);
            for (int i = 0; i < argNodes.size(); i++) {
                JmmType actual = types.getExprType(argNodes.get(i), currentMethod);
                if (actual == null) continue;
                if (!jmmTypeMatchesJavaClass(actual, m.getParameterTypes()[i])) {
                    addReport(newError(argNodes.get(i),
                            "Argument " + (i + 1) + " of '" + methodName
                                    + "': expected '" + m.getParameterTypes()[i].getSimpleName()
                                    + "', got '" + actual + "'"));
                }
            }
        } catch (ClassNotFoundException e) {
            // Class not on classpath — skip conservatively
        }
    }

    /**
     * Returns true if all argument types are compatible with the given Java reflection parameter types.
     * Returns true conservatively if a type cannot be resolved.
     */
    private boolean argsCompatibleWithReflection(List<JmmNode> args, Class<?>[] params) {
        for (int i = 0; i < args.size(); i++) {
            JmmType t = types.getExprType(args.get(i), currentMethod);
            if (t == null) return true;
            if (!jmmTypeMatchesJavaClass(t, params[i])) return false;
        }
        return true;
    }

    /**
     * Checks whether a JmmType is compatible with a Java reflection {@link Class}.
     * Handles primitives, arrays, and class types.
     */
    private boolean jmmTypeMatchesJavaClass(JmmType jmmType, Class<?> javaClass) {
        if (jmmType instanceof JmmPrimitiveType prim) {
            String primName = prim.name().toLowerCase();
            return switch (primName) {
                case "int"     -> javaClass == int.class || javaClass == long.class
                        || javaClass == Integer.class || javaClass == Long.class;
                case "boolean" -> javaClass == boolean.class || javaClass == Boolean.class;
                case "void"    -> javaClass == void.class;
                default        -> false;
            };
        }
        if (jmmType instanceof JmmArrayType) {
            return javaClass.isArray();
        }
        if (jmmType instanceof JmmClassType ct) {
            String simple = simpleNameOf(ct.name());
            return javaClass.getSimpleName().equals(simple) || javaClass.getName().equals(ct.name());
        }
        return false;
    }

    /**
     * Searches for a method by name in the given class and its superclass chain.
     * Returns the first match found, or empty if none exists.
     */
    private Optional<MethodSymbol> findMethodByName(String receiverFqn, String methodName) {
        String currentFqn = receiverFqn;
        var visited = new HashSet<String>();
        while (currentFqn != null && !visited.contains(currentFqn)) {
            visited.add(currentFqn);
            Optional<SymbolTable> stOpt = resolveSymbolTable(currentFqn);
            if (stOpt.isEmpty()) return Optional.empty();
            SymbolTable st = stOpt.get();
            List<MethodSymbol> byName = st.getMethods(methodName);
            if (!byName.isEmpty()) return Optional.of(byName.get(0));
            currentFqn = st.getSuperFullyQualifiedName();
        }
        return Optional.empty();
    }

    /**
     * Searches for a method by name and exact argument count in the class hierarchy.
     * Returns the first overload that matches both name and arity.
     */
    private Optional<MethodSymbol> findMethodByNameAndArgCount(String receiverFqn,
                                                               String methodName, int argCount) {
        String currentFqn = receiverFqn;
        var visited = new HashSet<String>();
        while (currentFqn != null && !visited.contains(currentFqn)) {
            visited.add(currentFqn);
            Optional<SymbolTable> stOpt = resolveSymbolTable(currentFqn);
            if (stOpt.isEmpty()) return Optional.empty();
            SymbolTable st = stOpt.get();
            Optional<MethodSymbol> exact = st.getMethods(methodName).stream()
                    .filter(m -> m.parameters().size() == argCount)
                    .findFirst();
            if (exact.isPresent()) return exact;
            currentFqn = st.getSuperFullyQualifiedName();
        }
        return Optional.empty();
    }

    /**
     * Verifies that each argument expression is type-compatible with the corresponding
     * formal parameter. Reports an error for each mismatch.
     */
    private void checkArguments(JmmNode callExpr, MethodSymbol method, List<JmmNode> argNodes) {
        var params = method.parameters();
        if (params.size() != argNodes.size()) return;
        for (int i = 0; i < params.size(); i++) {
            JmmType expected = params.get(i).type();
            JmmType actual = types.getExprType(argNodes.get(i), currentMethod);
            if (actual == null) continue;
            if (!types.isTypeCompatible(expected, actual)) {
                addReport(newError(argNodes.get(i),
                        "Argument " + (i + 1) + " of call to '" + method.name()
                                + "': expected '" + expected + "', got '" + actual + "'"));
            }
        }
    }

    /**
     * Resolves the {@link SymbolTable} for a given class name or FQN.
     * Tries the current class first, then imported symbol tables, then implicit imports.
     */
    private Optional<SymbolTable> resolveSymbolTable(String nameOrFqn) {
        if (typeNamesMatch(nameOrFqn, this.table.getFullyQualifiedName())) {
            return Optional.of(this.table);
        }
        Optional<SymbolTable> st = this.table.getImportedSymbolTable(nameOrFqn);
        if (st.isPresent()) return st;
        String simple = simpleNameOf(nameOrFqn);
        st = this.table.getImplicitImport(simple);
        if (st.isPresent()) return st;
        var fqnOpt = this.table.getImportedFullyQualifiedName(simple);
        if (fqnOpt.isPresent()) return this.table.getImportedSymbolTable(fqnOpt.get());
        return Optional.empty();
    }

    /** Returns true if two class names refer to the same type (by FQN or simple name). */
    private boolean typeNamesMatch(String a, String b) {
        if (a == null || b == null) return false;
        if (a.equals(b)) return true;
        return simpleNameOf(a).equals(simpleNameOf(b));
    }

    /** Extracts the simple class name from a fully qualified name (e.g. {@code "a.b.Foo"} → {@code "Foo"}). */
    private String simpleNameOf(String fqn) {
        if (fqn == null) return "";
        int dot = fqn.lastIndexOf('.');
        return dot >= 0 ? fqn.substring(dot + 1) : fqn;
    }
}
