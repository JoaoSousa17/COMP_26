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

public class JmmParserImpl implements JmmParser {

    @Override
    public String getDefaultRule() {
        return "program";
    }

    @Override
    public JmmParserResult parse(JmmLexerResult lexerResult, String startingRule, Map<String, String> config) {
        try {
            var tokenSource = new ListTokenSource(lexerResult.tokens());
            var tokens = new CommonTokenStream(tokenSource);
            var parser = new JavammParser(tokens);

            Function<String, Kind> stringToKind = s -> JmmKind.fromString(s).orElseThrow(() -> new RuntimeException("Could not convert kind: " + s));
            var anltrParser = new AntlrParser(stringToKind, config);
            var r = anltrParser.parse(parser, startingRule);

            var allReports = new ArrayList<Report>();
            allReports.addAll(lexerResult.reports());
            allReports.addAll(r.reports());

            if (r.rootNode() != null && !"stmt".equals(startingRule)) {
                checkForLoopsWithInitButNoUpdate(r.rootNode(), allReports);
            }

            return new JmmParserResult(r.rootNode(), allReports, r.config());
        } catch (Exception e) {
            return JmmParserResult.newError(Report.newError(Stage.SYNTATIC, -1, -1, "Exception during parsing", e), config);
        }
    }

    private void checkForLoopsWithInitButNoUpdate(pt.up.fe.comp.jmm.ast.JmmNode node, List<Report> reports) {
        if (JmmKind.FOR_STMT.check(node)) {
            boolean hasInit = node.getChildren().stream().anyMatch(c -> JmmKind.FOR_INIT.check(c));
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
