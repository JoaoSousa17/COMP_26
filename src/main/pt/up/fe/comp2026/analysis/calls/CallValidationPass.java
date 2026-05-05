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

public class CallValidationPass extends AnalysisVisitorWithTable {

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

    private Void visitMethodDecl(JmmNode method, SymbolTable table) {
        var sig = types.getMethodDeclSignature(method);
        currentMethod = this.table.getMethod(sig).orElse(null);
        return null;
    }

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

    private Void visitImplicitThisCallExpr(JmmNode callExpr, SymbolTable table) {
        String methodName = callExpr.get("name");
        List<JmmNode> argNodes = callExpr.getChildren();
        validateCall(callExpr, this.table.getFullyQualifiedName(), methodName, argNodes);
        return null;
    }

    private void validateCall(JmmNode errorNode, String receiverFqn,
                              String methodName, List<JmmNode> argNodes) {
        String simpleName = simpleNameOf(receiverFqn);
        boolean isCurrentClass = typeNamesMatch(receiverFqn, this.table.getFullyQualifiedName());

        if (!isCurrentClass) {
            boolean imported = this.table.getImportNames().contains(simpleName)
                    || this.table.isImplicitImport(simpleName);
            if (!imported) {
                addReport(newError(errorNode, "Class '" + simpleName + "' is not imported"));
                return;
            }
        }

        // Try to validate via reflection for imported classes
        if (!isCurrentClass) {
            String fqn = this.table.getImportedFullyQualifiedName(simpleName).orElse(receiverFqn);
            validateViaReflection(errorNode, fqn, simpleName, methodName, argNodes);
            return;
        }

        // Current class — use symbol table
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

            // Check arg types — accept if any overload matches
            for (Method m : countMatch) {
                if (argsCompatibleWithReflection(argNodes, m.getParameterTypes())) return;
            }

            // Report type mismatch for first candidate
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

    private boolean argsCompatibleWithReflection(List<JmmNode> args, Class<?>[] params) {
        for (int i = 0; i < args.size(); i++) {
            JmmType t = types.getExprType(args.get(i), currentMethod);
            if (t == null) return true;
            if (!jmmTypeMatchesJavaClass(t, params[i])) return false;
        }
        return true;
    }

    private boolean jmmTypeMatchesJavaClass(JmmType jmmType, Class<?> javaClass) {
        if (jmmType instanceof JmmPrimitiveType prim) {
            String primName = prim.name().toLowerCase(); // força lowercase
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
            if (javaClass == Object.class) return true;
            try {
                Class<?> actualClass = Class.forName(ct.name());
                if (javaClass.isAssignableFrom(actualClass)) return true;
            } catch (ClassNotFoundException ignored) {
                // Custom class not loadable — check subtype via our compiler's type system
                String paramSimple = javaClass.getSimpleName();
                String paramFqn = this.table.getImportedFullyQualifiedName(paramSimple).orElse(paramSimple);
                JmmClassType paramType = JmmClassType.ofInstance(paramFqn, true);
                if (types.isTypeCompatible(paramType, jmmType)) return true;
            }
            String simple = simpleNameOf(ct.name());
            return javaClass.getSimpleName().equals(simple) || javaClass.getName().equals(ct.name());
        }
        return false;
    }

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

    private boolean typeNamesMatch(String a, String b) {
        if (a == null || b == null) return false;
        if (a.equals(b)) return true;
        return simpleNameOf(a).equals(simpleNameOf(b));
    }

    private String simpleNameOf(String fqn) {
        if (fqn == null) return "";
        int dot = fqn.lastIndexOf('.');
        return dot >= 0 ? fqn.substring(dot + 1) : fqn;
    }
}
