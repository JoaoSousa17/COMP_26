package pt.up.fe.comp2026.analysis.entityops;

import pt.up.fe.comp.jmm.analysis.table.MethodSymbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.type.JmmType;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmArrayType;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmPrimitiveType;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2026.analysis.AnalysisVisitorWithTable;
import pt.up.fe.comp2026.jmm.ast.JmmKind;

/**
 * Analysis pass that enforces array-related semantic rules:
 *   - {@code void[]} is not a valid type for variables, fields, or parameters.
 *   - Array access expressions ({@code expr[i]}) require an array operand and an int index.
 *   - Array store statements ({@code name[i] = v}) require int indices and a compatible value type.
 *   - Array initializer elements ({@code new int[]{...}}) must be of type int.
 *   - Array size expressions ({@code new int[n]}) must be of type int.
 *   - {@code .length} may only be applied to array types.
 *   - Array expressions may only be assigned to array-typed variables.
 *   - Multi-dimensional array creation must not have "holes" (unsized dimension followed by a sized one).
 */
public class ArraySemanticAnalysisPass extends AnalysisVisitorWithTable {

    /** The method currently being visited, used for type resolution. */
    private MethodSymbol currentMethod;

    public ArraySemanticAnalysisPass(SymbolTable table) {
        super(table);
        buildVisitor();
    }

    @Override
    protected void buildVisitor() {
        addVisit(JmmKind.METHOD_DECL,       this::visitMethodDecl);
        addVisit(JmmKind.VAR_DECL,          this::visitVarDecl);
        addVisit(JmmKind.FIELD_DECL,        this::visitFieldDecl);
        addVisit(JmmKind.PARAM,             this::visitParam);
        addVisit(JmmKind.ARRAY_LOAD_EXPR,   this::visitArrayLoadExpr);
        addVisit(JmmKind.ARRAY_STORE_STMT,  this::visitArrayStoreStmt);
        addVisit(JmmKind.ARRAY_INITIALIZER, this::visitArrayInitializer);
        addVisit(JmmKind.NEW_ARRAY_EXPR,    this::visitNewArrayExpr);
        addVisit(JmmKind.LENGTH_EXPR,       this::visitLengthExpr);
        addVisit(JmmKind.ASSIGN_STMT,       this::visitAssignStmt);
    }

    /** Tracks the current method being analysed for scoped type resolution. */
    private Void visitMethodDecl(JmmNode method, SymbolTable table) {
        var sig = types.getMethodDeclSignature(method);
        currentMethod = this.table.getMethod(sig).orElse(null);
        return null;
    }

    // ------------------------------------------------------------------ //
    //  void[] type checks
    // ------------------------------------------------------------------ //

    private Void visitVarDecl(JmmNode node, SymbolTable table)   { checkVoidArray(node); return null; }
    private Void visitFieldDecl(JmmNode node, SymbolTable table) { checkVoidArray(node); return null; }
    private Void visitParam(JmmNode node, SymbolTable table)     { checkVoidArray(node); return null; }

    /** Reports an error if a declaration uses {@code void[]} as its type. */
    private void checkVoidArray(JmmNode declNode) {
        JmmNode typeNode = declNode.getObject("typeNode", JmmNode.class);
        String typeName = typeNode.get("name");
        var dimsList = typeNode.getObjectAsList("dims", String.class);
        int dimensions = dimsList.size() / 2;
        if ("void".equals(typeName) && dimensions > 0) {
            addReport(newError(declNode,
                    "void[] is not a valid type for '" + declNode.get("name") + "'"));
        }
    }

    // ------------------------------------------------------------------ //
    //  Array access: expr[expr]
    // ------------------------------------------------------------------ //

    /**
     * Validates an array load expression ({@code expr[index]}).
     * The base expression must be an array type and the index must be int.
     */
    private Void visitArrayLoadExpr(JmmNode arrayLoad, SymbolTable table) {
        JmmNode arrayExpr = arrayLoad.getChild(0);
        JmmNode indexExpr = arrayLoad.getChild(1);

        JmmType arrayType = types.getExprType(arrayExpr, currentMethod);
        if (arrayType != null && !(arrayType instanceof JmmArrayType)) {
            addReport(newError(arrayExpr,
                    "Cannot apply [] to non-array type '" + arrayType + "'"));
        }

        JmmType indexType = types.getExprType(indexExpr, currentMethod);
        if (indexType != null && !indexType.equals(JmmPrimitiveType.INT)) {
            addReport(newError(indexExpr,
                    "Array index must be of type int, but got '" + indexType + "'"));
        }
        return null;
    }

    // ------------------------------------------------------------------ //
    //  Array store: name[expr]* = expr
    // ------------------------------------------------------------------ //

    /**
     * Validates an array store statement ({@code name[i]... = value}).
     * All index expressions must be int, and the value type must be compatible
     * with the array's element type.
     */
    private Void visitArrayStoreStmt(JmmNode arrayStore, SymbolTable table) {
        int numChildren = arrayStore.getNumChildren();

        // All children except the last are index expressions
        for (int i = 0; i < numChildren - 1; i++) {
            JmmNode indexExpr = arrayStore.getChild(i);
            JmmType indexType = types.getExprType(indexExpr, currentMethod);
            if (indexType != null && !indexType.equals(JmmPrimitiveType.INT)) {
                addReport(newError(indexExpr,
                        "Array index must be of type int, but got '" + indexType + "'"));
            }
        }

        // Check the value expression type against the array's element type
        if (numChildren > 0) {
            String varName = arrayStore.get("name");
            JmmType varType = resolveVarType(varName);

            if (varType instanceof JmmArrayType && numChildren > 0) {
                JmmNode valueExpr = arrayStore.getChild(numChildren - 1);
                JmmType valueType = types.getExprType(valueExpr, currentMethod);

                if (valueType != null) {
                    JmmType elementType = getElementType(varType);
                    if (elementType != null && !types.isTypeCompatible(elementType, valueType)) {
                        addReport(newError(valueExpr,
                                "Type mismatch in array store: expected '" + elementType
                                        + "' but got '" + valueType + "'"));
                    }
                }
            }
        }
        return null;
    }

    /**
     * Returns the element type of an array type by stripping one dimension.
     * e.g. {@code boolean[] → boolean}, {@code int[][] → int[]}, {@code int[] → int}.
     * Uses the real base type of the JmmArrayType rather than assuming int.
     */
    private JmmType getElementType(JmmType arrayType) {
        if (!(arrayType instanceof JmmArrayType)) return null;
        String typeStr = arrayType.toString();
        int dims = extractDimension(typeStr);
        JmmType baseType = extractBaseType(typeStr);
        if (baseType == null) baseType = JmmPrimitiveType.INT; // safe fallback
        if (dims <= 1) return baseType;
        return new JmmArrayType(baseType, dims - 1);
    }

    /**
     * Extracts the primitive base type from a JmmArrayType's string representation.
     * Expected format: {@code "JmmArrayType[itemType=BOOLEAN, dimension=1]"}.
     * Checks BOOLEAN before INT to avoid false positives.
     */
    private JmmType extractBaseType(String typeStr) {
        if (typeStr == null) return null;
        if (typeStr.contains("itemType=BOOLEAN") || typeStr.contains("BOOLEAN")) {
            return JmmPrimitiveType.BOOLEAN;
        }
        if (typeStr.contains("itemType=INT") || typeStr.contains("INT")) {
            return JmmPrimitiveType.INT;
        }
        return null;
    }

    /**
     * Extracts the dimension count from a JmmArrayType's string representation.
     * Falls back to counting {@code '['} characters if the {@code dimension=} field is absent.
     */
    private int extractDimension(String typeStr) {
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

    // ------------------------------------------------------------------ //
    //  Array initializer: new int[]{ expr, ... } — elements must be int
    // ------------------------------------------------------------------ //

    /** Validates that every element in an array initializer is of type int. */
    private Void visitArrayInitializer(JmmNode arrayInit, SymbolTable table) {
        for (JmmNode element : arrayInit.getChildren()) {
            JmmType elemType = types.getExprType(element, currentMethod);
            if (elemType != null && !elemType.equals(JmmPrimitiveType.INT)) {
                addReport(newError(element,
                        "Array initializer elements must be of type int, but got '" + elemType + "'"));
            }
        }
        return null;
    }

    // ------------------------------------------------------------------ //
    //  new int[size] / new int[size][size] — sizes must be int,
    //  and no "holes" are allowed (e.g. new int[16][][44] is invalid)
    // ------------------------------------------------------------------ //

    /**
     * Validates a {@code new int[...]} expression.
     * Each size expression must be of type int, at least one size must be provided,
     * and no dimension may be sized after an unsized dimension (no holes).
     */
    private Void visitNewArrayExpr(JmmNode newArrayExpr, SymbolTable table) {
        // TODO: remove debug logging before release
        System.out.println("=== NewArrayExpr ===");
        System.out.println("  attributes: " + newArrayExpr.getAttributes());
        System.out.println("  numChildren: " + newArrayExpr.getNumChildren());
        for (int i = 0; i < newArrayExpr.getNumChildren(); i++) {
            JmmNode child = newArrayExpr.getChild(i);
            System.out.println("  child[" + i + "] kind=" + child.getKind()
                    + " numChildren=" + child.getNumChildren()
                    + " attributes=" + child.getAttributes());
        }

        // Every size expression must be of type int
        for (JmmNode sizeExpr : newArrayExpr.getChildren()) {
            JmmType sizeType = types.getExprType(sizeExpr, currentMethod);
            if (sizeType != null && !sizeType.equals(JmmPrimitiveType.INT)) {
                addReport(newError(sizeExpr,
                        "Array size must be of type int, but got '" + sizeType + "'"));
            }
        }

        // At least one sized dimension is required
        if (newArrayExpr.getNumChildren() == 0) {
            addReport(newError(newArrayExpr,
                    "Array creation requires at least the first dimension size"));
            return null;
        }

        checkPartialSizeDimensions(newArrayExpr);
        return null;
    }

    /**
     * Checks that no "hole" exists in the dimension sizes of a {@code new int[...]} expression.
     *
     * A hole occurs when a dimension without an explicit size is followed by one that has a size:
     * <ul>
     *   <li>{@code new int[16][][]}   → valid   (1 sized, 2 unsized at the end)</li>
     *   <li>{@code new int[16][][44]} → invalid (hole: dim 1 is unsized but dim 2 is sized)</li>
     *   <li>{@code new int[][16][44]} → invalid (hole: dim 0 is unsized but dims 1 and 2 are sized)</li>
     * </ul>
     *
     * Attempts to read dimension position info from node attributes ({@code dimSizes}, {@code dimHasSizes},
     * {@code sizedPositions}), falling back to inferring from the parent assignment's LHS type.
     */
    private void checkPartialSizeDimensions(JmmNode newArrayExpr) {
        // Attempt 1: read a dimSizes attribute (list where "" = unsized)
        try {
            var dimsList = newArrayExpr.getObjectAsList("dimSizes", String.class);
            if (dimsList != null && !dimsList.isEmpty()) {
                checkDimsListForGaps(newArrayExpr, dimsList);
                return;
            }
        } catch (Exception ignored) {}

        // Attempt 2: infer from the LHS type of a parent ASSIGN_STMT
        JmmNode parent = newArrayExpr.getParent();
        if (parent != null && JmmKind.ASSIGN_STMT.check(parent)) {
            String varName = parent.get("var");
            JmmType lhsType = resolveVarType(varName);
            if (lhsType instanceof JmmArrayType) {
                int totalDims = extractDimension(lhsType.toString());
                int sizedDims = newArrayExpr.getNumChildren();
                checkDimsViaNodeAttributes(newArrayExpr, totalDims, sizedDims);
            }
        }
    }

    /**
     * Scans a dimension list for holes: a sized dimension appearing after an unsized one.
     * Empty or null entries are treated as unsized; any other value is treated as sized.
     */
    private void checkDimsListForGaps(JmmNode newArrayExpr, java.util.List<String> dimsList) {
        boolean foundEmpty = false;
        for (int i = 0; i < dimsList.size(); i++) {
            String dim = dimsList.get(i);
            boolean hasSize = dim != null && !dim.isEmpty();
            if (!hasSize) {
                foundEmpty = true;
            } else if (foundEmpty) {
                addReport(newError(newArrayExpr,
                        "Invalid array creation: cannot specify size for dimension " + (i + 1)
                                + " without specifying size for all previous dimensions"));
                return;
            }
        }
    }

    /**
     * Attempts to detect dimension holes using supplementary node attributes
     * ({@code dimHasSizes} or {@code sizedPositions}) when the dimension list is unavailable.
     */
    private void checkDimsViaNodeAttributes(JmmNode newArrayExpr, int totalDims, int sizedDims) {
        // Try dimHasSizes — list of strings indicating whether each dimension has a size
        try {
            var hasSizes = newArrayExpr.getObjectAsList("dimHasSizes", String.class);
            if (hasSizes != null && !hasSizes.isEmpty()) {
                checkDimsListForGaps(newArrayExpr, hasSizes);
                return;
            }
        } catch (Exception ignored) {}

        // Try sizedPositions — list of indices of the sized dimensions (must be contiguous from 0)
        try {
            var positions = newArrayExpr.getObjectAsList("sizedPositions", Integer.class);
            if (positions != null && !positions.isEmpty()) {
                for (int i = 0; i < positions.size(); i++) {
                    if (positions.get(i) != i) {
                        addReport(newError(newArrayExpr,
                                "Invalid array creation: sized dimensions must come first"));
                        return;
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    // ------------------------------------------------------------------ //
    //  .length on non-array is an error
    // ------------------------------------------------------------------ //

    /** Reports an error if {@code .length} is accessed on a non-array type. */
    private Void visitLengthExpr(JmmNode lengthExpr, SymbolTable table) {
        JmmNode target = lengthExpr.getChild(0);
        JmmType targetType = types.getExprType(target, currentMethod);
        if (targetType != null && !(targetType instanceof JmmArrayType)) {
            addReport(newError(target,
                    "Cannot access .length on non-array type '" + targetType + "'"));
        }
        return null;
    }

    // ------------------------------------------------------------------ //
    //  AssignStmt: array expressions must be assigned to array variables
    // ------------------------------------------------------------------ //

    /**
     * Checks that {@code new int[n]} and {@code new int[]{...}} expressions
     * are only assigned to array-typed variables.
     */
    private Void visitAssignStmt(JmmNode stmt, SymbolTable table) {
        JmmNode rhs = stmt.getChild(0);
        boolean rhsIsNewArray = JmmKind.NEW_ARRAY_EXPR.check(rhs)
                || JmmKind.ARRAY_INITIALIZER.check(rhs);

        if (!rhsIsNewArray) return null;

        String varName = stmt.get("var");
        JmmType lhsType = resolveVarType(varName);
        if (lhsType == null) return null;

        if (!(lhsType instanceof JmmArrayType)) {
            addReport(newError(stmt,
                    "Cannot assign array expression to non-array variable '" + varName
                            + "' of type '" + lhsType + "'"));
        }
        return null;
    }

    /**
     * Resolves the type of a variable by searching, in order:
     * the current method's parameters, the current method's locals, and class fields.
     */
    private JmmType resolveVarType(String name) {
        if (currentMethod != null) {
            var p = currentMethod.getParameter(name);
            if (p.isPresent()) return p.get().type();
            var l = currentMethod.getLocalVariable(name);
            if (l.isPresent()) return l.get().type();
        }
        var f = this.table.getField(name);
        return f.map(pt.up.fe.comp.jmm.analysis.table.Symbol::type).orElse(null);
    }
}