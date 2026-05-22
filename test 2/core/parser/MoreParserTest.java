package core.parser;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;

import static pt.up.fe.comp.cp1.core.parser.RulesNames.*;

public class MoreParserTest extends JmmTestEnv {

    public MoreParserTest() {
        super("", "");
    }

    // =========================
    // EXPRESSIONS - PRECEDENCE / MIXED COMPOSITIONS
    // =========================

    @Test
    public void testExprArithmeticLogicalRelationalChain() {
        parseSnippet("a + b * c < d && e || f", EXPRESSION);
    }

    @Test
    public void testExprUnaryRelationalArithmeticChain() {
        parseSnippet("!a && b < c + d * e", EXPRESSION);
    }

    @Test
    public void testExprManyBinaryOperators() {
        parseSnippet("a + b - c * d / e % f", EXPRESSION);
    }

    @Test
    public void testExprEqualityRelationalLogicalMix() {
        parseSnippet("a + b == c * d || e <= f && g != h", EXPRESSION);
    }

    @Test
    public void testExprNestedParenthesesWithUnary() {
        parseSnippet("!((a + b) < (c * (d + e)))", EXPRESSION);
    }

    @Test
    public void testExprPrefixUnaryChain() {
        parseSnippet("++a + --b * -c", EXPRESSION);
    }

    @Test
    public void testExprUnaryPlusMinusAndModulo() {
        parseSnippet("+a + (-b % c)", EXPRESSION);
    }

    @Test
    public void testExprRelationalCascadeWithParentheses() {
        parseSnippet("(a < b) && (c >= d) || (e == f)", EXPRESSION);
    }

    // =========================
    // CALLS + ARRAYS + OPERATIONS MIX
    // =========================

    @Test
    public void testExprArrayAccessAndCallAndArithmetic() {
        parseSnippet("a[0] + b.foo(1) * c[2]", EXPRESSION);
    }

    @Test
    public void testExprCallInsideArrayIndex() {
        parseSnippet("a[b.foo(1)]", EXPRESSION);
    }

    @Test
    public void testExprArrayAccessAfterCall() {
        parseSnippet("a.foo()[i]", EXPRESSION);
    }

    @Test
    public void testExprCallAfterArrayAccess() {
        parseSnippet("a[i].foo()", EXPRESSION);
    }

    @Test
    public void testExprCallArrayCallChain() {
        parseSnippet("a.b()[i].c()", EXPRESSION);
    }

    @Test
    public void testExprArrayAccessOnNewObjectCall() {
        parseSnippet("new Foo().bar()[0]", EXPRESSION);
    }

    @Test
    public void testExprLengthSimple() {
        parseSnippet("a.length", EXPRESSION);
    }

    @Test
    public void testExprLengthInArithmetic() {
        parseSnippet("a.length + 1", EXPRESSION);
    }

    @Test
    public void testExprLengthAfterCall() {
        parseSnippet("a.foo().length", EXPRESSION);
    }

    @Test
    public void testExprLengthAfterArrayAccess() {
        parseSnippet("a[i].length", EXPRESSION);
    }

    @Test
    public void testExprLengthInRelationalExpression() {
        parseSnippet("a.length < b.length", EXPRESSION);
    }

    @Test
    public void testExprComplexCallArrayLengthMix() {
        parseSnippet("a.b(c[0], d.length).e()[i + 1].f().length", EXPRESSION);
    }

    // =========================
    // NEW + CALLS
    // =========================

    @Test
    public void testExprNewWithArgumentsNestedCalls() {
        parseSnippet("new Foo(a.bar(), b + 1, c[0])", EXPRESSION);
    }

    @Test
    public void testExprNewThenChainedCalls() {
        parseSnippet("new Foo(1, 2).bar().baz()", EXPRESSION);
    }

    @Test
    public void testExprCallWithNewAndArrayArguments() {
        parseSnippet("a.foo(new Foo(1), new int[10], new int[]{1,2,3})", EXPRESSION);
    }

    @Test
    public void testExprImplicitThisInsideArguments() {
        parseSnippet("""
                void foo() {
                    a = bar(baz(), qux(1, 2));
                }
                """, METHOD);
    }

    @Test
    public void testExprCallChainAsStatement() {
        parseSnippet("""
                void foo() {
                    a.b().c(d.e()).f();
                }
                """, METHOD);
    }

    @Test
    public void testExprNewCallAsStatement() {
        parseSnippet("""
                void foo() {
                    new Foo(1, 2).bar(baz()).qux();
                }
                """, METHOD);
    }

    // =========================
    // ARRAYS - INITIALIZER / MULTIDIMENSIONAL / MIX
    // =========================

    @Test
    public void testExprMultidimensionalAccessThreeLevels() {
        parseSnippet("a[0][1][2]", EXPRESSION);
    }

    @Test
    public void testExprMultidimensionalAccessWithExpressions() {
        parseSnippet("a[i + 1][j * 2][k - 1]", EXPRESSION);
    }

    @Test
    public void testExprMultidimensionalAccessAfterCall() {
        parseSnippet("a.foo()[0][1]", EXPRESSION);
    }

    @Test
    public void testExprMultidimensionalAccessInArithmetic() {
        parseSnippet("a[0][1] + b[2][3]", EXPRESSION);
    }

    @Test
    public void testExprArrayInitializerWithExpressions() {
        parseSnippet("new int[]{a, b + 1, c * 2, d % 3}", EXPRESSION);
    }

    @Test
    public void testExprArrayInitializerWithCallsAndAccesses() {
        parseSnippet("new int[]{a[0], b.foo(), c.length}", EXPRESSION);
    }

    @Test
    public void testExprNestedArrayAccessWithLength() {
        parseSnippet("a[0].length + b[1][2]", EXPRESSION);
    }

    // =========================
    // STATEMENTS - MIXED / INTEGRATION-LITE
    // =========================

    @Test
    public void testMethodIfWhileForDoWithCallsAndArrays() {
        parseSnippet("""
                void foo(int[] a, int i) {
                    if (a.length > 0) {
                        while (i < a.length && check(a[i])) {
                            a[i] = value(i) + a[0];
                            i = i + 1;
                        }
                    } else {
                        do {
                            reset();
                        } while (again());
                    }

                    for (i = 0; i < a.length; i = i + 1) {
                        process(a[i]);
                    }
                }
                """, METHOD);
    }

    @Test
    public void testMethodNestedCompoundStatementsWithExpressionStatements() {
        parseSnippet("""
                void foo() {
                    {
                        {
                            a.b();
                            this.c();
                            d = e.f(g());
                        }
                    }
                }
                """, METHOD);
    }

    @Test
    public void testMethodReturnComplexExpression() {
        parseSnippet("""
                int foo(int[] a, int i) {
                    return a[i] + bar().baz()[0] * (a.length - 1);
                }
                """, METHOD);
    }

    @Test
    public void testMethodArrayStoresWithComplexIndexes() {
        parseSnippet("""
                void foo(int[][] a, int i, int j) {
                    a[i + 1][j * 2] = b().c()[0] + d.length;
                }
                """, METHOD);
    }

    @Test
    public void testMethodExpressionStatementsDifferentTargets() {
        parseSnippet("""
                void foo() {
                    this.bar();
                    a.bar();
                    A.staticCall();
                    foo();
                }
                """, METHOD);
    }

    // =========================
    // DECLARATIONS - MIXED / EDGE USEFUL CASES
    // =========================

    @Test
    public void testProgramManyImportsAndMixedMembers() {
        parseSnippet("""
                package p.q.r;
                import a.b.C;
                import x.y.Z;
                import io.println.Util;

                class A extends B {
                    int x = 1;
                    int[][] matrix;
                    Foo obj = new Foo(1, 2);

                    protected static int helper(int a, int b) {
                        return a + b;
                    }

                    int value;
                    private Foo build() {
                        return new Foo(x, value);
                    }

                    public static void main(String[] args) {
                        int i;
                        i = 0;
                    }

                    void use() {
                        obj.work(matrix[0][1], helper(x, value));
                    }
                }
                """);
    }

    @Test
    public void testProgramArbitraryPositionWithManyMemberKinds() {
        parseSnippet("""
                package p;
                class A {
                    void first() {}
                    int a = 0;
                    protected int second(int x) { return x; }
                    Foo b;
                    private static void third() {}
                    boolean c = true;
                    int[] arr;
                }
                """);
    }

    // =========================
    // NEGATIVE - TARGETED USEFUL FAILURES
    // =========================

    @Test
    public void testInvalidLengthWithArguments() {
        parseSnippetWithErrors("a.length()", EXPRESSION);
    }

    @Test
    public void testInvalidDoubleDotBeforeCall() {
        parseSnippetWithErrors("a..foo()", EXPRESSION);
    }

    @Test
    public void testInvalidCallChainWithMissingMethodName() {
        parseSnippetWithErrors("a.foo().(1)", EXPRESSION);
    }

    @Test
    public void testInvalidArrayAccessAfterDot() {
        parseSnippetWithErrors("a.[0]", EXPRESSION);
    }

    @Test
    public void testInvalidLengthTrailingDot() {
        parseSnippetWithErrors("a.length.", EXPRESSION);
    }

    @Test
    public void testInvalidNewObjectThenDotOnly() {
        parseSnippetWithErrors("new Foo().", EXPRESSION);
    }

    @Test
    public void testInvalidCallWithMissingArgumentAfterCommaInNestedContext() {
        parseSnippetWithErrors("a.foo(b(), , c())", EXPRESSION);
    }

    @Test
    public void testInvalidArrayInitializerNestedMissingElement() {
        parseSnippetWithErrors("new int[]{a[0], , c.length}", EXPRESSION);
    }

    @Test
    public void testInvalidStatementCallMissingSemicolonInNestedBlock() {
        parseSnippetWithErrors("""
                void foo() {
                    {
                        a.b()
                    }
                }
                """, METHOD);
    }

    @Test
    public void testInvalidForHeaderMissingUpdateAssignment() {
        parseSnippetWithErrors("""
                void foo() {
                    for (i = 0; i < 10) {
                        a();
                    }
                }
                """, METHOD);
    }

    @Test
    public void testInvalidDoWhileMissingSemicolonAfterNestedBlock() {
        parseSnippetWithErrors("""
                void foo() {
                    do {
                        a();
                    } while (b())
                }
                """, METHOD);
    }

    // =========================
    // PERMISSIVE GRAMMAR POSITIVE TESTS
    // =========================

    @Test
    public void testPermissiveArrayAccessOverParenthesizedExpr() {
        parseSnippet("(1 + 2)[0]", EXPRESSION);
    }

    @Test
    public void testPermissiveLengthOverParenthesizedExpr() {
        parseSnippet("(a + b).length", EXPRESSION);
    }

    @Test
    public void testPermissiveCallOverParenthesizedExpr() {
        parseSnippet("(a + b).foo()", EXPRESSION);
    }

    @Test
    public void testPermissiveArrayAccessOverCallResultChain() {
        parseSnippet("a.foo()[b.bar()][c]", EXPRESSION);
    }
}