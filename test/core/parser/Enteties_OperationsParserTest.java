package core.parser;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;

import static pt.up.fe.comp.cp1.core.parser.RulesNames.*;

public class Enteties_OperationsParserTest extends JmmTestEnv {

    public Enteties_OperationsParserTest() {
        super("", "");
    }

    // =========================
    // CORE 3.1.3 - EXPRESSIONS
    // =========================

    @Test
    public void testIntLiteralZero() {
        parseSnippet("0", EXPRESSION);
    }

    @Test
    public void testIntLiteralPositive() {
        parseSnippet("123", EXPRESSION);
    }

    @Test
    public void testBooleanTrue() {
        parseSnippet("true", EXPRESSION);
    }

    @Test
    public void testBooleanFalse() {
        parseSnippet("false", EXPRESSION);
    }

    @Test
    public void testIdentifierExpr() {
        parseSnippet("a", EXPRESSION);
    }

    @Test
    public void testThisExpr() {
        parseSnippet("this", EXPRESSION);
    }

    @Test
    public void testParenthesizedExpr() {
        parseSnippet("(1)", EXPRESSION);
    }

    @Test
    public void testNestedParenthesizedExpr() {
        parseSnippet("((a))", EXPRESSION);
    }

    @Test
    public void testUnaryNot() {
        parseSnippet("!a", EXPRESSION);
    }

    @Test
    public void testUnaryNotParenthesized() {
        parseSnippet("!(a < 10)", EXPRESSION);
    }

    @Test
    public void testAddExpr() {
        parseSnippet("a + b", EXPRESSION);
    }

    @Test
    public void testSubExpr() {
        parseSnippet("a - b", EXPRESSION);
    }

    @Test
    public void testMulExpr() {
        parseSnippet("a * b", EXPRESSION);
    }

    @Test
    public void testDivExpr() {
        parseSnippet("a / b", EXPRESSION);
    }

    @Test
    public void testLessThanExpr() {
        parseSnippet("a < b", EXPRESSION);
    }

    @Test
    public void testAndExpr() {
        parseSnippet("a && b", EXPRESSION);
    }

    @Test
    public void testChainedArithmetic() {
        parseSnippet("a + b - c", EXPRESSION);
    }

    @Test
    public void testMulHasPriorityOverAdd() {
        parseSnippet("a + b * c", EXPRESSION);
    }

    @Test
    public void testParenthesesOverridePriority() {
        parseSnippet("(a + b) * c", EXPRESSION);
    }

    @Test
    public void testRelationalWithArithmetic() {
        parseSnippet("a + b < c * d", EXPRESSION);
    }

    @Test
    public void testLogicalAndWithRelational() {
        parseSnippet("a < b && c < d", EXPRESSION);
    }

    @Test
    public void testUnaryNotWithAnd() {
        parseSnippet("!a && b", EXPRESSION);
    }

    @Test
    public void testComplexCoreExpression() {
        parseSnippet("!(a < b) && (c + d * e < f)", EXPRESSION);
    }

    // =========================
    // CORE 3.1.3 - ASSIGNMENTS / ENTITY ACCESS
    // =========================

    @Test
    public void testSimpleAssignmentStatement() {
        parseSnippet("""
                void foo() {
                    a = 10;
                }
                """, METHOD);
    }

    @Test
    public void testAssignmentFromIdentifier() {
        parseSnippet("""
                void foo() {
                    a = b;
                }
                """, METHOD);
    }

    @Test
    public void testAssignmentFromThis() {
        parseSnippet("""
                void foo() {
                    a = this;
                }
                """, METHOD);
    }

    @Test
    public void testAssignmentWithArithmeticExpression() {
        parseSnippet("""
                void foo() {
                    a = b + c * d;
                }
                """, METHOD);
    }

    @Test
    public void testAssignmentWithLogicalExpression() {
        parseSnippet("""
                void foo() {
                    a = b < c && !d;
                }
                """, METHOD);
    }

    @Test
    public void testFieldAndLocalLikeIdentifiersInExpressions() {
        parseSnippet("""
                package p;
                class A {
                    int field;
                    int other;

                    int foo(int param) {
                        int local;
                        local = field + other + param;
                        return local;
                    }
                }
                """);
    }

    // =========================
    // EXTENSIONS 3.2.3 - ADDITIONAL ARITHMETIC
    // =========================

    @Test
    public void testUnaryPlusLiteral() {
        parseSnippet("+1", EXPRESSION);
    }

    @Test
    public void testUnaryMinusLiteral() {
        parseSnippet("-1", EXPRESSION);
    }

    @Test
    public void testUnaryPlusIdentifier() {
        parseSnippet("+a", EXPRESSION);
    }

    @Test
    public void testUnaryMinusIdentifier() {
        parseSnippet("-a", EXPRESSION);
    }

    @Test
    public void testPrefixIncrement() {
        parseSnippet("++i", EXPRESSION);
    }

    @Test
    public void testPrefixDecrement() {
        parseSnippet("--j", EXPRESSION);
    }

    @Test
    public void testModuloExpr() {
        parseSnippet("a % b", EXPRESSION);
    }

    @Test
    public void testModuloWithOtherArithmetic() {
        parseSnippet("a + b % c", EXPRESSION);
    }

    @Test
    public void testUnaryMinusWithMultiplication() {
        parseSnippet("-a * b", EXPRESSION);
    }

    @Test
    public void testPrefixIncrementInsideAssignment() {
        parseSnippet("""
                void foo() {
                    a = ++i;
                }
                """, METHOD);
    }

    @Test
    public void testPrefixDecrementInsideAssignment() {
        parseSnippet("""
                void foo() {
                    a = --i;
                }
                """, METHOD);
    }

    // =========================
    // EXTENSIONS 3.2.3 - ADDITIONAL LOGICAL
    // =========================

    @Test
    public void testEqualsExpr() {
        parseSnippet("a == b", EXPRESSION);
    }

    @Test
    public void testNotEqualsExpr() {
        parseSnippet("a != b", EXPRESSION);
    }

    @Test
    public void testGreaterThanExpr() {
        parseSnippet("a > b", EXPRESSION);
    }

    @Test
    public void testGreaterOrEqualExpr() {
        parseSnippet("a >= b", EXPRESSION);
    }

    @Test
    public void testLessOrEqualExpr() {
        parseSnippet("a <= b", EXPRESSION);
    }

    @Test
    public void testOrExpr() {
        parseSnippet("a || b", EXPRESSION);
    }

    @Test
    public void testOrAndPrecedenceLikeExpression() {
        parseSnippet("a || b && c", EXPRESSION);
    }

    @Test
    public void testEqualityWithRelational() {
        parseSnippet("a + b == c - d", EXPRESSION);
    }

    @Test
    public void testComplexExtendedExpression() {
        parseSnippet("!(a >= b) || ++i < j % 2", EXPRESSION);
    }

    // =========================
    // BORDERLINE / NEGATIVE
    // =========================

    @Test
    public void testInvalidLeadingZeroLiteral() {
        parseSnippetWithErrors("010", EXPRESSION);
    }

    @Test
    public void testInvalidUnaryNotWithoutOperand() {
        parseSnippetWithErrors("!", EXPRESSION);
    }

    @Test
    public void testInvalidBinaryAddWithoutRightOperand() {
        parseSnippetWithErrors("a +", EXPRESSION);
    }

    @Test
    public void testInvalidBinaryMulWithoutLeftOperand() {
        parseSnippetWithErrors("* a", EXPRESSION);
    }

    @Test
    public void testInvalidLessThanWithoutRightOperand() {
        parseSnippetWithErrors("a <", EXPRESSION);
    }

    @Test
    public void testInvalidAndWithoutRightOperand() {
        parseSnippetWithErrors("a &&", EXPRESSION);
    }

    @Test
    public void testInvalidUnclosedParenthesis() {
        parseSnippetWithErrors("(a + b", EXPRESSION);
    }

    @Test
    public void testInvalidExtraClosingParenthesis() {
        parseSnippetWithErrors("a + b)", EXPRESSION);
    }

    @Test
    public void testInvalidEmptyParentheses() {
        parseSnippetWithErrors("()", EXPRESSION);
    }

    @Test
    public void testInvalidAssignmentWithoutExpression() {
        parseSnippetWithErrors("""
                void foo() {
                    a = ;
                }
                """, METHOD);
    }

    @Test
    public void testInvalidAssignmentMissingSemicolon() {
        parseSnippetWithErrors("""
                void foo() {
                    a = 10
                }
                """, METHOD);
    }

    @Test
    public void testInvalidUnaryPlusWithoutOperand() {
        parseSnippetWithErrors("+", EXPRESSION);
    }

    @Test
    public void testInvalidUnaryMinusWithoutOperand() {
        parseSnippetWithErrors("-", EXPRESSION);
    }

    @Test
    public void testInvalidPrefixIncrementWithoutOperand() {
        parseSnippetWithErrors("++", EXPRESSION);
    }

    @Test
    public void testInvalidPrefixDecrementWithoutOperand() {
        parseSnippetWithErrors("--", EXPRESSION);
    }

    @Test
    public void testInvalidModuloWithoutRightOperand() {
        parseSnippetWithErrors("a %", EXPRESSION);
    }

    @Test
    public void testInvalidEqualsWithoutRightOperand() {
        parseSnippetWithErrors("a ==", EXPRESSION);
    }

    @Test
    public void testInvalidNotEqualsWithoutRightOperand() {
        parseSnippetWithErrors("a !=", EXPRESSION);
    }

    @Test
    public void testInvalidGreaterOrEqualWithoutRightOperand() {
        parseSnippetWithErrors("a >=", EXPRESSION);
    }

    @Test
    public void testInvalidLessOrEqualWithoutRightOperand() {
        parseSnippetWithErrors("a <=", EXPRESSION);
    }

    @Test
    public void testInvalidOrWithoutRightOperand() {
        parseSnippetWithErrors("a ||", EXPRESSION);
    }

    @Test
    public void testInvalidPostIncrementNotRequested() {
        parseSnippetWithErrors("i++", EXPRESSION);
    }

    @Test
    public void testInvalidPostDecrementNotRequested() {
        parseSnippetWithErrors("i--", EXPRESSION);
    }

    @Test
    public void testInvalidMixedOperatorSequence() {
        parseSnippetWithErrors("a + * b", EXPRESSION);
    }
}