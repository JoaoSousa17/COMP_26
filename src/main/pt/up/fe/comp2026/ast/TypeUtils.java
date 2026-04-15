package pt.up.fe.comp2026.ast;

import pt.up.fe.comp.jmm.analysis.table.MethodSymbol;
import pt.up.fe.comp.jmm.analysis.table.Signature;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmPrimitiveType;
import pt.up.fe.comp.jmm.analysis.table.type.JmmType;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmArrayType;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmClassType;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2026.jmm.ast.JmmKind;
import pt.up.fe.comp2026.symboltable.JmmSymbolTable;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static pt.up.fe.comp2026.jmm.ast.JmmKind.*;

/**
 * Utility class for type-related operations during semantic analysis.
 *
 * Provides:
 *   - Conversion of AST type nodes to {@link JmmType} instances.
 *   - Expression type inference for all expression kinds in the grammar.
 *   - Type compatibility checking, including subtype relationships.
 *   - Method signature construction from method declaration AST nodes.
 */
public class TypeUtils {

    private final JmmSymbolTable table;

    public TypeUtils(SymbolTable table) {
        this.table = (JmmSymbolTable) table;
    }

    /** Factory method for convenience. */
    public static TypeUtils with(SymbolTable table) {
        return new TypeUtils(table);
    }

    /** Returns the singleton int primitive type. */
    public static JmmPrimitiveType intType() {
        return JmmPrimitiveType.INT;
    }

    /**
     * Converts an AST type node into a {@link JmmType}.
     * Handles primitives, imported class types, and array dimensions.
     *
     * @param typeNode an AST node of kind TYPE.
     * @return the corresponding JmmType (primitive, class, or array).
     */
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
            // Resolve class type: look up import status and FQN
            boolean isImported = table.getImportNames().contains(name) || table.isImplicitImport(name);
            String fqn = table.getImportedFullyQualifiedName(name).orElse(name);
            baseType = JmmClassType.ofInstance(fqn, isImported);
        }

        return dimensions > 0 ? new JmmArrayType(baseType, dimensions) : baseType;
    }

    /**
     * Infers the {@link JmmType} of an expression node.
     * Dispatches on the node kind; returns {@code null} if the type cannot be determined.
     *
     * @param expr          the expression AST node.
     * @param currentMethod the enclosing method, used to resolve local variables and parameters.
     * @return the inferred type, or {@code null} if unresolvable.
     */
    public JmmType getExprType(JmmNode expr, MethodSymbol currentMethod) {
        return switch (expr.getKind()) {
            case INTEGER_LITERAL  -> intType();
            case BOOL_LITERAL     -> JmmPrimitiveType.BOOLEAN;
            case BINARY_EXPR      -> getBinExprType(expr, currentMethod);
            case VAR_REF_EXPR     -> getVarExprType(expr, currentMethod);
            case PAREN_EXPR       -> getExprType(expr.getChild(0), currentMethod);
            case UNARY_EXPR       -> JmmPrimitiveType.BOOLEAN;
            case PLUS_EXPR, MINUS_EXPR, PLUS_PLUS_EXPR, MINUS_MINUS_EXPR -> intType();
            case THIS_EXPR        -> getThisType();
            case NEW_EXPR         -> getNewExprType(expr);
            case NEW_ARRAY_EXPR   -> getNewArrayExprType(expr);
            case ARRAY_INITIALIZER -> new JmmArrayType(JmmPrimitiveType.INT, 1);
            case ARRAY_LOAD_EXPR  -> getArrayLoadExprType(expr, currentMethod);
            case METHOD_CALL_EXPR -> getMethodCallType(expr, currentMethod);
            case IMPLICIT_THIS_CALL_EXPR -> getImplicitThisCallType(expr);
            case FIELD_ACCESS_EXPR -> getFieldAccessType(expr, currentMethod);
            case LENGTH_EXPR      -> intType();
            default -> null;
        };
    }

    /** Convenience overload for contexts where no enclosing method is available. */
    public JmmType getExprType(JmmNode expr) {
        return getExprType(expr, null);
    }

    /**
     * Checks whether {@code assigned} is type-compatible with {@code target}.
     * Supports direct equality and subtype relationships between class types.
     *
     * @param target   the expected type.
     * @param assigned the type being assigned or passed.
     * @return {@code true} if the assignment is valid.
     */
    public boolean isTypeCompatible(JmmType target, JmmType assigned) {
        if (target.equals(assigned)) return true;

        // Allow subtype assignment between class types
        if (target instanceof JmmClassType targetClass && assigned instanceof JmmClassType assignedClass) {
            return isSubtype(assignedClass.name(), targetClass.name(), new HashSet<>());
        }

        return false;
    }

    /**
     * Builds the {@link Signature} for a method declaration AST node
     * by extracting its name and parameter types.
     *
     * @param methodDecl an AST node of kind METHOD_DECL.
     * @return the corresponding method signature.
     */
    public Signature getMethodDeclSignature(JmmNode methodDecl) {
        METHOD_DECL.check(methodDecl);
        var methodName = methodDecl.get("name");
        var paramTypes = methodDecl.getChildren(JmmKind.PARAM).stream()
                .map(param -> convertType(param.getObject("typeNode", JmmNode.class)))
                .toList();
        return new Signature(methodName, paramTypes);
    }

    // ------------------------------------------------------------------ //
    //  Private helpers
    // ------------------------------------------------------------------ //

    /** Returns int for arithmetic operators and boolean for relational/logical operators. */
    private JmmType getBinExprType(JmmNode binaryExpr, MethodSymbol currentMethod) {
        String operator = binaryExpr.get("op");
        return switch (operator) {
            case "+", "-", "*", "/", "%" -> intType();
            case "<", ">", "<=", ">=", "==", "!=", "&&", "||" -> JmmPrimitiveType.BOOLEAN;
            default -> throw new RuntimeException("Unknown operator '" + operator + "'");
        };
    }

    /**
     * Resolves the type of a variable reference by searching, in order:
     * method parameters, method locals, and class fields.
     */
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

        return null;
    }

    /** Returns the type of {@code this} as the current class's fully qualified class type. */
    private JmmType getThisType() {
        return JmmClassType.ofInstance(table.getFullyQualifiedName(), false);
    }

    /** Returns the class type produced by a {@code new X(...)} expression. */
    private JmmType getNewExprType(JmmNode expr) {
        String name = expr.get("name");
        boolean isImported = table.getImportNames().contains(name) || table.isImplicitImport(name);
        String fqn = table.getImportedFullyQualifiedName(name).orElse(name);
        return JmmClassType.ofInstance(fqn, isImported);
    }

    /**
     * Returns the array type produced by a {@code new int[n]...} expression.
     * Dimension count is inferred from the number of size-expression children.
     */
    private JmmType getNewArrayExprType(JmmNode newArrayExpr) {
        int dims = Math.max(1, newArrayExpr.getNumChildren());
        return new JmmArrayType(JmmPrimitiveType.INT, dims);
    }

    /**
     * Returns the element type after one array access ({@code expr[i]}).
     * e.g. {@code boolean[] → boolean}, {@code int[][] → int[]}, {@code int[] → int}.
     * Uses the actual base type of the array rather than assuming int.
     */
    private JmmType getArrayLoadExprType(JmmNode arrayLoad, MethodSymbol currentMethod) {
        JmmNode arrayExpr = arrayLoad.getChild(0);
        JmmType arrayType = getExprType(arrayExpr, currentMethod);

        if (arrayType == null) return null;
        if (!(arrayType instanceof JmmArrayType)) return null;

        String typeStr = arrayType.toString();
        int dims = extractDimensionFromTypeStr(typeStr);
        JmmType baseType = extractBaseTypeFromArrayType(typeStr);

        if (dims <= 1) {
            return baseType != null ? baseType : JmmPrimitiveType.INT;
        } else {
            JmmType base = baseType != null ? baseType : JmmPrimitiveType.INT;
            return new JmmArrayType(base, dims - 1);
        }
    }

    /**
     * Extracts the primitive base type from a JmmArrayType's string representation.
     * Expected format: {@code "JmmArrayType[itemType=BOOLEAN, dimension=1]"}.
     * Checks BOOLEAN before INT to avoid false positives.
     * Returns {@code null} for non-primitive (class) base types.
     */
    private JmmType extractBaseTypeFromArrayType(String typeStr) {
        if (typeStr == null) return null;
        if (typeStr.contains("itemType=BOOLEAN") || typeStr.contains("BOOLEAN")) {
            return JmmPrimitiveType.BOOLEAN;
        }
        if (typeStr.contains("itemType=INT") || typeStr.contains("INT")) {
            return JmmPrimitiveType.INT;
        }
        // Class array base types require additional parsing — not yet supported
        return null;
    }

    /**
     * Extracts the dimension count from a JmmArrayType's string representation.
     * Falls back to counting {@code '['} characters if the {@code dimension=} field is absent.
     */
    private int extractDimensionFromTypeStr(String typeStr) {
        if (typeStr != null && typeStr.contains("dimension=")) {
            try {
                int idx = typeStr.indexOf("dimension=") + "dimension=".length();
                int end = typeStr.indexOf("]", idx);
                if (end < 0) end = typeStr.indexOf(",", idx);
                if (end < 0) end = typeStr.length();
                return Integer.parseInt(typeStr.substring(idx, end).trim());
            } catch (NumberFormatException e) { /* fall through */ }
        }
        if (typeStr != null) {
            long count = typeStr.chars().filter(c -> c == '[').count();
            if (count > 0) return (int) count;
        }
        return 1;
    }

    /**
     * Returns the return type of a method call {@code expr.method(...)}.
     * Resolves the receiver type, then looks up the method in the symbol table.
     */
    private JmmType getMethodCallType(JmmNode expr, MethodSymbol currentMethod) {
        String methodName = expr.get("name");
        JmmType objType = getExprType(expr.getChild(0), currentMethod);

        if (!(objType instanceof JmmClassType classType)) return null;

        String fqn = classType.name();

        // Check the current class first
        if (typeNamesMatch(fqn, table.getFullyQualifiedName())) {
            var methods = table.getMethods(methodName);
            if (!methods.isEmpty()) return methods.get(0).returnType();
        }

        // Fall back to imported symbol tables
        var st = resolveSymbolTable(fqn);
        if (st.isPresent()) {
            var methods = st.get().getMethods(methodName);
            if (!methods.isEmpty()) return methods.get(0).returnType();
        }

        return null;
    }

    /**
     * Returns the type of a field access {@code expr.field}.
     * Resolves the receiver type, then looks up the field in the symbol table.
     */
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

    /** Returns the return type of an implicit-this method call {@code method(...)}. */
    private JmmType getImplicitThisCallType(JmmNode expr) {
        String methodName = expr.get("name");
        var methods = table.getMethods(methodName);
        if (!methods.isEmpty()) return methods.get(0).returnType();
        return null;
    }

    /**
     * Checks whether {@code assignedFqn} is a subtype of {@code targetFqn}
     * by walking the superclass chain. Uses a visited set to avoid cycles.
     */
    private boolean isSubtype(String assignedFqn, String targetFqn, Set<String> visited) {
        if (typeNamesMatch(assignedFqn, targetFqn)) return true;
        if (visited.contains(assignedFqn)) return false;
        visited.add(assignedFqn);
        String superFqn = getSuperFqn(assignedFqn);
        if (superFqn == null) return false;
        return isSubtype(superFqn, targetFqn, visited);
    }

    /** Returns the FQN of the superclass of the given class, or {@code null} if none. */
    private String getSuperFqn(String fqn) {
        if (typeNamesMatch(fqn, table.getFullyQualifiedName())) {
            return table.getSuperFullyQualifiedName();
        }
        var st = resolveSymbolTable(fqn);
        return st.map(SymbolTable::getSuperFullyQualifiedName).orElse(null);
    }

    /** Resolves the symbol table for a class by name, checking explicit and implicit imports. */
    private Optional<SymbolTable> resolveSymbolTable(String name) {
        var st = table.getImportedSymbolTable(name);
        if (st.isEmpty()) st = table.getImplicitImport(name);
        return st;
    }

    /** Returns true if two class names refer to the same type (by FQN or simple name). */
    private boolean typeNamesMatch(String a, String b) {
        if (a.equals(b)) return true;
        return simpleNameOf(a).equals(simpleNameOf(b));
    }

    /** Extracts the simple class name from a fully qualified name (e.g. {@code "a.b.Foo"} → {@code "Foo"}). */
    private String simpleNameOf(String fqn) {
        int dot = fqn.lastIndexOf('.');
        return dot >= 0 ? fqn.substring(dot + 1) : fqn;
    }
}
