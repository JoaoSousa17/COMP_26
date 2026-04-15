package core.semantics.arrays;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;

public class ArraySemanticsTest extends JmmTestEnv {

    public ArraySemanticsTest() {
        super("", "");
    }

    @Test
    public void testArrayAccessIntIndex() {
        setDescription("Array access with int index is valid");
        semanticsFromSnippet("""
                  package x; class A {
                      int foo() { int[] a; return a[0]; }
                  }""", false);
    }

    @Test
    public void testArrayAccessBooleanIndexIsError() {
        setDescription("Array access with boolean index must report error");
                semanticsFromSnippet("""
                  package x; class A {
                      int foo() { int[] a; boolean b; return a[b]; }
                  }""", true);
    }

    @Test
    public void testAccessOnNonArrayIsError() {
        setDescription("Using [] on int variable must report error");
        semanticsFromSnippet("""
                  package x; class A {
                      int foo() { int x; return x[0]; }
                  }""", true);
    }

    @Test
    public void testArrayInArithmeticIsError() {
        setDescription("Using array directly in arithmetic must report error");
                semanticsFromSnippet("""
                  package x; class A {
                      int foo(int[] a, int[] b) { return a + b; }
                  }""", true);
    }

    @Test
    public void testMultidimAccessReturnsArray() {
        setDescription("int[][] a; a[0][1] is valid and returns int");
        semanticsFromSnippet("""
                  package x; class A {
                      int foo() { int[][] a; return a[0][1]; }
                  }""", false);
    }

    @Test
    public void testMultidimPartialAccessReturnsArray() {
        setDescription("int[][] a; a[0] returns int[], assignable to int[]");
        semanticsFromSnippet("""
                  package x; class A {
                      int[] foo() { int[][] a; int[] b; b = a[0]; return b;
  }
                  }""", false);
    }

    @Test
    public void testArrayStoreIntIndex() {
        setDescription("Array store with int index is valid");
        semanticsFromSnippet("""
                  package x; class A {
                      void foo() { int[] a; a[0] = 42; }
                  }""", false);
    }

    @Test
    public void testArrayStoreBooleanIndexIsError() {
        setDescription("Array store with boolean index must report error");
                semanticsFromSnippet("""
                  package x; class A {
                      void foo() { int[] a; boolean b; a[b] = 42; }
                  }""", true);
    }

    @Test
    public void testArrayInitializerInts() {
        setDescription("Array initializer with int elements is valid");
        semanticsFromSnippet("""
                  package x; class A {
                      void foo() { int[] a; a = new int[] { 1, 2, 3 }; }
                  }""", false);
    }

    @Test
    public void testArrayInitializerBooleanIsError() {
        setDescription("Array initializer with boolean element must report error");
                semanticsFromSnippet("""
                  package x; class A {
                      void foo() { int[] a; a = new int[] { true, 2 }; }
                  }""", true);
    }

    @Test
    public void testVoidArrayIsError() {
        setDescription("void[] declaration must report error");
        semanticsFromSnippet("""
                  package x; class A {
                      void foo() { void[] a; }
                  }""", true);
    }


}
