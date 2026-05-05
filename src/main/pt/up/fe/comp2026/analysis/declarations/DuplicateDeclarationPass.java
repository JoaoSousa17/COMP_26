package pt.up.fe.comp2026.analysis.declarations;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2026.analysis.AnalysisVisitor;
import pt.up.fe.comp2026.jmm.ast.JmmKind;

import java.util.HashSet;
import java.util.Set;

public class DuplicateDeclarationPass extends AnalysisVisitor {

    public DuplicateDeclarationPass() {
        super();
        buildVisitor();
    }

    @Override
    protected void buildVisitor() {
        // register visitor for when encountering METHOD_DECL node
        addVisit(JmmKind.METHOD_DECL, this::visitMethodDecl);
    }

    private Void visitMethodDecl(JmmNode method, SymbolTable table) {
        var methodName = method.get("name");

        // collect parameters
        var params = method.getChildren(JmmKind.PARAM);
        Set<String> paramNames = new HashSet<>();

        for (var param : params) {
            String name = param.get("name");

            if (!paramNames.add(name)) {
                addReport(newError(param, "Duplicate parameter '" + name + "' in method" + methodName + "'"));
            }
        }

        // collect local variables
        var locals = method.getChildren(JmmKind.VAR_DECL);
        Set<String> localNames = new HashSet<>();

        for (var local : locals) {
            String name = local.get("name");

            // verification 1: collides with parameter
            if (paramNames.contains(name)) {
                addReport(newError(local, "Local variable '" + name + "' has same name as a parameter in method '" + methodName + "'"));
            }
            // verification 2: duplicate in locals
            else if (!localNames.add(name)) {
                addReport(newError(local, "Duplicate local variable '" + name + "' in method '" + methodName + "'"));
            }
        }

        return null;
    }
}
