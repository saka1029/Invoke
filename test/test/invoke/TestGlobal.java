package test.invoke;

import static org.junit.Assert.*;
import static invoke.Global.*;

import org.junit.Test;

import invoke.Symbol;

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
    
    @Test
    public void testListSplice() {
        Object inner = list("b", "c");
        assertEquals(list("a", "b", "c", "d"), list("a", splice(inner), "d"));
        assertEquals(list("a", "d"), list("a", splice(NIL), "d"));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testListSpliceError() {
        assertEquals(list("a", "d"), list("a", splice("b"), "d"));
    }
    
    @Test
    public void testMutiaryOperator() {
        Symbol lang = symbol("Lang");
        Symbol method = symbol("plus");
        Integer unit = 0;
//        Symbol operator = symbol("+");
        assertEquals(read("0"), multiaryOperator(lang, method, unit, NIL));
        assertEquals(read("(invoke Lang plus 0 1)"), multiaryOperator(lang, method, unit, list(1)));
        assertEquals(read("(invoke Lang plus 1 2)"), multiaryOperator(lang, method, unit, list(1, 2)));
        assertEquals(read("(invoke Lang plus (invoke Lang plus 1 2) 3)"), multiaryOperator(lang, method, unit, list(1, 2, 3)));
        assertEquals(read("(invoke Lang plus (invoke Lang plus (invoke Lang plus 1 2) 3) 4)"), multiaryOperator(lang, method, unit, list(1, 2, 3, 4)));
        assertEquals(read("(invoke Lang plus (invoke Lang plus (invoke Lang plus (invoke Lang plus 1 2) 3) 4) 5)"),
            multiaryOperator(lang, method, unit, list(1, 2, 3, 4, 5)));
    }

}
