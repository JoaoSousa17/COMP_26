package pt.up.fe.comp2026.analysis.statements;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2026.analysis.AnalysisVisitorWithTable;
import pt.up.fe.comp2026.jmm.ast.JmmKind;

public class ForUpdateCheckPass extends AnalysisVisitorWithTable {

    public ForUpdateCheckPass(SymbolTable table) {
        super(table);
        buildVisitor();
    }

    @Override
    protected void buildVisitor() {
        addVisit(JmmKind.FOR_STMT, this::visitForStmt);
    }

    private Void visitForStmt(JmmNode forStmt, SymbolTable table) {
        // Only error if forInit is present but forUpdate is absent
        boolean hasForInit = forStmt.getChildren().stream()
                .anyMatch(c -> JmmKind.FOR_INIT.check(c));
        boolean hasForUpdate = forStmt.getChildren().stream()
                .anyMatch(c -> JmmKind.FOR_UPDATE.check(c));

        if (hasForInit && !hasForUpdate) {
            // Report as SYNTATIC so parseSnippetWithErrors catches it
            addReport(Report.newError(
                    Stage.SYNTATIC,
                    forStmt.getLine(),
                    forStmt.getColumn(),
                    "For loop with initialization must have an update assignment",
                    null));
        }
        return null;
    }
}
