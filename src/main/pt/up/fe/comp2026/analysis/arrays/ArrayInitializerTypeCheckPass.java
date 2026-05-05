package pt.up.fe.comp2026.analysis.arrays;

import pt.up.fe.comp.jmm.analysis.table.MethodSymbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.type.JmmType;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmPrimitiveType;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2026.analysis.AnalysisVisitorWithTable;
import pt.up.fe.comp2026.jmm.ast.JmmKind;

public class ArrayInitializerTypeCheckPass extends AnalysisVisitorWithTable {

    private MethodSymbol currentMethod;

    public ArrayInitializerTypeCheckPass(SymbolTable table) {
        super(table);
        buildVisitor();
    }

    @Override
    protected void buildVisitor() {
        addVisit(JmmKind.METHOD_DECL, this::visitMethodDecl);
        addVisit(JmmKind.ARRAY_INITIALIZER, this::visitArrayInitializer);
    }

    private Void visitMethodDecl(JmmNode method, SymbolTable table) {
        currentMethod = this.table.getMethod(types.getMethodDeclSignature(method)).orElse(null);
        return null;
    }

    // new int[] { expr, expr, ... } - each expr must be int
    private Void visitArrayInitializer(JmmNode node, SymbolTable table) {
        for (JmmNode element : node.getChildren()) {
            JmmType elemType = types.getExprType(element, currentMethod);

            if (elemType == null) continue;

            if (!elemType.equals(JmmPrimitiveType.INT)) {
                addReport(newError(element, "Array initializer element must be int, got '" + elemType + "'"));
            }
        }
        return null;
    }
}
