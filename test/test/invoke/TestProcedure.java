package test.invoke;

import static org.junit.Assert.*;

import org.junit.Test;
import static invoke.Global.*;

public class TestProcedure {

    @Test
    public void testCar() {
        assertEquals(read("a"), eval(read("(car '(a b))")));
    }
    
    @Test
    public void testCdr() {
        assertEquals(read("(b)"), eval(read("(cdr '(a b))")));
    }
    
    @Test
    public void testCons() {
        assertEquals(read("(a . b)"), eval(read("(cons 'a 'b)")));
    }
    
    @Test
    public void testList() {
        assertEquals(read("(a b c)"), eval(read("(list 'a 'b 'c)")));
    }
    
    @Test
    public void testEqTrue() {
        assertEquals(read("true"), eval(read("(eq? 'a 'a)")));
    }
    
    @Test
    public void testEqFalse() {
        assertEquals(read("false"), eval(read("(eq? 'a 'b)")));
    }
    
    @Test
    public void testEqualTrue() {
        assertEquals(read("true"), eval(read("(equal? '(a b) '(a b))")));
    }
    
    @Test
    public void testEqualFalseCar() {
        assertEquals(read("false"), eval(read("(equal? '(a b) '(x b))")));
    }
    
    @Test
    public void testEqualFalseCdr() {
        assertEquals(read("false"), eval(read("(equal? '(a b) '(a x))")));
    }
    
    @Test
    public void testEqualFalseDifferentClass() {
        assertEquals(read("false"), eval(read("(equal? '(a b) 3)")));
    }
    
    @Test
    public void testPairTrue() {
        assertEquals(read("true"), eval(read("(pair? '(a b))")));
    }
    
    @Test
    public void testPairFalse() {
        assertEquals(read("false"), eval(read("(pair? 'a)")));
    }
    
    @Test
    public void testPairFalseNil() {
        assertEquals(read("false"), eval(read("(pair? '())")));
    }
    
    @Test
    public void testDisplay() {
        assertEquals(read("*UNDEF*"), eval(read("(display 'a 'b)")));
    }
    
    @Test
    public void testLength() {
        assertEquals(read("2"), eval(read("(length \"ab\")")));
    }
    
    @Test
    public void testLengthArray() {
        assertEquals(read("2"), eval(read("(length (@ Array (newInstance int 2)))")));
    }
  
}
