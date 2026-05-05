package pt.up.fe.comp2026.analysis.arrays;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2026.analysis.AnalysisVisitorWithTable;
import pt.up.fe.comp2026.jmm.ast.JmmKind;

public class VoidBaseTypePass extends AnalysisVisitorWithTable {

    public VoidBaseTypePass(SymbolTable table) {
        super(table);
        buildVisitor();
    }

    @Override
    protected void buildVisitor() {
        addVisit(JmmKind.TYPE, this::visitType);
    }

    private Void visitType(JmmNode typeNode, SymbolTable table) {
        String name = typeNode.get("name");
        var dims = typeNode.getObjectAsList("dims", String.class);
        int dimensions = dims.size() / 2; // grammar stores [] as 2 tokens

        if ("void".equals(name) && dimensions > 0) {
            addReport(newError(typeNode, "void cannot be used as an array base type"));
        }
        return null;
    }
}
