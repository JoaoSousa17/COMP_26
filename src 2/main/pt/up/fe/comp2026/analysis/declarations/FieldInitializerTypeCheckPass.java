package pt.up.fe.comp2026.analysis.declarations;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.type.JmmType;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2026.analysis.AnalysisVisitorWithTable;
import pt.up.fe.comp2026.jmm.ast.JmmKind;

/**
 * Analysis pass that type-checks field initializer expressions.
 *
 * For each field declaration with an initializer ({@code type name = expr;}),
 * verifies that the type of the initializer expression is compatible with the
 * declared field type. Reports an error if they are incompatible.
 */
public class FieldInitializerTypeCheckPass extends AnalysisVisitorWithTable {

    public FieldInitializerTypeCheckPass(SymbolTable table) {
        super(table);
        buildVisitor();
    }

    @Override
    protected void buildVisitor() {
        addVisit(JmmKind.FIELD_DECL, this::visitFieldDecl);
    }

    /**
     * Checks that the initializer expression of a field, if present, is type-compatible
     * with the field's declared type.
     *
     * Note: the type node is stored as an object attribute rather than an AST child,
     * so the only AST child of a field declaration (when present) is the initializer expression.
     */
    private Void visitFieldDecl(JmmNode fieldDecl, SymbolTable table) {
        // Grammar: fieldDecl : typeNode=type name=ID ('=' expr)? ';'
        // typeNode is stored as object attribute, not as AST child;
        // the only child node, if any, is the initializer expression.
        var typeNode = fieldDecl.getObject("typeNode", JmmNode.class);
        JmmType declaredType = types.convertType(typeNode);

        // Find the initializer expression — any child that is not a TYPE node
        JmmNode initExpr = null;
        for (JmmNode child : fieldDecl.getChildren()) {
            if (!JmmKind.TYPE.check(child)) {
                initExpr = child;
                break;
            }
        }

        if (initExpr == null) return null; // no initializer — nothing to check

        JmmType initType = types.getExprType(initExpr, null);
        if (initType == null) return null; // unresolved type — skip conservatively

        if (!types.isTypeCompatible(declaredType, initType)) {
            addReport(newError(initExpr,
                    "Field '" + fieldDecl.get("name") + "': initializer type '"
                            + initType + "' is not compatible with declared type '" + declaredType + "'"));
        }

        return null;
    }
}
