package pt.up.fe.comp2026.analysis.statements;

import pt.up.fe.comp.jmm.analysis.table.MethodSymbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.type.JmmType;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmPrimitiveType;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2026.analysis.AnalysisVisitorWithTable;
import pt.up.fe.comp2026.jmm.ast.JmmKind;

/**
 * Analysis pass that checks return statement correctness:
 *   - Non-void methods must have at least one return statement.
 *   - Void methods must not have any return statement.
 *   - The return expression type must match the method's declared return type.
 */
public class ReturnAnalysisPass extends AnalysisVisitorWithTable {

    /** The method currently being visited, used for return-type resolution. */
    private MethodSymbol currentMethod;

    public ReturnAnalysisPass(SymbolTable table) {
        super(table);
        buildVisitor();
    }

    @Override
    protected void buildVisitor() {
        addVisit(JmmKind.METHOD_DECL, this::visitMethodDecl);
        addVisit(JmmKind.RETURN_STMT, this::visitReturnStmt);
    }

    /**
     * Tracks the current method and checks that non-void methods contain
     * at least one return statement in their body.
     */
    private Void visitMethodDecl(JmmNode method, SymbolTable table) {
        var signature = types.getMethodDeclSignature(method);
        currentMethod = this.table.getMethod(signature).orElse(null);
        if (currentMethod == null) return null;

        JmmType returnType = currentMethod.returnType();
        boolean isVoid = JmmPrimitiveType.VOID.equals(returnType);

        if (!isVoid) {
            boolean hasReturn = !method.getDescendants(JmmKind.RETURN_STMT).isEmpty();
            if (!hasReturn) {
                addReport(newError(method,
                        "Method '" + currentMethod.name() + "' must have a return statement"));
            }
        }

        return null;
    }

    /**
     * Validates a return statement in the context of the current method:
     *   - Void methods must not return a value.
     *   - Non-void methods must return an expression whose type is compatible
     *     with the declared return type.
     */
    private Void visitReturnStmt(JmmNode returnStmt, SymbolTable table) {
        if (currentMethod == null) return null;

        JmmType returnType = currentMethod.returnType();
        boolean isVoid = JmmPrimitiveType.VOID.equals(returnType);

        if (isVoid) {
            addReport(newError(returnStmt,
                    "Void method '" + currentMethod.name() + "' must not have a return statement"));
            return null;
        }

        // Non-void: the return statement must carry an expression
        if (returnStmt.getNumChildren() == 0) {
            addReport(newError(returnStmt,
                    "Method '" + currentMethod.name() + "' must return a value of type '" + returnType + "'"));
            return null;
        }

        // Verify the expression type matches the declared return type
        JmmNode returnExpr = returnStmt.getChild(0);
        JmmType exprType = types.getExprType(returnExpr, currentMethod);

        if (exprType != null && !types.isTypeCompatible(returnType, exprType)) {
            addReport(newError(returnStmt,
                    "Type mismatch in return: expected '" + returnType + "', got '" + exprType + "'"));
        }

        return null;
    }
}
