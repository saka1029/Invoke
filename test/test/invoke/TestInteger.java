package test.invoke;

import static org.junit.Assert.*;

import org.junit.Test;
import static invoke.Global.*;

public class TestInteger {
  
    @Test
    public void testNumEqTrue() {
        assertEquals(read("true"), eval(read("(== 2 2)")));
    }
  
    @Test
    public void testNumEqFalse() {
        assertEquals(read("false"), eval(read("(== 1 2)")));
    }
  
    @Test
    public void testNumEqFalseNotNum() {
        assertEquals(read("false"), eval(read("(== 1 'a)")));
    }
  
    @Test
    public void testNumNeTrue() {
        assertEquals(read("true"), eval(read("(!= 1 2)")));
    }
  
    @Test
    public void testNumNeFalse() {
        assertEquals(read("false"), eval(read("(!= 2 2)")));
    }
  
    @Test
    public void testNumNeFalseNotNum() {
        assertEquals(read("true"), eval(read("(!= 1 'a)")));
    }
    
    @Test
    public void testNumLtTrue() {
        assertEquals(read("true"), eval(read("(< 1 2)")));
    }
    
    @Test
    public void testNumLtFalse() {
        assertEquals(read("false"), eval(read("(< 1 1)")));
    }
    
    @Test
    public void testNumLeTrue() {
        assertEquals(read("true"), eval(read("(<= 1 2)")));
    }
    
    @Test
    public void testNumLeFalse() {
        assertEquals(read("false"), eval(read("(<= 2 1)")));
    }
    
    @Test
    public void testNumGtTrue() {
        assertEquals(read("true"), eval(read("(> 2 1)")));
    }
    
    @Test
    public void testNumGtFalse() {
        assertEquals(read("false"), eval(read("(> 1 1)")));
    }
    
    @Test
    public void testNumGeTrue() {
        assertEquals(read("true"), eval(read("(>= 2 1)")));
    }
    
    @Test
    public void testNumGeFalse() {
        assertEquals(read("false"), eval(read("(>= 1 2)")));
    }
    
    @Test
    public void testNumPlus0() {
        assertEquals(read("0"), eval(read("(+)")));
    }
    
    @Test
    public void testNumPlus1() {
        assertEquals(read("1"), eval(read("(+ 1)")));
    }
    
    @Test
    public void testNumPlus2() {
        assertEquals(read("3"), eval(read("(+ 1 2)")));
    }
    
    @Test
    public void testNumPlus3() {
        assertEquals(read("6"), eval(read("(+ 1 2 3)")));
    }
    
    @Test
    public void testNumMinus0() {
        assertEquals(read("0"), eval(read("(-)")));
    }
    
    @Test
    public void testNumMinus1() {
        assertEquals(read("-1"), eval(read("(- 1)")));
    }
    
    @Test
    public void testNumMinus2() {
        assertEquals(read("-1"), eval(read("(- 1 2)")));
    }
    
    @Test
    public void testMult0() {
        assertEquals(read("1"), eval(read("(*)")));
    }
    
    @Test
    public void testMult1() {
        assertEquals(read("2"), eval(read("(* 2)")));
    }
    
    @Test
    public void testMult2() {
        assertEquals(read("6"), eval(read("(* 2 3)")));
    }
    
    @Test
    public void testDiv0() {
        assertEquals(read("1"), eval(read("(/)")));
    }
    
    @Test
    public void testDiv1() {
        assertEquals(read("0"), eval(read("(/ 2)")));
    }
    
    @Test
    public void testDiv2() {
        assertEquals(read("3"), eval(read("(/ 9 3)")));
    }
}
