package pt.up.fe.comp2026.analysis.declarations;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.type.JmmType;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2026.analysis.AnalysisVisitorWithTable;
import pt.up.fe.comp2026.jmm.ast.JmmKind;

public class FieldInitializerTypeCheckPass extends AnalysisVisitorWithTable {

    public FieldInitializerTypeCheckPass(SymbolTable table) {
        super(table);
        buildVisitor();
    }

    @Override
    protected void buildVisitor() {
        addVisit(JmmKind.FIELD_DECL, this::visitFieldDecl);
    }

    private Void visitFieldDecl(JmmNode fieldDecl, SymbolTable table) {
        // Grammar: fieldDecl : typeNode=type name=ID ('=' expr)? ';'
        // typeNode is stored as object attribute, not as AST child
        // The expr initializer IS a child node when present
        // Since type is stored as object, children are only: [expr] if initializer exists

        var typeNode = fieldDecl.getObject("typeNode", JmmNode.class);
        JmmType declaredType = types.convertType(typeNode);

        // Find expr child — it's any child that is not a TYPE node
        JmmNode initExpr = null;
        for (JmmNode child : fieldDecl.getChildren()) {
            if (!JmmKind.TYPE.check(child)) {
                initExpr = child;
                break;
            }
        }

        if (initExpr == null) return null; // no initializer

        JmmType initType = types.getExprType(initExpr, null);
        if (initType == null) return null;

        if (!types.isTypeCompatible(declaredType, initType)) {
            addReport(newError(initExpr,
                    "Field '" + fieldDecl.get("name") + "': initializer type '"
                            + initType + "' is not compatible with declared type '" + declaredType + "'"));
        }

        return null;
    }
}
