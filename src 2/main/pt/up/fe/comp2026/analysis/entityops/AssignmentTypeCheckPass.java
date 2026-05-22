package pt.up.fe.comp2026.analysis.entityops;

import pt.up.fe.comp.jmm.analysis.table.MethodSymbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.type.JmmType;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmArrayType;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2026.analysis.AnalysisVisitorWithTable;
import pt.up.fe.comp2026.jmm.ast.JmmKind;

/**
 * Analysis pass that type-checks assignment statements.
 *
 * Handles three cases:
 *   - {@code NewArrayExpr} RHS: verifies the LHS is an array and that dimension counts match.
 *   - {@code ArrayInitializer} RHS: verifies the LHS is an array type.
 *   - General case: verifies the RHS type is compatible with the LHS declared type.
 */
public class AssignmentTypeCheckPass extends AnalysisVisitorWithTable {

    /** The method currently being visited, used for local variable type resolution. */
    private MethodSymbol currentMethod;

    public AssignmentTypeCheckPass(SymbolTable table) {
        super(table);
        buildVisitor();
    }

    @Override
    protected void buildVisitor() {
        addVisit(JmmKind.METHOD_DECL, this::visitMethodDecl);
        addVisit(JmmKind.ASSIGN_STMT, this::visitAssignStmt);
    }

    /**
     * Tracks the current method. Falls back to a name-only lookup if the
     * signature-based resolution fails (e.g. overloaded methods not yet resolved).
     */
    private Void visitMethodDecl(JmmNode method, SymbolTable table) {
        try {
            var sig = types.getMethodDeclSignature(method);
            currentMethod = this.table.getMethod(sig).orElse(null);
        } catch (Exception e) {
            currentMethod = null;
        }
        if (currentMethod == null) {
            // Fallback: find by name alone
            String methodName = method.get("name");
            var methods = this.table.getMethods(methodName);
            if (!methods.isEmpty()) currentMethod = methods.get(0);
        }
        return null;
    }

    /**
     * Validates a single assignment statement.
     * Delegates to specialised checks for array expressions;
     * performs a general type-compatibility check otherwise.
     */
    private Void visitAssignStmt(JmmNode stmt, SymbolTable table) {
        String varName = stmt.get("var");
        JmmNode rhs = stmt.getChild(0);

        JmmType lhsType = resolveVarType(varName);
        if (lhsType == null) return null;

        // --- new int[n]... : check LHS is array and dimension counts are compatible ---
        if (JmmKind.NEW_ARRAY_EXPR.check(rhs)) {
            if (!(lhsType instanceof JmmArrayType lhsArray)) {
                addReport(newError(stmt,
                        "Cannot assign array expression to non-array variable '" + varName
                                + "' of type '" + lhsType + "'"));
                return null;
            }
            int lhsDims = extractDimension(lhsType.toString());
            int rhsSizedDims = rhs.getNumChildren(); // children = explicitly-sized dimensions

            if (rhsSizedDims > lhsDims) {
                addReport(newError(stmt,
                        "Dimension mismatch: cannot assign " + rhsSizedDims
                                + "-or-more dimensional array to " + lhsDims
                                + "-dimensional variable '" + varName + "'"));
            }

            // Also check total RHS dimensions (including unsized trailing ones) against LHS
            int rhsTotalDims = getTotalDimsFromNode(rhs, rhsSizedDims);
            if (rhsTotalDims > lhsDims) {
                addReport(newError(stmt,
                        "Dimension mismatch: cannot assign " + rhsTotalDims
                                + "-dimensional array to " + lhsDims
                                + "-dimensional variable '" + varName + "'"));
            }
            return null;
        }

        // --- new int[]{...} : LHS must be an array ---
        if (JmmKind.ARRAY_INITIALIZER.check(rhs)) {
            if (!(lhsType instanceof JmmArrayType)) {
                addReport(newError(stmt,
                        "Cannot assign array expression to non-array variable '" + varName
                                + "' of type '" + lhsType + "'"));
            }
            return null;
        }

        // --- General case: type compatibility check ---
        JmmType rhsType = types.getExprType(rhs, currentMethod);
        if (rhsType == null) return null;

        if (!types.isTypeCompatible(lhsType, rhsType))
            addReport(newError(stmt, "Type mismatch: can't assign '" + rhsType + "' to '" + lhsType + "'"));

        return null;
    }

    /**
     * Attempts to determine the total number of dimensions of a {@code NewArrayExpr} node,
     * including any unsized trailing dimensions not represented as children.
     * Tries several known attribute names ({@code numDims}, {@code totalDims}, {@code dims},
     * {@code numBrackets}); falls back to the number of children if none are found.
     */
    private int getTotalDimsFromNode(JmmNode newArrayExpr, int rhsSizedDims) {
        try {
            var numDims = newArrayExpr.getOptional("numDims");
            if (numDims.isPresent()) return Integer.parseInt(numDims.get());
        } catch (Exception ignored) {}
        try {
            var totalDims = newArrayExpr.getOptional("totalDims");
            if (totalDims.isPresent()) return Integer.parseInt(totalDims.get());
        } catch (Exception ignored) {}
        try {
            var dimsList = newArrayExpr.getObjectAsList("dims", String.class);
            if (!dimsList.isEmpty()) return dimsList.size() / 2;
        } catch (Exception ignored) {}
        try {
            var nb = newArrayExpr.getOptional("numBrackets");
            if (nb.isPresent()) return Integer.parseInt(nb.get());
        } catch (Exception ignored) {}
        // Cannot determine unsized trailing dimensions — use sized count as lower bound
        return rhsSizedDims;
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
