package test.invoke;

import static org.junit.Assert.*;
import static invoke.Global.*;

import org.junit.Test;

public class TestBackquoteMacro {

    @Test
    public void testSymbol() {
        assertEquals(read("'a"), backquote(read("a")));
    }
    
    @Test
    public void testUnquote() {
        assertEquals(read("(cons b (cons 'c '()))"), backquote(read("(,b c)")));
    }
    
    @Test
    public void testSplicing() {
        assertEquals(read("(append b (cons 'c '()))"), backquote(read("(,@b c)")));
    }
    
    @Test
    public void testSplicing2() {
        assertEquals(read("(append a (cons 'b (append c '())))"), backquote(read("(,@a b ,@c)")));
    }

}
