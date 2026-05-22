package core.parser;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;

import static pt.up.fe.comp.cp1.core.parser.RulesNames.*;

public class ArraysParserTest extends JmmTestEnv {

    public ArraysParserTest() {
        super("", "");
    }

    // =========================
    // CORE 3.1.5 - ARRAY TYPES
    // =========================

    @Test
    public void testIntArrayType() {
        parseSnippet("int[]", TYPE);
    }

    @Test
    public void testStringArrayTypeForMain() {
        parseSnippet("String[]", TYPE);
    }

    @Test
    public void testCustomArrayType() {
        parseSnippet("Foo[]", TYPE);
    }

    @Test
    public void testFieldWithArrayType() {
        parseSnippet("""
                package p;
                class A {
                    int[] values;
                }
                """);
    }

    @Test
    public void testMethodReturningArrayType() {
        parseSnippet("""
                package p;
                class A {
                    int[] foo() {}
                }
                """);
    }

    @Test
    public void testMethodWithArrayParameter() {
        parseSnippet("""
                package p;
                class A {
                    void foo(int[] values) {}
                }
                """);
    }

    @Test
    public void testMethodWithLocalArrayVariable() {
        parseSnippet("""
                package p;
                class A {
                    void foo() {
                        int[] values;
                    }
                }
                """);
    }

    // =========================
    // CORE 3.1.5 - NEW ARRAY
    // =========================

    @Test
    public void testNewIntArrayLiteralSize() {
        parseSnippet("new int[10]", EXPRESSION);
    }

    @Test
    public void testNewIntArrayIdentifierSize() {
        parseSnippet("new int[size]", EXPRESSION);
    }

    @Test
    public void testNewIntArrayComplexSize() {
        parseSnippet("new int[a + b * 2]", EXPRESSION);
    }

    @Test
    public void testAssignNewIntArray() {
        parseSnippet("""
                void foo() {
                    a = new int[10];
                }
                """, METHOD);
    }

    @Test
    public void testReturnNewIntArray() {
        parseSnippet("""
                int[] foo() {
                    return new int[10];
                }
                """, METHOD);
    }

    @Test
    public void testArrayFieldInitializerThroughAssignmentInMethod() {
        parseSnippet("""
                package p;
                class A {
                    int[] a;
                    void foo() {
                        a = new int[10];
                    }
                }
                """);
    }

    // =========================
    // CORE 3.1.5 - ARRAY LOAD
    // =========================

    @Test
    public void testSimpleArrayAccess() {
        parseSnippet("a[0]", EXPRESSION);
    }

    @Test
    public void testArrayAccessWithIdentifierIndex() {
        parseSnippet("a[i]", EXPRESSION);
    }

    @Test
    public void testArrayAccessWithExpressionIndex() {
        parseSnippet("a[i + 1]", EXPRESSION);
    }

    @Test
    public void testArrayAccessOnNewArray() {
        parseSnippet("new int[10][0]", EXPRESSION);
    }

    @Test
    public void testArrayAccessInsideAssignment() {
        parseSnippet("""
                void foo() {
                    x = a[i];
                }
                """, METHOD);
    }

    @Test
    public void testArrayAccessInsideReturn() {
        parseSnippet("""
                int foo() {
                    return a[0];
                }
                """, METHOD);
    }

    @Test
    public void testArrayAccessCombinedWithArithmetic() {
        parseSnippet("a[0] + b[1]", EXPRESSION);
    }

    // =========================
    // CORE 3.1.5 - ARRAY STORE
    // =========================

    @Test
    public void testSimpleArrayStore() {
        parseSnippet("""
                void foo() {
                    a[0] = 1;
                }
                """, METHOD);
    }

    @Test
    public void testArrayStoreWithIdentifierIndex() {
        parseSnippet("""
                void foo() {
                    a[i] = x;
                }
                """, METHOD);
    }

    @Test
    public void testArrayStoreWithExpressionIndex() {
        parseSnippet("""
                void foo() {
                    a[i + 1] = x;
                }
                """, METHOD);
    }

    @Test
    public void testArrayStoreWithExpressionValue() {
        parseSnippet("""
                void foo() {
                    a[0] = x + y * 2;
                }
                """, METHOD);
    }

    @Test
    public void testArrayStoreUsingLoadedValue() {
        parseSnippet("""
                void foo() {
                    a[0] = b[1];
                }
                """, METHOD);
    }

    @Test
    public void testProgramWithArrayNewLoadAndStore() {
        parseSnippet("""
                package p;
                class A {
                    int[] data;

                    void foo(int i) {
                        int x;
                        data = new int[10];
                        data[0] = 1;
                        data[i] = data[0];
                        x = data[i];
                    }
                }
                """);
    }

    // =========================
    // EXTENSIONS 3.2.5 - ARRAY INITIALIZER
    // =========================

    @Test
    public void testEmptyArrayInitializer() {
        parseSnippet("new int[]{}", EXPRESSION);
    }

    @Test
    public void testSingleElementArrayInitializer() {
        parseSnippet("new int[]{1}", EXPRESSION);
    }

    @Test
    public void testMultipleElementArrayInitializer() {
        parseSnippet("new int[]{1, 2, 3}", EXPRESSION);
    }

    @Test
    public void testArrayInitializerWithExpressions() {
        parseSnippet("new int[]{a, b + 1, c * 2}", EXPRESSION);
    }

    @Test
    public void testAssignArrayInitializer() {
        parseSnippet("""
                void foo() {
                    a = new int[]{1, 2, 3};
                }
                """, METHOD);
    }

    @Test
    public void testReturnArrayInitializer() {
        parseSnippet("""
                int[] foo() {
                    return new int[]{1, 2, 3};
                }
                """, METHOD);
    }

    @Test
    public void testArrayInitializerInsideProgram() {
        parseSnippet("""
                package p;
                class A {
                    int[] data;

                    void foo() {
                        data = new int[]{1, 2, 3};
                    }
                }
                """);
    }

    // =========================
    // EXTENSIONS 3.2.5 - MULTIDIMENSIONAL ARRAYS
    // =========================

    @Test
    public void testTwoDimensionalIntArrayType() {
        parseSnippet("int[][]", TYPE);
    }

    @Test
    public void testThreeDimensionalIntArrayType() {
        parseSnippet("int[][][]", TYPE);
    }

    @Test
    public void testTwoDimensionalCustomArrayType() {
        parseSnippet("Foo[][]", TYPE);
    }

    @Test
    public void testFieldWithTwoDimensionalArrayType() {
        parseSnippet("""
                package p;
                class A {
                    int[][] matrix;
                }
                """);
    }

    @Test
    public void testMethodWithTwoDimensionalArrayParameter() {
        parseSnippet("""
                package p;
                class A {
                    void foo(int[][] matrix) {}
                }
                """);
    }

    @Test
    public void testLocalVariableWithThreeDimensionalArrayType() {
        parseSnippet("""
                package p;
                class A {
                    void foo() {
                        int[][][] cube;
                    }
                }
                """);
    }

    @Test
    public void testMultidimensionalArrayAccess() {
        parseSnippet("a[0][1]", EXPRESSION);
    }

    @Test
    public void testThreeDimensionalArrayAccess() {
        parseSnippet("a[0][1][2]", EXPRESSION);
    }

    @Test
    public void testMultidimensionalArrayAccessWithExpressions() {
        parseSnippet("a[i + 1][j * 2]", EXPRESSION);
    }

    @Test
    public void testMultidimensionalArrayStore() {
        parseSnippet("""
                void foo() {
                    a[0][1] = 5;
                }
                """, METHOD);
    }

    @Test
    public void testMultidimensionalArrayStoreUsingLoad() {
        parseSnippet("""
                void foo() {
                    a[i][j] = b[0][1];
                }
                """, METHOD);
    }

    @Test
    public void testMultidimensionalArrayProgram() {
        parseSnippet("""
                package p;
                class A {
                    int[][] matrix;

                    void foo(int i, int j) {
                        int x;
                        x = matrix[i][j];
                        matrix[0][1] = x;
                    }
                }
                """);
    }

    // =========================
    // BORDERLINE / NEGATIVE
    // =========================

    @Test
    public void testInvalidArrayTypeMissingClosingBracket() {
        parseSnippetWithErrors("int[", TYPE);
    }

    @Test
    public void testInvalidArrayTypeOnlyBrackets() {
        parseSnippetWithErrors("[]", TYPE);
    }

    @Test
    public void testInvalidNewArrayWithoutSize() {
        parseSnippetWithErrors("new int[]", EXPRESSION);
    }

    @Test
    public void testInvalidNewArrayMissingClosingBracket() {
        parseSnippetWithErrors("new int[10", EXPRESSION);
    }

    @Test
    public void testInvalidNewArrayMissingExpression() {
        parseSnippetWithErrors("new int[]", EXPRESSION);
    }

    @Test
    public void testInvalidArrayAccessMissingIndex() {
        parseSnippetWithErrors("a[]", EXPRESSION);
    }

    @Test
    public void testInvalidArrayAccessMissingClosingBracket() {
        parseSnippetWithErrors("a[0", EXPRESSION);
    }

    @Test
    public void testInvalidArrayStoreMissingAssignedExpression() {
        parseSnippetWithErrors("""
                void foo() {
                    a[0] = ;
                }
                """, METHOD);
    }

    @Test
    public void testInvalidArrayStoreMissingSemicolon() {
        parseSnippetWithErrors("""
                void foo() {
                    a[0] = 1
                }
                """, METHOD);
    }

    @Test
    public void testInvalidArrayStoreMissingIndex() {
        parseSnippetWithErrors("""
                void foo() {
                    a[] = 1;
                }
                """, METHOD);
    }

    @Test
    public void testInvalidArrayInitializerMissingClosingBrace() {
        parseSnippetWithErrors("new int[]{1, 2", EXPRESSION);
    }

    @Test
    public void testInvalidArrayInitializerTrailingComma() {
        parseSnippetWithErrors("new int[]{1, 2,}", EXPRESSION);
    }

    @Test
    public void testInvalidArrayInitializerMissingElementBetweenCommas() {
        parseSnippetWithErrors("new int[]{1,,2}", EXPRESSION);
    }

    @Test
    public void testInvalidArrayInitializerWithoutOpeningBrace() {
        parseSnippetWithErrors("new int[]1,2}", EXPRESSION);
    }

    @Test
    public void testInvalidMultidimensionalTypeMalformed() {
        parseSnippetWithErrors("int[][", TYPE);
    }

    @Test
    public void testInvalidMultidimensionalAccessMissingSecondIndex() {
        parseSnippetWithErrors("a[0][]", EXPRESSION);
    }

    @Test
    public void testInvalidMultidimensionalAccessMissingBracket() {
        parseSnippetWithErrors("a[0][1", EXPRESSION);
    }
}