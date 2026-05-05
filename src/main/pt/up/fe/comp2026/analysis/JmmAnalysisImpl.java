package pt.up.fe.comp2026.analysis;

import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2026.analysis.arrays.ArrayAccessPass;
import pt.up.fe.comp2026.analysis.arrays.ArrayInitializerTypeCheckPass;
import pt.up.fe.comp2026.analysis.calls.CallValidationPass;
import pt.up.fe.comp2026.analysis.calls.InstantiationValidationPass;
import pt.up.fe.comp2026.analysis.declarations.DuplicateDeclarationPass;
import pt.up.fe.comp2026.analysis.declarations.FieldInitializerTypeCheckPass;
import pt.up.fe.comp2026.analysis.entityops.AssignmentTypeCheckPass;
import pt.up.fe.comp2026.analysis.entityops.OperandTypeCheckPass;
import pt.up.fe.comp2026.analysis.entityops.UndeclaredVariablePass;
import pt.up.fe.comp2026.analysis.statements.ConditionAnalysisPass;
import pt.up.fe.comp2026.analysis.statements.ForUpdateCheckPass;
import pt.up.fe.comp2026.analysis.statements.ReturnAnalysisPass;
import pt.up.fe.comp2026.symboltable.JmmSymbolTableBuilder;

import java.util.ArrayList;
import java.util.List;

public class JmmAnalysisImpl implements JmmAnalysis {

    private List<AnalysisPass> buildPasses(SymbolTable table) {
        return List.of(
                // Declarations
                new DuplicateDeclarationPass(),
                new FieldInitializerTypeCheckPass(table),
                // Statements
                new ConditionAnalysisPass(table),
                new ReturnAnalysisPass(table),
                new ForUpdateCheckPass(table),
                // Entities and Operations
                new UndeclaredVariablePass(table),
                new OperandTypeCheckPass(table),
                new AssignmentTypeCheckPass(table),
                // Arrays
                new ArrayAccessPass(table),
                new ArrayInitializerTypeCheckPass(table),
                // Calls
                new InstantiationValidationPass(table),
                new CallValidationPass(table)
        );
    }

    @Override
    public JmmSemanticsResult buildSymbolTable(JmmParserResult parserResult) {
        JmmNode rootNode = parserResult.rootNode();
        var symbolTableBuilderResults = JmmSymbolTableBuilder.build(rootNode);
        SymbolTable table = symbolTableBuilderResults.table();
        List<Report> reports = symbolTableBuilderResults.reports();
        return new JmmSemanticsResult(parserResult, table, reports);
    }

    @Override
    public JmmSemanticsResult semanticAnalysis(JmmSemanticsResult semanticsResult) {
        var table = semanticsResult.getSymbolTable();
        var analysisVisitors = buildPasses(table);
        var rootNode = semanticsResult.getRootNode();
        var reports = new ArrayList<Report>();

        for (var analysisVisitor : analysisVisitors) {
            try {
                var passReports = analysisVisitor.analyze(rootNode, table);
                boolean hasErrors = passReports.stream()
                        .anyMatch(report -> report.getType() == ReportType.ERROR);
                reports.addAll(passReports);
                if (hasErrors) {
                    return new JmmSemanticsResult(semanticsResult, reports);
                }
            } catch (Exception e) {
                reports.add(Report.newError(Stage.SEMANTIC, -1, -1,
                        "Problem while executing analysis pass '" + analysisVisitor.getClass() + "'", e));
            }
        }

        return new JmmSemanticsResult(semanticsResult, reports);
    }
}
