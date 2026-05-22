package core.parser;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;

import static pt.up.fe.comp.cp1.core.parser.RulesNames.*;

public class CallsParserTest extends JmmTestEnv {

    public CallsParserTest() {
        super("", "");
    }

    // =========================
    // CORE 3.1.4 - NEW EXPRESSIONS
    // =========================

    @Test
    public void testNewObjectSimple() {
        parseSnippet("new Foo()", EXPRESSION);
    }

    @Test
    public void testNewObjectAssigned() {
        parseSnippet("""
                void foo() {
                    a = new Foo();
                }
                """, METHOD);
    }

    @Test
    public void testNewObjectInsideReturn() {
        parseSnippet("""
                Foo foo() {
                    return new Foo();
                }
                """, METHOD);
    }

    @Test
    public void testNewObjectInsideArguments() {
        parseSnippet("a.foo(new Foo())", EXPRESSION);
    }

    // =========================
    // CORE 3.1.4 - CALL EXPRESSIONS
    // =========================

    @Test
    public void testCallOnIdentifierNoArgs() {
        parseSnippet("a.foo()", EXPRESSION);
    }

    @Test
    public void testCallOnThisNoArgs() {
        parseSnippet("this.foo()", EXPRESSION);
    }

    @Test
    public void testCallOnClassIdNoArgs() {
        parseSnippet("Foo.bar()", EXPRESSION);
    }

    @Test
    public void testCallOnIdentifierOneArg() {
        parseSnippet("a.foo(1)", EXPRESSION);
    }

    @Test
    public void testCallOnIdentifierManyArgs() {
        parseSnippet("a.foo(1, true, b)", EXPRESSION);
    }

    @Test
    public void testCallOnThisManyArgs() {
        parseSnippet("this.foo(a, b + 1, !c)", EXPRESSION);
    }

    @Test
    public void testCallOnImportedStyleTarget() {
        parseSnippet("io.println(10)", EXPRESSION);
    }

    @Test
    public void testCallWithParenthesizedArguments() {
        parseSnippet("a.foo((b + c), (d))", EXPRESSION);
    }

    @Test
    public void testNestedCallAsArgument() {
        parseSnippet("a.foo(b.bar())", EXPRESSION);
    }

    @Test
    public void testCallAfterNewObject() {
        parseSnippet("new Foo().bar()", EXPRESSION);
    }

    @Test
    public void testChainedCalls() {
        parseSnippet("a.foo().bar().baz()", EXPRESSION);
    }

    @Test
    public void testCallWithComplexArgumentExpression() {
        parseSnippet("a.foo((b + c) * d, e && f, !g)", EXPRESSION);
    }

    // =========================
    // CORE 3.1.4 - EXPRESSION STATEMENTS
    // =========================

    @Test
    public void testExpressionStatementSimpleCall() {
        parseSnippet("""
                void foo() {
                    a.bar();
                }
                """, METHOD);
    }

    @Test
    public void testExpressionStatementCallOnThis() {
        parseSnippet("""
                void foo() {
                    this.bar();
                }
                """, METHOD);
    }

    @Test
    public void testExpressionStatementCallChain() {
        parseSnippet("""
                void foo() {
                    a.b().c();
                }
                """, METHOD);
    }

    @Test
    public void testExpressionStatementNewAndCall() {
        parseSnippet("""
                void foo() {
                    new Foo().bar();
                }
                """, METHOD);
    }

    @Test
    public void testCallUsedInAssignment() {
        parseSnippet("""
                void foo() {
                    a = b.bar();
                }
                """, METHOD);
    }

    @Test
    public void testCallUsedInReturn() {
        parseSnippet("""
                int foo() {
                    return a.bar();
                }
                """, METHOD);
    }

    @Test
    public void testProgramWithImportedCallAndLocalCall() {
        parseSnippet("""
                package p;
                import util.io;

                class A {
                    int x;

                    int bar() {
                        return 1;
                    }

                    void foo() {
                        io.println(this.bar());
                    }
                }
                """);
    }

    // =========================
    // EXTENSIONS 3.2.4 - NEW WITH ARGUMENTS
    // =========================

    @Test
    public void testNewObjectWithOneArgument() {
        parseSnippet("new Foo(1)", EXPRESSION);
    }

    @Test
    public void testNewObjectWithManyArguments() {
        parseSnippet("new Foo(1, true, a)", EXPRESSION);
    }

    @Test
    public void testNewObjectWithComplexArguments() {
        parseSnippet("new Foo(a + b, !c, this)", EXPRESSION);
    }

    @Test
    public void testNewObjectWithArgumentsAssigned() {
        parseSnippet("""
                void foo() {
                    a = new Foo(1, 2);
                }
                """, METHOD);
    }

    @Test
    public void testCallOverNewObjectWithArguments() {
        parseSnippet("new Foo(1, 2).bar()", EXPRESSION);
    }

    // =========================
    // EXTENSIONS 3.2.4 - IMPLICIT THIS
    // =========================

    @Test
    public void testImplicitThisCallNoArgs() {
        parseSnippet("""
                void foo() {
                    bar();
                }
                """, METHOD);
    }

    @Test
    public void testImplicitThisCallWithOneArg() {
        parseSnippet("""
                void foo() {
                    bar(1);
                }
                """, METHOD);
    }

    @Test
    public void testImplicitThisCallWithManyArgs() {
        parseSnippet("""
                void foo() {
                    bar(a, b + 1, !c);
                }
                """, METHOD);
    }

    @Test
    public void testImplicitThisCallInAssignment() {
        parseSnippet("""
                void foo() {
                    a = bar();
                }
                """, METHOD);
    }

    @Test
    public void testImplicitThisCallInReturn() {
        parseSnippet("""
                int foo() {
                    return bar();
                }
                """, METHOD);
    }

    @Test
    public void testImplicitThisNestedAsArgument() {
        parseSnippet("""
                void foo() {
                    baz(bar());
                }
                """, METHOD);
    }

    // =========================
    // BORDERLINE / NEGATIVE
    // =========================

    @Test
    public void testInvalidNewWithoutClassName() {
        parseSnippetWithErrors("new ()", EXPRESSION);
    }

    @Test
    public void testInvalidNewWithoutParentheses() {
        parseSnippetWithErrors("new Foo", EXPRESSION);
    }

    @Test
    public void testInvalidNewMissingClosingParen() {
        parseSnippetWithErrors("new Foo(", EXPRESSION);
    }

    @Test
    public void testInvalidCallWithoutMethodName() {
        parseSnippetWithErrors("a.()", EXPRESSION);
    }

    @Test
    public void testInvalidCallWithoutOpeningParen() {
        parseSnippetWithErrors("a.foo)", EXPRESSION);
    }

    @Test
    public void testInvalidCallWithoutClosingParen() {
        parseSnippetWithErrors("a.foo(", EXPRESSION);
    }

    @Test
    public void testInvalidCallWithTrailingComma() {
        parseSnippetWithErrors("a.foo(1,)", EXPRESSION);
    }

    @Test
    public void testInvalidCallWithMissingArgumentBetweenCommas() {
        parseSnippetWithErrors("a.foo(1,,2)", EXPRESSION);
    }

    @Test
    public void testInvalidExpressionStatementMissingSemicolon() {
        parseSnippetWithErrors("""
                void foo() {
                    a.bar()
                }
                """, METHOD);
    }

    @Test
    public void testInvalidExpressionStatementNewCallMissingSemicolon() {
        parseSnippetWithErrors("""
                void foo() {
                    new Foo().bar()
                }
                """, METHOD);
    }

    @Test
    public void testInvalidCallChainEndingWithDot() {
        parseSnippetWithErrors("a.foo().", EXPRESSION);
    }

    @Test
    public void testInvalidNewWithTrailingCommaInArgs() {
        parseSnippetWithErrors("new Foo(1,)", EXPRESSION);
    }

    @Test
    public void testInvalidNewWithMissingArgumentBetweenCommas() {
        parseSnippetWithErrors("new Foo(1,,2)", EXPRESSION);
    }

    @Test
    public void testInvalidThisWithoutMethodInCallContext() {
        parseSnippetWithErrors("this.()", EXPRESSION);
    }
}