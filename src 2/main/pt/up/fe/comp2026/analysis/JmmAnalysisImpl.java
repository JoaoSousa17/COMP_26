package pt.up.fe.comp2026.analysis;

import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2026.analysis.calls.CallValidationPass;
import pt.up.fe.comp2026.analysis.calls.InstantiationValidationPass;
import pt.up.fe.comp2026.analysis.declarations.DuplicateDeclarationPass;
import pt.up.fe.comp2026.analysis.declarations.FieldInitializerTypeCheckPass;
import pt.up.fe.comp2026.analysis.entityops.ArraySemanticAnalysisPass;
import pt.up.fe.comp2026.analysis.entityops.AssignmentTypeCheckPass;
import pt.up.fe.comp2026.analysis.entityops.OperandTypeCheckPass;
import pt.up.fe.comp2026.analysis.entityops.UndeclaredVariablePass;
import pt.up.fe.comp2026.analysis.statements.ConditionAnalysisPass;
import pt.up.fe.comp2026.analysis.statements.ForUpdateCheckPass;
import pt.up.fe.comp2026.analysis.statements.ReturnAnalysisPass;
import pt.up.fe.comp2026.symboltable.JmmSymbolTableBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Main entry point for the semantic analysis phase of the Java-- compiler.
 *
 * Implements {@link JmmAnalysis} with two responsibilities:
 *   1. {@link #buildSymbolTable} — constructs the symbol table from the parsed AST.
 *   2. {@link #semanticAnalysis} — runs all semantic analysis passes in order,
 *      stopping early if any pass produces errors.
 */
public class JmmAnalysisImpl implements JmmAnalysis {

    /**
     * Constructs the ordered list of semantic analysis passes to run.
     * Passes are executed in declaration → statement → expression → call order
     * so that earlier passes can assume structural validity when later ones run.
     *
     * @param table the symbol table built from the current program.
     * @return ordered list of analysis passes.
     */
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
                new ArraySemanticAnalysisPass(table),
                // Calls
                new InstantiationValidationPass(table),
                new CallValidationPass(table)
        );
    }

    /**
     * Builds the symbol table from the parser result.
     * Delegates to {@link JmmSymbolTableBuilder} and collects any reports
     * produced during symbol table construction.
     *
     * @param parserResult the result of the parsing phase.
     * @return a {@link JmmSemanticsResult} containing the symbol table and any build reports.
     */
    @Override
    public JmmSemanticsResult buildSymbolTable(JmmParserResult parserResult) {
        JmmNode rootNode = parserResult.rootNode();
        var symbolTableBuilderResults = JmmSymbolTableBuilder.build(rootNode);
        SymbolTable table = symbolTableBuilderResults.table();
        List<Report> reports = symbolTableBuilderResults.reports();
        return new JmmSemanticsResult(parserResult, table, reports);
    }

    /**
     * Runs all semantic analysis passes over the AST.
     *
     * Passes are executed sequentially. If any pass produces at least one error report,
     * execution stops immediately and the accumulated reports are returned.
     * Unhandled exceptions within a pass are caught and recorded as error reports
     * rather than crashing the compiler.
     *
     * @param semanticsResult the result of symbol table construction.
     * @return a {@link JmmSemanticsResult} with all accumulated reports.
     */
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
                // Stop early on errors to avoid cascading false positives in later passes
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
