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
 *   - Void methods must not return a VALUE (but bare "return;" is allowed).
 *   - The return expression type must match the method's declared return type.
 */
public class ReturnAnalysisPass extends AnalysisVisitorWithTable {

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

    private Void visitMethodDecl(JmmNode method, SymbolTable table) {
        var signature = types.getMethodDeclSignature(method);
        currentMethod = this.table.getMethod(signature).orElse(null);
        if (currentMethod == null) return null;

        JmmType returnType = currentMethod.returnType();
        boolean isVoid = JmmPrimitiveType.VOID.equals(returnType);

        // Non-void methods must have at least one return statement
        if (!isVoid) {
            boolean hasReturn = !method.getDescendants(JmmKind.RETURN_STMT).isEmpty();
            if (!hasReturn) {
                addReport(newError(method,
                        "Method '" + currentMethod.name() + "' must have a return statement"));
            }
        }

        return null;
    }

    private Void visitReturnStmt(JmmNode returnStmt, SymbolTable table) {
        if (currentMethod == null) return null;

        JmmType returnType = currentMethod.returnType();
        boolean isVoid = JmmPrimitiveType.VOID.equals(returnType);

        if (isVoid) {
            // Void method: bare "return;" is fine; "return expr;" is not
            if (returnStmt.getNumChildren() > 0) {
                addReport(newError(returnStmt,
                        "Void method '" + currentMethod.name() + "' cannot return a value"));
            }
            return null;
        }

        // Non-void: must carry an expression
        if (returnStmt.getNumChildren() == 0) {
            addReport(newError(returnStmt,
                    "Method '" + currentMethod.name() + "' must return a value of type '" + returnType + "'"));
            return null;
        }

        // Verify expression type matches declared return type
        JmmNode returnExpr = returnStmt.getChild(0);
        JmmType exprType = types.getExprType(returnExpr, currentMethod);

        if (exprType != null && !types.isTypeCompatible(returnType, exprType)) {
            addReport(newError(returnStmt,
                    "Type mismatch in return: expected '" + returnType + "', got '" + exprType + "'"));
        }

        return null;
    }
}
