package pt.up.fe.comp2026.analysis.calls;

import pt.up.fe.comp.jmm.analysis.table.MethodSymbol;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.type.JmmType;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmClassType;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp2026.analysis.AnalysisVisitorWithTable;
import pt.up.fe.comp2026.jmm.ast.JmmKind;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class MethodCallValidationPass extends AnalysisVisitorWithTable {

    private MethodSymbol currentMethod;

    public MethodCallValidationPass(SymbolTable table) {
        super(table);
        buildVisitor();
    }

    @Override
    protected void buildVisitor() {
        addVisit(JmmKind.METHOD_DECL, this::visitMethodDecl);
        addVisit(JmmKind.METHOD_CALL_EXPR, this::visitMethodCallExpr);
        addVisit(JmmKind.IMPLICIT_THIS_CALL_EXPR, this::visitImplicitThisCallExpr);
        addVisit(JmmKind.NEW_EXPR, this::visitNewExpr);
    }

    // track current method scope
    private Void visitMethodDecl(JmmNode method, SymbolTable table) {
        currentMethod = this.table.getMethod(types.getMethodDeclSignature(method)).orElse(null);
        return null;
    }

    private Void visitMethodCallExpr(JmmNode callExpr, SymbolTable table) {
        JmmNode receiver = callExpr.getChild(0);
        List<JmmNode> args = callExpr.getChildren().subList(1, callExpr.getNumChildren());
        String methodName = callExpr.get("name");

        JmmType receiverType = types.getExprType(receiver, currentMethod);

        if (receiverType == null && JmmKind.VAR_REF_EXPR.check(receiver)) {
            String receiverName = receiver.get("name");
            if (types.isClassName(receiverName)) {
                boolean isCurrentClass = receiverName.equals(this.table.getClassName());
                if (isCurrentClass) {
                    validateStaticLocalCall(callExpr, methodName, args);
                } else {
                    String fqn = this.table.getImportedFullyQualifiedName(receiverName).orElse(receiverName);
                    validadeStaticImportedCall(callExpr, receiverName, fqn, methodName, args);
                }
                return null;
            }
            return null; // unknown receiver: skip
        }

        if (!(receiverType instanceof JmmClassType classType)) return null;

        String fqn = classType.name();
        boolean isCurrentClass = typeNamesMatch(fqn, this.table.getFullyQualifiedName());

        if (isCurrentClass) {
            validateLocalCall(callExpr, methodName, args);
        } else {
            validateImportedCall(callExpr, fqn, methodName, args);
        }

        return null;
    }

    private Void visitImplicitThisCallExpr(JmmNode callExpr, SymbolTable table) {
        // static methods don't have 'this' - implicit 'this' calls are not valid
        if (currentMethod != null && currentMethod.isStatic()) {
            addReport(newError(callExpr, "Cannot use 'this' call inside static method '" + currentMethod.name() + "'"));
            return null;
        }

        String methodName = callExpr.get("name");
        List<JmmNode> args = callExpr.getChildren();
        validateLocalCall(callExpr, methodName, args);
        return null;
    }

    private Void visitNewExpr(JmmNode newExpr, SymbolTable table) {
        String className = newExpr.get("name");
        List<JmmNode> args = newExpr.getChildren(); // all children are constructor args

        boolean isSelf = className.equals(this.table.getClassName());
        boolean isImported = this.table.getImportNames().contains(className);
        boolean isImplicit = this.table.isImplicitImport(className);

        if (!isSelf && !isImported && !isImplicit) {
            addReport(newError(newExpr, "Cannot instantiate class '" + className + "': not imported"));
            return null;
        }

        // validate constructor args for imported/implicit classes
        if (!isSelf && (isImported || isImplicit)) {
            String fqn = this.table.getImportedFullyQualifiedName(className).orElse(className);
            validateConstructorArgs(newExpr, className, fqn, args);
        }
        return null;
    }

    private void validateConstructorArgs(JmmNode newExpr, String simpleName, String fqn, List<JmmNode> args) {
        try {
            Class<?> clazz = Class.forName(fqn);
            var ctors = Arrays.stream(clazz.getConstructors())
                    .filter(c -> c.getParameterCount() == args.size())
                    .toList();

            if (ctors.isEmpty() && !args.isEmpty()) {
                int anyCount = clazz.getConstructors().length > 0
                        ? clazz.getConstructors()[0].getParameterCount() : 0;
                addReport(newError(newExpr, "Constructor of '" + simpleName + "' expects " + anyCount + " argument(s) but got " + args.size()));
                return;
            }

            // accept if any overload is type-compatible
            for (var ctor : ctors) {
                if (argsCompatibleWithReflection(args, ctor.getParameterTypes())) return;
            }

            // report type mismatch for first count-matching constructor
            if (!ctors.isEmpty()) {
                var ctor = ctors.get(0);
                for (int i = 0; i < args.size(); i++) {
                    JmmType argType = types.getExprType(args.get(i), currentMethod);
                    if (argType == null) continue;
                    Class<?> paramClass = ctor.getParameterTypes()[i];
                    if (!isTypeCompatibleWithClass(argType, paramClass)) {
                        addReport(newError(args.get(i), "Constructor argument " + (i + 1) + " of '" + simpleName + "': expected " + paramClass.getSimpleName() + " but got " + argType));
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            // not on classpath
        }
    }

    private void validateLocalCall(JmmNode callExpr, String methodName, List<JmmNode> args) {
        var localMethods = this.table.getMethods(methodName);

        if (!localMethods.isEmpty()) {
            validateArgs(callExpr, methodName, args, localMethods);
            return;
        }

        // Not found locally: try superclass chain recursively
        String superFqn = this.table.getSuperFullyQualifiedName();
        if (superFqn == null) {
            addReport(newError(callExpr, "Method '" + methodName + "' not found in class hierarchy"));
            return;
        }

        var superSt = resolveSymbolTable(superFqn);
        if (superSt.isEmpty()) {
            return; // super not accessible — be lenient
        }

        var superMethods = findMethodInHierarchy(superSt.get(), methodName);
        if (!superMethods.isEmpty()) {
            validateArgs(callExpr, methodName, args, superMethods);
        } else {
            addReport(newError(callExpr, "Method '" + methodName + "' not found in class hierarchy"));
        }
    }

    private void validateImportedCall(JmmNode callExpr, String fqn, String methodName, List<JmmNode> args) {
        // classType.name() gives the simple name as written in source (e.g. "Date", not "java.util.Date").
        // Resolve it to FQN via the import list so the symbol table and Class.forName work correctly.
        String resolvedFqn = this.table.getImportedFullyQualifiedName(fqn).orElse(fqn);

        var st = resolveSymbolTable(resolvedFqn);
        if (st.isEmpty()) return; // unknown external class

        // Use Class.getMethods() (includes inherited + interface default methods) because
        // Importer uses getDeclaredMethods() and misses interface defaults (e.g. SplittableRandom.nextInt(int,int)).
        try {
            Class<?> clazz = Class.forName(resolvedFqn);
            validateCallViaReflection(callExpr, clazz, fqn, methodName, args);
        } catch (ClassNotFoundException e) {
            // Class not loadable in this JVM; fall back to ST-based validation
            var methods = findMethodInHierarchy(st.get(), methodName);
            if (methods.isEmpty()) {
                addReport(newError(callExpr, "Method '" + methodName + "' not found on class '" + simpleNameOf(fqn) + "'"));
                return;
            }
            validateArgs(callExpr, methodName, args, methods);
        }
    }

    private void validateCallViaReflection(JmmNode callExpr, Class<?> clazz, String fqn, String methodName, List<JmmNode> args) {
        var methods = Arrays.stream(clazz.getMethods())
                .filter(m -> m.getName().equals(methodName))
                .toList();

        if (methods.isEmpty()) {
            addReport(newError(callExpr, "Method '" + methodName + "' not found on class '" + simpleNameOf(fqn) + "'"));
            return;
        }

        var countMatches = methods.stream()
                .filter(m -> m.getParameterCount() == args.size())
                .toList();

        if (countMatches.isEmpty()) {
            int expected = methods.get(0).getParameterCount();
            addReport(newError(callExpr, "Method '" + methodName + "' expects " + expected + " argument(s) but got " + args.size()));
            return;
        }

        // Accept if any overload is type-compatible
        for (var method : countMatches) {
            if (argsCompatibleWithReflection(args, method.getParameterTypes())) return;
        }

        // Report type mismatch per argument for the first candidate
        var method = countMatches.get(0);
        for (int i = 0; i < args.size(); i++) {
            JmmType argType = types.getExprType(args.get(i), currentMethod);
            if (argType == null) continue;
            Class<?> paramClass = method.getParameterTypes()[i];
            if (!isTypeCompatibleWithClass(argType, paramClass)) {
                addReport(newError(args.get(i),
                        "Argument " + (i + 1) + " of '" + methodName + "': expected "
                                + paramClass.getSimpleName() + " but got " + argType));
            }
        }
    }

    private boolean argsCompatibleWithReflection(List<JmmNode> args, Class<?>[] params) {
        for (int i = 0; i < args.size(); i++) {
            JmmType argType = types.getExprType(args.get(i), currentMethod);
            if (argType == null) return true;
            if (!isTypeCompatibleWithClass(argType, params[i])) return false;
        }
        return true;
    }

    private boolean isTypeCompatibleWithClass(JmmType jmmType, Class<?> javaClass) {
        if (jmmType instanceof JmmClassType ct) {
            if (javaClass == Object.class) return true;
            try {
                Class<?> actualClass = Class.forName(ct.name());
                if (javaClass.isAssignableFrom(actualClass)) return true;
            } catch (ClassNotFoundException ignored) {}
            String simple = simpleNameOf(ct.name());
            return javaClass.getSimpleName().equals(simple) || javaClass.getName().equals(ct.name());
        }
        String printed = jmmType.print();
        return javaClass.getSimpleName().equals(printed) || javaClass.getName().equals(printed);
    }

    /**
     * Recursively searches for methods with the given name through the full super chain.
     * Needed because Importer uses getDeclaredMethods() (not getMethods()), so inherited
     * methods are absent from a class's direct symbol table.
     */
    private List<MethodSymbol> findMethodInHierarchy(SymbolTable st, String methodName) {
        var methods = st.getMethods(methodName);
        if (!methods.isEmpty()) return methods;

        String superFqn = st.getSuperFullyQualifiedName();
        if (superFqn == null) return List.of();

        return resolveSymbolTable(superFqn)
                .map(superSt -> findMethodInHierarchy(superSt, methodName))
                .orElse(List.of());
    }

    private void validateArgs(JmmNode callExpr, String methodName, List<JmmNode> args, List<MethodSymbol> candidates) {
        var countMatches = candidates.stream()
                .filter(m -> m.parameters().size() == args.size())
                .toList();

        if (countMatches.isEmpty()) {
            int expected = candidates.get(0).parameters().size();
            addReport(newError(callExpr, "Method '" + methodName + "' expects '" + expected + "argument(s) but got " + args.size()));
            return;
        }

        for (var method : countMatches) {
            if (argsCompatible(args, method.parameters())) return;
        }

        // per arg type mismatch for first candidate
        var method = countMatches.get(0);
        for (int i = 0; i < args.size(); i++) {
            JmmType paramType = method.parameters().get(i).type();
            JmmType argType = types.getExprType(args.get(i), currentMethod);
            if (argType == null) continue;
            if (!types.isTypeCompatible(paramType, argType)) {
                addReport(newError(args.get(i), "Argument " + (i + 1) + " of '" + methodName + "': expected " + paramType + " but got " + argType));
            }
        }
    }

    private boolean argsCompatible(List<JmmNode> args, List<Symbol> params) {
        for (int i = 0; i < args.size(); i++) {
            JmmType argType = types.getExprType(args.get(i), currentMethod);
            if (argType == null) return true; // unresolve arg
            if (!types.isTypeCompatible(params.get(i).type(), argType)) return false;
        }
        return true;
    }

    private Optional<SymbolTable> resolveSymbolTable(String name) {
        var st = this.table.getImportedSymbolTable(name);
        if (st.isEmpty()) st = this.table.getImplicitImport(name);
        return st;
    }

    private boolean typeNamesMatch(String a, String b) {
        return a.equals(b) || simpleNameOf(a).equals(simpleNameOf(b));
    }

    private String simpleNameOf(String fqn) {
        int dot = fqn.lastIndexOf('.');
        return dot >= 0 ? fqn.substring(dot + 1) : fqn;
    }

    private void validateStaticLocalCall(JmmNode callExpr, String methodName, List<JmmNode> args) {
        var methods = this.table.getMethods(methodName);

        if (methods.isEmpty()) {
            addReport(newError(callExpr, "Method '" + methodName + "' not found in class '" + this.table.getClassName() + "'"));
            return;
        }

        var staticMatches = methods.stream().filter(MethodSymbol::isStatic).toList();
        if (staticMatches.isEmpty()) {
            addReport(newError(callExpr, "Cannot call non-static method '" + methodName + "' on class '" + this.table.getClassName() + "'"));
            return;
        }

        // mark the AST node as static call
        callExpr.put("isStaticCall", "true");

        validateArgs(callExpr, methodName, args, staticMatches);
    }

    private void validadeStaticImportedCall(JmmNode callExpr, String simpleName, String fqn, String methodName, List<JmmNode> args) {
        // marks AST as static call early
        callExpr.put("isStaticCall", "true");

        try {
            Class<?> clazz = Class.forName(fqn);

            // oly static methods (called on class, not instance)
            var staticMethods = Arrays.stream(clazz.getMethods())
                    .filter(m -> m.getName().equals(methodName) &&
                    java.lang.reflect.Modifier.isStatic(m.getModifiers()))
                    .toList();

            if (staticMethods.isEmpty()) {
                boolean nonStaticExists = Arrays.stream(clazz.getMethods())
                        .anyMatch(m -> m.getName().equals(methodName));
                if (nonStaticExists) {
                    addReport(newError(callExpr, "Cannot call non-static method '" + methodName + "' on class '" + simpleName + "'"));
                } else {
                    addReport(newError(callExpr, "Method '" + methodName + "' not found on class '" + simpleName + "'"));
                }
                return;
            }

            validateCallViaReflection(callExpr, clazz, simpleName, methodName, args);
        } catch (ClassNotFoundException e) {
            // class not on classpath: fallback to symbol table
            var st = resolveSymbolTable(fqn);
            if (st.isEmpty()) return;

            var methods = findMethodInHierarchy(st.get(), methodName);
            if (methods.isEmpty()) {
                addReport(newError(callExpr, "Method '" + methodName + "' not found on class '" + simpleName + "'"));
                return;
            }

            var staticMatches = methods.stream().filter(MethodSymbol::isStatic).toList();
            if (staticMatches.isEmpty()) {
                addReport(newError(callExpr, "Cannot call non-static method '" + methodName + "' on class '" + simpleName + "'"));
                return;
            }

            validateArgs(callExpr, methodName, args, staticMatches);
        }
    }
}
