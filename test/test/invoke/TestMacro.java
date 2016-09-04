package test.invoke;

import static org.junit.Assert.*;
import static invoke.Global.*;

import org.junit.Test;

public class TestMacro {

    static void test(String expected, String eval) {
        assertEquals(read(expected), eval(read(eval)));
    }

    @Test
    public void testUnquote() {
        test("(a (p q r) b)", "(let ((X '(p q r))) `(a ,X b))");
    }

    @Test
    public void testSplicing() {
        test("(a p q r b)", "(let ((X '(p q r))) `(a ,@X b))");
    }

    @Test
    public void testCascade() {
        test("*UNDEF*", "(define-macro (cascade obj . args)"
            + "  (if (null? args)"
            + "       obj"
            + "       (if (pair? (car args))"
            + "           `(cascade (invoke ,obj ,(caar args) ,@(cdar args)) ,@(cdr args))"
            + "           `(cascade (field ,obj ,(car args)) ,@(cdr args)))))");
        test("123", "(cascade 123)");
        test("" + Integer.MAX_VALUE, "(cascade Integer MAX_VALUE)");
        test("\"BCDE\"", "(cascade \"abcde\" (toUpperCase) (substring 1))");
    }

}
