package pt.up.fe.comp2026.analysis.declarations;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2026.analysis.AnalysisVisitor;
import pt.up.fe.comp2026.jmm.ast.JmmKind;

import java.util.HashSet;
import java.util.Set;

/**
 * Analysis pass that detects duplicate name declarations within a method.
 *
 * For each method, reports errors when:
 *   - Two parameters share the same name.
 *   - A local variable shadows a parameter name.
 *   - Two local variables share the same name.
 */
public class DuplicateDeclarationPass extends AnalysisVisitor {

    public DuplicateDeclarationPass() {
        super();
        buildVisitor();
    }

    @Override
    protected void buildVisitor() {
        addVisit(JmmKind.METHOD_DECL, this::visitMethodDecl);
    }

    /**
     * Validates that no parameter or local variable name is declared more than once
     * within the given method, and that locals do not shadow parameters.
     */
    private Void visitMethodDecl(JmmNode method, SymbolTable table) {
        var methodName = method.get("name");

        // Collect parameter names, flagging any duplicates
        var params = method.getChildren(JmmKind.PARAM);
        Set<String> paramNames = new HashSet<>();

        for (var param : params) {
            String name = param.get("name");
            if (!paramNames.add(name)) {
                addReport(newError(param, "Duplicate parameter '" + name + "' in method" + methodName + "'"));
            }
        }

        // Collect local variable names, checking against parameters and other locals
        var locals = method.getChildren(JmmKind.VAR_DECL);
        Set<String> localNames = new HashSet<>();

        for (var local : locals) {
            String name = local.get("name");

            if (paramNames.contains(name)) {
                // Local variable clashes with a parameter name
                addReport(newError(local, "Local variable '" + name + "' has same name as a parameter in method '" + methodName + "'"));
            } else if (!localNames.add(name)) {
                // Local variable declared more than once
                addReport(newError(local, "Duplicate local variable '" + name + "' in method '" + methodName + "'"));
            }
        }

        return null;
    }
}
