package pt.up.fe.comp2026.analysis.entityops;

import pt.up.fe.comp.jmm.analysis.table.MethodSymbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.type.JmmType;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2026.analysis.AnalysisVisitorWithTable;
import pt.up.fe.comp2026.jmm.ast.JmmKind;

public class AssignmentTypeCheckPass extends AnalysisVisitorWithTable {
    private MethodSymbol currentMethod;

    public AssignmentTypeCheckPass(SymbolTable table) {
        super(table);
        buildVisitor();
    }

    @Override
    protected void buildVisitor() {
        addVisit(JmmKind.METHOD_DECL, this::visitMethodDecl);
        addVisit(JmmKind.ASSIGN_STMT, this::visitAssignStmt);
        addVisit(JmmKind.ASSIGNMENT,  this::visitAssignment);
    }

    private Void visitMethodDecl(JmmNode method, SymbolTable table) {
        currentMethod =
                this.table.getMethod(types.getMethodDeclSignature(method)).orElse(null);
        return null;
    }

    private Void visitAssignStmt(JmmNode stmt, SymbolTable table) {
        String varName = stmt.get("var");
        JmmNode rhs = stmt.getChild(0);

        JmmType lhsType = resolveVarType(varName);
        if (lhsType == null) return null; // undeclared — caught by UndeclaredVariablePass

        JmmType rhsType = types.getExprType(rhs, currentMethod);
        if (rhsType == null) return null; // unknown type — skip

        if (!types.isTypeCompatible(lhsType, rhsType))
            addReport(newError(stmt, "Type mismatch: can't assign '" + rhsType + "' to '" + lhsType + "'"));

        return null;
    }

    // For-init and for-update: `name = expr` without semicolon
    private Void visitAssignment(JmmNode stmt, SymbolTable table) {
        String varName = stmt.get("name");
        JmmNode rhs = stmt.getChild(0);
        JmmType lhsType = resolveVarType(varName);
        if (lhsType == null) return null;
        JmmType rhsType = types.getExprType(rhs, currentMethod);
        if (rhsType == null) return null;
        if (!types.isTypeCompatible(lhsType, rhsType))
            addReport(newError(stmt, "Type mismatch: can't assign '" + rhsType + "' to '" + lhsType + "'"));
        return null;
    }

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
