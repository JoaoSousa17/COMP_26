package core.parser;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;

import static pt.up.fe.comp.cp1.core.parser.RulesNames.*;

public class StatementsParserTest extends JmmTestEnv {

    public StatementsParserTest() {
        super("", "");
    }

    // =========================
    // CORE 3.1.2 - STATEMENTS
    // =========================

    @Test
    public void testIfElseSimple() {
        parseSnippet("""
                void foo() {
                    if (true) return;
                    else return;
                }
                """, METHOD);
    }

    @Test
    public void testIfElseWithBlocks() {
        parseSnippet("""
                void foo() {
                    if (true) {
                        return;
                    } else {
                        return;
                    }
                }
                """, METHOD);
    }

    @Test
    public void testIfElseNested() {
        parseSnippet("""
                void foo() {
                    if (true)
                        if (false) return;
                        else return;
                    else
                        return;
                }
                """, METHOD);
    }

    @Test
    public void testWhileSimple() {
        parseSnippet("""
                void foo() {
                    while (true) return;
                }
                """, METHOD);
    }

    @Test
    public void testWhileWithBlock() {
        parseSnippet("""
                void foo() {
                    while (true) {
                        return;
                    }
                }
                """, METHOD);
    }

    @Test
    public void testNestedWhiles() {
        parseSnippet("""
                void foo() {
                    while (true) {
                        while (false) {
                            return;
                        }
                    }
                }
                """, METHOD);
    }

    @Test
    public void testReturnWithoutExpressionInVoidMethod() {
        parseSnippet("""
                void foo() {
                    return;
                }
                """, METHOD);
    }

    @Test
    public void testReturnWithExpressionInIntMethod() {
        parseSnippet("""
                int foo() {
                    return 1;
                }
                """, METHOD);
    }

    @Test
    public void testReturnWithBooleanExpression() {
        parseSnippet("""
                boolean foo() {
                    return true;
                }
                """, METHOD);
    }

    @Test
    public void testMultipleReturnsInsideIfElse() {
        parseSnippet("""
                int foo() {
                    if (true) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
                """, METHOD);
    }

    @Test
    public void testEmptyCompoundStatement() {
        parseSnippet("""
                void foo() {
                    {}
                }
                """, METHOD);
    }

    @Test
    public void testNestedCompoundStatements() {
        parseSnippet("""
                void foo() {
                    {
                        {
                            return;
                        }
                    }
                }
                """, METHOD);
    }

    @Test
    public void testCompoundStatementWithSeveralStatements() {
        parseSnippet("""
                void foo() {
                    {
                        a = 1;
                        b = 2;
                        return;
                    }
                }
                """, METHOD);
    }

    @Test
    public void testWhileContainingIfElse() {
        parseSnippet("""
                void foo() {
                    while (true) {
                        if (false) return;
                        else return;
                    }
                }
                """, METHOD);
    }

    @Test
    public void testIfElseContainingWhile() {
        parseSnippet("""
                void foo() {
                    if (true) {
                        while (false) return;
                    } else {
                        return;
                    }
                }
                """, METHOD);
    }

    // =========================
    // EXTENSIONS 3.2.2 - STATEMENTS
    // =========================

    @Test
    public void testIfWithoutElseSimple() {
        parseSnippet("""
                void foo() {
                    if (true) return;
                }
                """, METHOD);
    }

    @Test
    public void testIfWithoutElseWithBlock() {
        parseSnippet("""
                void foo() {
                    if (true) {
                        return;
                    }
                }
                """, METHOD);
    }

    @Test
    public void testNestedIfWithoutElse() {
        parseSnippet("""
                void foo() {
                    if (true)
                        if (false)
                            return;
                }
                """, METHOD);
    }

    @Test
    public void testForLoopSimple() {
        parseSnippet("""
                void foo() {
                    for (i = 0; true; i = 1) return;
                }
                """, METHOD);
    }

    @Test
    public void testForLoopWithBlock() {
        parseSnippet("""
                void foo() {
                    for (i = 0; true; i = 1) {
                        return;
                    }
                }
                """, METHOD);
    }

    @Test
    public void testForLoopWithComparisonCondition() {
        parseSnippet("""
                void foo() {
                    for (i = 0; i < 10; i = i + 1) {
                        return;
                    }
                }
                """, METHOD);
    }

    @Test
    public void testNestedForLoop() {
        parseSnippet("""
                void foo() {
                    for (i = 0; true; i = 1) {
                        for (j = 0; false; j = 1) {
                            return;
                        }
                    }
                }
                """, METHOD);
    }

    @Test
    public void testDoWhileSimple() {
        parseSnippet("""
                void foo() {
                    do return;
                    while (true);
                }
                """, METHOD);
    }

    @Test
    public void testDoWhileWithBlock() {
        parseSnippet("""
                void foo() {
                    do {
                        return;
                    } while (true);
                }
                """, METHOD);
    }

    @Test
    public void testNestedDoWhile() {
        parseSnippet("""
                void foo() {
                    do {
                        do {
                            return;
                        } while (false);
                    } while (true);
                }
                """, METHOD);
    }

    @Test
    public void testDoWhileInsideIf() {
        parseSnippet("""
                void foo() {
                    if (true) {
                        do {
                            return;
                        } while (false);
                    }
                }
                """, METHOD);
    }

    // =========================
    // BORDERLINE / NEGATIVE
    // =========================

    @Test
    public void testInvalidIfWithoutCondition() {
        parseSnippetWithErrors("""
                void foo() {
                    if () return;
                }
                """, METHOD);
    }

    @Test
    public void testInvalidIfMissingClosingParen() {
        parseSnippetWithErrors("""
                void foo() {
                    if (true return;
                }
                """, METHOD);
    }

    @Test
    public void testInvalidIfMissingThenStatement() {
        parseSnippetWithErrors("""
                void foo() {
                    if (true) else return;
                }
                """, METHOD);
    }

    @Test
    public void testInvalidElseWithoutIf() {
        parseSnippetWithErrors("""
                void foo() {
                    else return;
                }
                """, METHOD);
    }

    @Test
    public void testInvalidWhileWithoutCondition() {
        parseSnippetWithErrors("""
                void foo() {
                    while () return;
                }
                """, METHOD);
    }

    @Test
    public void testInvalidWhileMissingStatement() {
        parseSnippetWithErrors("""
                void foo() {
                    while (true)
                }
                """, METHOD);
    }

    @Test
    public void testInvalidReturnMissingSemicolon() {
        parseSnippetWithErrors("""
                void foo() {
                    return
                }
                """, METHOD);
    }

    @Test
    public void testInvalidReturnExpressionMissingSemicolon() {
        parseSnippetWithErrors("""
                int foo() {
                    return 1
                }
                """, METHOD);
    }

    @Test
    public void testInvalidCompoundStatementMissingClosingBrace() {
        parseSnippetWithErrors("""
                void foo() {
                    {
                        return;
                }
                """, METHOD);
    }

    @Test
    public void testInvalidForMissingFirstSemicolon() {
        parseSnippetWithErrors("""
                void foo() {
                    for (i = 0 true; i = 1) return;
                }
                """, METHOD);
    }

    @Test
    public void testInvalidForMissingSecondSemicolon() {
        parseSnippetWithErrors("""
                void foo() {
                    for (i = 0; true i = 1) return;
                }
                """, METHOD);
    }

    @Test
    public void testInvalidForMissingClosingParen() {
        parseSnippetWithErrors("""
                void foo() {
                    for (i = 0; true; i = 1 return;
                }
                """, METHOD);
    }

    @Test
    public void testInvalidForMissingBody() {
        parseSnippetWithErrors("""
                void foo() {
                    for (i = 0; true; i = 1)
                }
                """, METHOD);
    }

    @Test
    public void testInvalidDoWhileMissingWhilePart() {
        parseSnippetWithErrors("""
                void foo() {
                    do {
                        return;
                    }
                }
                """, METHOD);
    }

    @Test
    public void testInvalidDoWhileMissingCondition() {
        parseSnippetWithErrors("""
                void foo() {
                    do return; while ();
                }
                """, METHOD);
    }

    @Test
    public void testInvalidDoWhileMissingSemicolonAfterCondition() {
        parseSnippetWithErrors("""
                void foo() {
                    do return; while (true)
                }
                """, METHOD);
    }

    @Test
    public void testInvalidDoWhileMissingBody() {
        parseSnippetWithErrors("""
                void foo() {
                    do while (true);
                }
                """, METHOD);
    }
}