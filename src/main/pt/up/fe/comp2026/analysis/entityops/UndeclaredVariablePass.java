package pt.up.fe.comp2026.analysis.entityops;

import pt.up.fe.comp.jmm.analysis.table.MethodSymbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2026.analysis.AnalysisVisitorWithTable;
import pt.up.fe.comp2026.jmm.ast.JmmKind;

public class UndeclaredVariablePass extends AnalysisVisitorWithTable {
    private MethodSymbol currentMethod;

    public UndeclaredVariablePass(SymbolTable table) {
        super(table);
        buildVisitor();
    }

    @Override
    protected void buildVisitor() {
        addVisit(JmmKind.METHOD_DECL, this::visitMethodDecl);
        addVisit(JmmKind.VAR_REF_EXPR, this::visitVarRefExpr);
    }

    private Void visitMethodDecl(JmmNode method, SymbolTable table) {
        var sig = types.getMethodDeclSignature(method);
        currentMethod = this.table.getMethod(sig).orElse(null);
        return null;
    }

    private Void visitVarRefExpr(JmmNode varRefExpr, SymbolTable table) {
        String name = varRefExpr.get("name");

        if (currentMethod != null && currentMethod.getParameter(name).isPresent()) return null;
        if (currentMethod != null && currentMethod.getLocalVariable(name).isPresent()) return null;
        if (this.table.getField(name).isPresent()) return null;
        if (this.table.getImportNames().contains(name)) return null;
        if (this.table.isImplicitImport(name)) return null;
        if (this.table.getClassName().equals(name)) return null; // class name used as static call receiver

        addReport(newError(varRefExpr, "Undeclared variable '" + name + "'"));
        return null;
    }
}
