package core.parser;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;

import static pt.up.fe.comp.cp1.core.parser.RulesNames.*;

public class DeclarationsParserTest extends JmmTestEnv {

    public DeclarationsParserTest() {
        super("", "");
    }

    // =========================
    // TYPE
    // =========================

    @Test
    public void testPrimitiveInt() {
        parseSnippet("int", TYPE);
    }

    @Test
    public void testPrimitiveBoolean() {
        parseSnippet("boolean", TYPE);
    }

    @Test
    public void testVoidType() {
        parseSnippet("void", TYPE);
    }

    @Test
    public void testCustomType() {
        parseSnippet("MyClass", TYPE);
    }

    @Test
    public void testIntArrayType() {
        parseSnippet("int[]", TYPE);
    }

    @Test
    public void testStringArrayType() {
        parseSnippet("String[]", TYPE);
    }

    @Test
    public void testCustomArrayType() {
        parseSnippet("Foo[]", TYPE);
    }

    @Test
    public void testInvalidTypeStartingWithDigit() {
        parseSnippetWithErrors("1Foo", TYPE);
    }

    @Test
    public void testInvalidMalformedArrayType() {
        parseSnippetWithErrors("int[", TYPE);
    }

    // =========================
    // PACKAGE
    // =========================

    @Test
    public void testSinglePackage() {
        parseSnippet("package bar;", PACKAGE);
    }

    @Test
    public void testMultiPackage() {
        parseSnippet("package bar.foo.org;", PACKAGE);
    }

    @Test
    public void testPackageWithUnderscoreAndDollar() {
        parseSnippet("package my_pkg.$internal.mod1;", PACKAGE);
    }

    @Test
    public void testInvalidPackageWithoutSemicolon() {
        parseSnippetWithErrors("package bar", PACKAGE);
    }

    @Test
    public void testInvalidPackageEmptySegment() {
        parseSnippetWithErrors("package bar..foo;", PACKAGE);
    }

    @Test
    public void testInvalidPackageStartsWithDot() {
        parseSnippetWithErrors("package .bar;", PACKAGE);
    }

    @Test
    public void testInvalidPackageSegmentStartingWithDigit() {
        parseSnippetWithErrors("package bar.1foo;", PACKAGE);
    }

    // =========================
    // IMPORT
    // =========================

    @Test
    public void testSingleImport() {
        parseSnippet("import bar.Foo;", IMPORT);
    }

    @Test
    public void testMultiSegmentImport() {
        parseSnippet("import bar.foo.A;", IMPORT);
    }

    @Test
    public void testImportWithMainAsIdentifier() {
        parseSnippet("import my.main.Main;", IMPORT);
    }

    @Test
    public void testImportWithUnderscoreAndDollar() {
        parseSnippet("import my_pkg.$internal.Foo1;", IMPORT);
    }

    @Test
    public void testInvalidImportWithoutSemicolon() {
        parseSnippetWithErrors("import a.b.C", IMPORT);
    }

    @Test
    public void testInvalidImportWithEmptySegment() {
        parseSnippetWithErrors("import a..C;", IMPORT);
    }

    @Test
    public void testInvalidImportEndingInDot() {
        parseSnippetWithErrors("import a.b.;", IMPORT);
    }

    @Test
    public void testInvalidImportStartsWithDot() {
        parseSnippetWithErrors("import .a.b;", IMPORT);
    }

    @Test
    public void testInvalidImportSegmentStartingWithDigit() {
        parseSnippetWithErrors("import a.1b.C;", IMPORT);
    }

    // =========================
    // PROGRAM / CLASS DECLARATION - CORE 3.1.1
    // =========================

    @Test
    public void testMinimalProgramWithClass() {
        parseSnippet("""
                package p;
                class A {}
                """);
    }

    @Test
    public void testClassWithExtends() {
        parseSnippet("""
                package p;
                class A extends B {}
                """);
    }

    @Test
    public void testClassWithSingleField() {
        parseSnippet("""
                package p;
                class A {
                    int a;
                }
                """);
    }

    @Test
    public void testClassWithMultipleFields() {
        parseSnippet("""
                package p;
                class A {
                    int a;
                    boolean b;
                    String c;
                    Foo d;
                    int[] e;
                }
                """);
    }

    @Test
    public void testClassWithSingleMethod() {
        parseSnippet("""
                package p;
                class A {
                    void foo() {}
                }
                """);
    }

    @Test
    public void testClassWithMethodParameters() {
        parseSnippet("""
                package p;
                class A {
                    int sum(int a, int b, Foo c, String[] e) {}
                }
                """);
    }

    @Test
    public void testClassWithLocalVariables() {
        parseSnippet("""
                package p;
                class A {
                    void foo() {
                        int a;
                        boolean b;
                        String s;
                        Foo f;
                        int[] arr;
                    }
                }
                """);
    }

    @Test
    public void testProgramWithImportsFieldsMethodsAndLocals() {
        parseSnippet("""
                package p.q;
                import a.b.C;
                import x.y.Z;

                class A extends B {
                    int field1;
                    String field2;
                    C field3;

                    public static void main(String[] args) {
                        int local1;
                        String local2;
                    }

                    Foo bar(int x, boolean y, String[] z) {
                        int tmp;
                    }
                }
                """);
    }

    @Test
    public void testMainMethodDeclaration() {
        parseSnippet("""
                package p;
                class A {
                    public static void main(String[] args) {}
                }
                """);
    }

    @Test
    public void testIdentifiersWithUnderscoreAndDollar() {
        parseSnippet("""
                package p;
                class $A_1 extends _B$2 {
                    int _field$1;
                    void $method_2(int $arg_3) {
                        int _local$4;
                    }
                }
                """);
    }

    @Test
    public void testCommentsAroundDeclarations() {
        parseSnippet("""
                package p;
                import a.b.C;
                /* class comment */
                class A {
                    // field comment
                    int a;
                    /* method comment */
                    void foo(
                        int x /* inline param */
                    ) {
                        // local comment
                        int y;
                    }
                }
                """);
    }

    // =========================
    // EXTENSIONS 3.2.1
    // =========================

    @Test
    public void testArbitraryPositionMethodsBeforeFields() {
        parseSnippet("""
                package p;
                class A {
                    void first() {}
                    int a;
                    void second() {}
                    boolean b;
                }
                """);
    }

    @Test
    public void testArbitraryPositionAlternatingFieldsAndMethods() {
        parseSnippet("""
                package p;
                class A {
                    int a;
                    void m1() {}
                    boolean b;
                    int m2(int x) {}
                    Foo c;
                    public static void main(String[] args) {}
                }
                """);
    }

    @Test
    public void testPrivateMethod() {
        parseSnippet("""
                package p;
                class A {
                    private void foo() {}
                }
                """);
    }

    @Test
    public void testProtectedMethod() {
        parseSnippet("""
                package p;
                class A {
                    protected int foo() {}
                }
                """);
    }

    @Test
    public void testPrivateStaticMethod() {
        parseSnippet("""
                package p;
                class A {
                    private static void foo() {}
                }
                """);
    }

    @Test
    public void testProtectedStaticMethod() {
        parseSnippet("""
                package p;
                class A {
                    protected static int foo(int a) {}
                }
                """);
    }

    @Test
    public void testFieldInitializerWithIntLiteral() {
        parseSnippet("""
                package p;
                class A {
                    int a = 0;
                }
                """);
    }

    @Test
    public void testFieldInitializerWithBooleanLiteral() {
        parseSnippet("""
                package p;
                class A {
                    boolean flag = true;
                }
                """);
    }

    @Test
    public void testMultipleFieldsWithAndWithoutInitializer() {
        parseSnippet("""
                package p;
                class A {
                    int a = 1;
                    boolean b = false;
                    String s;
                }
                """);
    }

    @Test
    public void testMixedFieldInitializersAndArbitraryPosition() {
        parseSnippet("""
                package p;
                class A {
                    int a = 1;
                    void foo() {}
                    boolean b = false;
                    protected int bar() {}
                    String s;
                }
                """);
    }

    // =========================
    // NEGATIVE / BORDERLINE
    // =========================

    @Test
    public void testInvalidMissingPackageFromProgramRoot() {
        parseSnippetWithErrors("class A {}");
    }

    @Test
    public void testInvalidMissingClassName() {
        parseSnippetWithErrors("""
                package p;
                class {}
                """);
    }

    @Test
    public void testInvalidMissingClassBody() {
        parseSnippetWithErrors("""
                package p;
                class A
                """);
    }

    @Test
    public void testInvalidClassNameStartingWithDigit() {
        parseSnippetWithErrors("""
                package p;
                class 1A {}
                """);
    }

    @Test
    public void testInvalidExtendsWithoutSuperName() {
        parseSnippetWithErrors("""
                package p;
                class A extends {}
                """);
    }

    @Test
    public void testInvalidFieldMissingSemicolon() {
        parseSnippetWithErrors("""
                package p;
                class A {
                    int a
                }
                """);
    }

    @Test
    public void testInvalidFieldTypeStartingWithDigit() {
        parseSnippetWithErrors("""
                package p;
                class A {
                    10 a;
                }
                """);
    }

    @Test
    public void testInvalidMethodMissingParentheses() {
        parseSnippetWithErrors("""
                package p;
                class A {
                    void foo {}
                }
                """);
    }

    @Test
    public void testInvalidMethodMissingBody() {
        parseSnippetWithErrors("""
                package p;
                class A {
                    void foo();
                }
                """);
    }

    @Test
    public void testInvalidParameterMissingName() {
        parseSnippetWithErrors("""
                package p;
                class A {
                    void foo(int) {}
                }
                """);
    }

    @Test
    public void testInvalidParameterMissingType() {
        parseSnippetWithErrors("""
                package p;
                class A {
                    void foo(a) {}
                }
                """);
    }

    @Test
    public void testInvalidLocalVariableMissingSemicolon() {
        parseSnippetWithErrors("""
                package p;
                class A {
                    void foo() {
                        int a
                    }
                }
                """);
    }

    @Test
    public void testInvalidArbitraryPositionMalformedField() {
        parseSnippetWithErrors("""
                package p;
                class A {
                    void foo() {}
                    int;
                }
                """);
    }

    @Test
    public void testInvalidPrivateProtectedTogether() {
        parseSnippetWithErrors("""
                package p;
                class A {
                    private protected void foo() {}
                }
                """);
    }

    @Test
    public void testInvalidVisibilityAfterStatic() {
        parseSnippetWithErrors("""
                package p;
                class A {
                    static private void foo() {}
                }
                """);
    }

    @Test
    public void testInvalidFieldInitializerWithoutExpression() {
        parseSnippetWithErrors("""
                package p;
                class A {
                    int a = ;
                }
                """);
    }

    @Test
    public void testInvalidFieldInitializerMissingSemicolon() {
        parseSnippetWithErrors("""
                package p;
                class A {
                    int a = 0
                }
                """);
    }

    @Test
    public void testInvalidExtraTokensAfterClass() {
        parseSnippetWithErrors("""
                package p;
                class A {} extra
                """);
    }

    @Test
    public void testInvalidStuffOutsideClass() {
        parseSnippetWithErrors("""
                package p;
                class A {}
                int a;
                """);
    }
}