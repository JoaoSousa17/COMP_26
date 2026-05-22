package pt.up.fe.comp2026.parser;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ListTokenSource;
import pt.up.fe.comp.jmm.ast.antlr.AntlrParser;
import pt.up.fe.comp.jmm.ast.Kind;
import pt.up.fe.comp.jmm.lexer.JmmLexerResult;
import pt.up.fe.comp.jmm.parser.JmmParser;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2026.JavammParser;
import pt.up.fe.comp2026.jmm.ast.JmmKind;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Implementation of the Java-- parser.
 *
 * Wraps the ANTLR-generated {@link JavammParser} and delegates AST construction
 * to {@link AntlrParser}. After parsing, performs a structural validation pass
 * to detect for-loops that declare an init clause but omit the update clause,
 * which is not expressible as a grammar constraint alone.
 */
public class JmmParserImpl implements JmmParser {

    /** Returns the default grammar rule used as the parse entry point. */
    @Override
    public String getDefaultRule() {
        return "program";
    }

    /**
     * Parses the token stream produced by the lexer into an AST.
     *
     * Combines reports from both the lexer and parser phases. If parsing succeeds
     * and the starting rule is not a single statement, an additional structural
     * check is run to validate for-loop init/update consistency.
     * Any unhandled exception during parsing is recorded as a syntactic error.
     *
     * @param lexerResult  the result of the lexing phase, including tokens and lexer reports.
     * @param startingRule the grammar rule to use as the parse entry point.
     * @param config       compiler configuration options.
     * @return a {@link JmmParserResult} containing the root AST node and all reports.
     */
    @Override
    public JmmParserResult parse(JmmLexerResult lexerResult, String startingRule, Map<String, String> config) {
        try {
            var tokenSource = new ListTokenSource(lexerResult.tokens());
            var tokens = new CommonTokenStream(tokenSource);
            var parser = new JavammParser(tokens);

            Function<String, Kind> stringToKind = s -> JmmKind.fromString(s)
                    .orElseThrow(() -> new RuntimeException("Could not convert kind: " + s));
            var antlrParser = new AntlrParser(stringToKind, config);
            var r = antlrParser.parse(parser, startingRule);

            // Merge lexer and parser reports
            var allReports = new ArrayList<Report>();
            allReports.addAll(lexerResult.reports());
            allReports.addAll(r.reports());

            // Structural for-loop check (skipped for single-statement parse entries)
            if (r.rootNode() != null && !"stmt".equals(startingRule)) {
                checkForLoopsWithInitButNoUpdate(r.rootNode(), allReports);
            }

            return new JmmParserResult(r.rootNode(), allReports, r.config());
        } catch (Exception e) {
            return JmmParserResult.newError(
                    Report.newError(Stage.SYNTATIC, -1, -1, "Exception during parsing", e), config);
        }
    }

    /**
     * Recursively checks all for-loop nodes in the AST for a missing update clause.
     *
     * A for-loop that declares an init clause ({@code ForInit}) but omits the
     * update clause ({@code ForUpdate}) is a syntactic error in Java--, since
     * the grammar allows both to be optional independently but the language
     * semantics require them to appear together.
     *
     * @param node    the current AST node being inspected.
     * @param reports the report list to append errors to.
     */
    private void checkForLoopsWithInitButNoUpdate(pt.up.fe.comp.jmm.ast.JmmNode node, List<Report> reports) {
        if (JmmKind.FOR_STMT.check(node)) {
            boolean hasInit   = node.getChildren().stream().anyMatch(c -> JmmKind.FOR_INIT.check(c));
            boolean hasUpdate = node.getChildren().stream().anyMatch(c -> JmmKind.FOR_UPDATE.check(c));
            if (hasInit && !hasUpdate) {
                reports.add(Report.newError(
                        Stage.SYNTATIC,
                        node.getLine(),
                        node.getColumn(),
                        "For loop with initialization must have an update assignment",
                        null));
            }
        }
        for (var child : node.getChildren()) {
            checkForLoopsWithInitButNoUpdate(child, reports);
        }
    }
}
