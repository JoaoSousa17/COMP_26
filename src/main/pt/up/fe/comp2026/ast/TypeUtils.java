package pt.up.fe.comp2026.ast;

import pt.up.fe.comp.jmm.analysis.table.MethodSymbol;
import pt.up.fe.comp.jmm.analysis.table.Signature;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmPrimitiveType;
import pt.up.fe.comp.jmm.analysis.table.type.JmmType;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmArrayType;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmClassType;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2026.jmm.ast.JmmKind;
import pt.up.fe.comp2026.symboltable.JmmSymbolTable;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static pt.up.fe.comp2026.jmm.ast.JmmKind.*;


/**
 * Utility methods regarding types.
 */
public class TypeUtils {


    private final JmmSymbolTable table;

    public TypeUtils(SymbolTable table) {
        this.table = (JmmSymbolTable) table;
    }

    public static TypeUtils with(SymbolTable table) {
        return new TypeUtils(table);
    }

    public static JmmPrimitiveType intType() {
        return JmmPrimitiveType.INT;
    }

    public JmmType convertType(JmmNode typeNode) {
        assert (JmmKind.TYPE.check(typeNode));

        var name = typeNode.get("name");
        var dimsList = typeNode.getObjectAsList("dims", String.class);
        int dimensions = dimsList.size() / 2;

        JmmType baseType;
        var primitive = JmmPrimitiveType.fromString(name);

        if (primitive.isPresent()) {
            baseType = primitive.get();
        } else {
            String fqn;
            boolean isImported;
            if (name.equals(table.getClassName())) {
                fqn = table.getFullyQualifiedName();
                isImported = false;
            } else {
                isImported = table.getImportNames().contains(name) || table.isImplicitImport(name);
                fqn = table.getImportedFullyQualifiedName(name).orElse(name);
            }
            baseType = JmmClassType.ofInstance(fqn, isImported);
        }

        return dimensions > 0 ? new JmmArrayType(baseType, dimensions) : baseType;
    }


    /**
     * Gets the {@link JmmType} of an arbitrary expression, using method context for variable lookup.
     * Returns null for expression kinds whose type cannot be determined (e.g. unresolved imports).
     */
    public JmmType getExprType(JmmNode expr, MethodSymbol currentMethod) {
        return switch (expr.getKind()) {
            case INTEGER_LITERAL         -> intType();
            case BOOL_LITERAL            -> JmmPrimitiveType.BOOLEAN;
            case BINARY_EXPR             -> getBinExprType(expr, currentMethod);
            case VAR_REF_EXPR            -> getVarExprType(expr, currentMethod);
            case PAREN_EXPR              -> getExprType(expr.getChild(0), currentMethod);
            case UNARY_EXPR              -> JmmPrimitiveType.BOOLEAN; // only `!`
            case PLUS_EXPR, MINUS_EXPR, PLUS_PLUS_EXPR, MINUS_MINUS_EXPR -> intType();
            case THIS_EXPR               -> getThisType();
            case NEW_EXPR                -> getNewExprType(expr);
            case METHOD_CALL_EXPR        -> getMethodCallType(expr, currentMethod);
            case IMPLICIT_THIS_CALL_EXPR -> getImplicitThisCallType(expr);
            case FIELD_ACCESS_EXPR       -> getFieldAccessType(expr, currentMethod);
            case LENGTH_EXPR             -> intType();
            case ARRAY_LOAD_EXPR         -> getArrayLoadType(expr, currentMethod);
            case NEW_ARRAY_EXPR          -> getNewArrayType(expr);
            case ARRAY_INITIALIZER       -> new JmmArrayType(JmmPrimitiveType.INT, 1);
            // Unknown or unimplemented kinds — return null so callers skip the check
            default -> null;
        };
    }

    /**
     * Gets the {@link JmmType} of an arbitrary expression (no method context).
     */
    public JmmType getExprType(JmmNode expr) {
        return getExprType(expr, null);
    }

    /**
     * Returns true if {@code assigned} is type-compatible with {@code target}.
     * Handles same-type equality and class hierarchy (assigned extends target).
     */
    public boolean isTypeCompatible(JmmType target, JmmType assigned) {
        if (target.equals(assigned)) return true;

        if (target instanceof JmmClassType targetClass && assigned instanceof JmmClassType assignedClass) {
            return isSubtype(assignedClass.name(), targetClass.name(), new HashSet<>());
        }

        return false;
    }

    public boolean isClassName(String name) {
        return name.equals(table.getClassName())
                || table.getImportNames().contains(name)
                || table.isImplicitImport(name);
    }


    public Signature getMethodDeclSignature(JmmNode methodDecl) {
        METHOD_DECL.check(methodDecl);

        var methodName = methodDecl.get("name");

        var paramTypes = methodDecl.getChildren(JmmKind.PARAM).stream()
                .map(param -> convertType(param.getObject("typeNode", JmmNode.class)))
                .toList();

        return new Signature(methodName, paramTypes);
    }


    // --- private helpers ---

    private JmmType getBinExprType(JmmNode binaryExpr, MethodSymbol currentMethod) {
        String operator = binaryExpr.get("op");

        return switch (operator) {
            case "+", "-", "*", "/", "%" -> intType();
            case "<", ">", "<=", ">=", "==", "!=", "&&", "||" -> JmmPrimitiveType.BOOLEAN;
            default ->
                    throw new RuntimeException("Unknown operator '" + operator + "' of expression '" + binaryExpr + "'");
        };
    }

    private JmmType getVarExprType(JmmNode varRefExpr, MethodSymbol currentMethod) {
        String name = varRefExpr.get("name");

        if (currentMethod != null) {
            var param = currentMethod.getParameter(name);
            if (param.isPresent()) return param.get().type();

            var local = currentMethod.getLocalVariable(name);
            if (local.isPresent()) return local.get().type();
        }

        var field = table.getField(name);
        if (field.isPresent()) return field.get().type();

        // Could be an imported class name used as a receiver — type unknown
        return null;
    }

    private JmmType getThisType() {
        String fqn = table.getFullyQualifiedName();
        return JmmClassType.ofInstance(fqn, false);
    }

    private JmmType getNewExprType(JmmNode expr) {
        String name = expr.get("name");
        boolean isImported = table.getImportNames().contains(name) || table.isImplicitImport(name);
        String fqn = table.getImportedFullyQualifiedName(name).orElse(name);
        return JmmClassType.ofInstance(fqn, isImported);
    }

    private JmmType getMethodCallType(JmmNode expr, MethodSymbol currentMethod) {
        String methodName = expr.get("name");
        JmmNode receiver = expr.getChild(0);
        JmmType objType = getExprType(receiver, currentMethod);

        // Static call: receiver is an imported class name (VAR_REF_EXPR with null type)
        if (objType == null && VAR_REF_EXPR.check(receiver)) {
            String className = receiver.get("name");
            String fqn = table.getImportedFullyQualifiedName(className).orElse(className);
            var st = resolveSymbolTable(fqn);
            if (st.isPresent()) {
                var methods = st.get().getMethods(methodName);
                if (!methods.isEmpty()) return methods.get(0).returnType();
            }
            try {
                Class<?> clazz = Class.forName(fqn);
                return Arrays.stream(clazz.getMethods())
                        .filter(m -> m.getName().equals(methodName))
                        .findFirst()
                        .map(m -> javaClassToJmmType(m.getReturnType()))
                        .orElse(null);
            } catch (ClassNotFoundException ignored) {}
            return null;
        }

        if (!(objType instanceof JmmClassType classType)) return null;

        String fqn = classType.name();

        // Called on the current class
        if (typeNamesMatch(fqn, table.getFullyQualifiedName())) {
            var methods = table.getMethods(methodName);
            if (!methods.isEmpty()) return methods.get(0).returnType();
        }

        // Called on an imported or implicit class — try symbol table first
        var st = resolveSymbolTable(fqn);
        if (st.isPresent()) {
            var methods = st.get().getMethods(methodName);
            if (!methods.isEmpty()) return methods.get(0).returnType();
        }

        // Fallback: use Java reflection so imported real classes resolve correctly
        String resolvedFqn = table.getImportedFullyQualifiedName(simpleNameOf(fqn)).orElse(fqn);
        try {
            Class<?> clazz = Class.forName(resolvedFqn);
            return Arrays.stream(clazz.getMethods())
                    .filter(m -> m.getName().equals(methodName))
                    .findFirst()
                    .map(m -> javaClassToJmmType(m.getReturnType()))
                    .orElse(null);
        } catch (ClassNotFoundException ignored) {}

        return null;
    }

    private JmmType javaClassToJmmType(Class<?> clazz) {
        if (clazz == int.class || clazz == Integer.class) return JmmPrimitiveType.INT;
        if (clazz == boolean.class || clazz == Boolean.class) return JmmPrimitiveType.BOOLEAN;
        if (clazz == void.class) return JmmPrimitiveType.VOID;
        if (clazz.isArray()) return new JmmArrayType(javaClassToJmmType(clazz.getComponentType()), 1);
        return JmmClassType.ofInstance(clazz.getName(), true);
    }

    private JmmType getFieldAccessType(JmmNode expr, MethodSymbol currentMethod) {
        String fieldName = expr.get("name");
        JmmType objType = getExprType(expr.getChild(0), currentMethod);
        if (!(objType instanceof JmmClassType classType)) return null;

        String fqn = classType.name();

        if (typeNamesMatch(fqn, table.getFullyQualifiedName())) {
            var field = table.getField(fieldName);
            if (field.isPresent()) return field.get().type();
        }

        var st = resolveSymbolTable(fqn);
        if (st.isPresent()) {
            var field = st.get().getField(fieldName);
            if (field.isPresent()) return field.get().type();
        }

        return null;
    }

    private JmmType getImplicitThisCallType(JmmNode expr) {
        String methodName = expr.get("name");
        var methods = table.getMethods(methodName);
        if (!methods.isEmpty()) return methods.get(0).returnType();
        return null;
    }

    /**
     * Checks if {@code assignedFqn} is a subtype of {@code targetFqn} by walking the class hierarchy.
     * Handles both FQN and simple-name comparisons for implicit imports.
     */
    private boolean isSubtype(String assignedFqn, String targetFqn, Set<String> visited) {
        if (typeNamesMatch(assignedFqn, targetFqn)) return true;
        if (visited.contains(assignedFqn)) return false;
        visited.add(assignedFqn);

        String superFqn = getSuperFqn(assignedFqn);
        if (superFqn == null) return false;
        return isSubtype(superFqn, targetFqn, visited);
    }

    private String getSuperFqn(String fqn) {
        // Current class
        if (typeNamesMatch(fqn, table.getFullyQualifiedName())) {
            var superFqn = table.getSuperFullyQualifiedName();
            return (superFqn == null || superFqn.isBlank()) ? "java.lang.Object" : superFqn;
        }

        var st = resolveSymbolTable(fqn);
        if (st.isPresent()) {
            var superFqn = st.get().getSuperFullyQualifiedName();
            return (superFqn == null || superFqn.isBlank()) ? "java.lang.Object" : superFqn;
        }
        return null;
    }

    /**
     * Tries to resolve a SymbolTable for the given class name (FQN or simple name).
     */
    private Optional<SymbolTable> resolveSymbolTable(String name) {
        var st = table.getImportedSymbolTable(name);
        if (st.isPresent()) return st;
        st = table.getImplicitImport(name);
        if (st.isPresent()) return st;
        // name may be a simple name — resolve to FQN via imports and retry
        String fqn = table.getImportedFullyQualifiedName(name).orElse(null);
        if (fqn != null) {
            st = table.getImportedSymbolTable(fqn);
            if (st.isPresent()) return st;
        }
        return Optional.empty();
    }

    /**
     * Compares two class names, tolerating simple-name vs FQN mismatches for java.lang types.
     */
    private boolean typeNamesMatch(String a, String b) {
        if (a.equals(b)) return true;
        return simpleNameOf(a).equals(simpleNameOf(b));
    }

    private String simpleNameOf(String fqn) {
        int dot = fqn.lastIndexOf('.');
        return dot >= 0 ? fqn.substring(dot + 1) : fqn;
    }

    // a[i] in int[][] -> returns int[];  a[i] on int[] -> returns int
    private JmmType getArrayLoadType(JmmNode expr, MethodSymbol currentMethod) {
        JmmType arrayType = getExprType(expr.getChild(0), currentMethod);
        if (!(arrayType instanceof JmmArrayType arr)) return null;
        int accessDepth = expr.getNumChildren() - 1; // number of index children
        JmmType result = arr;
        for (int i = 0; i < accessDepth; i++) {
            if (!(result instanceof JmmArrayType a)) return null;
            result = a.dimension() == 1
                    ? a.itemType()
                    : new JmmArrayType(a.itemType(), a.dimension() - 1);
        }
        return result;
    }

    // new int[n] -> int[];  new int[n][m] -> int[][];  new int[n][][] -> int[][][]
    private JmmType getNewArrayType(JmmNode expr) {
        // edims: one entry per extra dimension (']' for sized, '[' for unsized)
        var edimsList = expr.getObjectAsList("edims", String.class);
        int totalDims = 1 + edimsList.size();
        return new JmmArrayType(JmmPrimitiveType.INT, totalDims);
    }

}