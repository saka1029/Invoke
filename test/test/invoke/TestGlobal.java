package test.invoke;

import static org.junit.Assert.*;
import static invoke.Global.*;

import org.junit.Test;

public class TestGlobal {
    
    static void test(String expected, String test) {
        assertEquals(read(expected), eval(read(test)));
    }

    @Test
    public void testCar() {
        test("1", "(car '(1 2 3))");
    }

    @Test
    public void testCdr() {
        test("(2 3)", "(cdr '(1 2 3))");
    }

    @Test
    public void testCons() {
        test("(1 . 2)", "(cons 1 2)");
    }

    @Test
    public void testEq() {
        test("true", "(eq? 1 1)");
        test("false", "(eq? 1 2)");
        test("false", "(eq? '(a b) '(a b))");
        test("false", "(eq? '(a b) '(a x))");
        test("false", "(eq? '(a b) '(x b))");
    }

    @Test
    public void testEqual() {
        test("true", "(equal? 1 1)");
        test("false", "(equal? 1 2)");
        test("true", "(equal? '(a b) '(a b))");
        test("false", "(equal? '(a b) '(a x))");
        test("false", "(equal? '(a b) '(x b))");
    }

}
