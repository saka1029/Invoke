package test;

import static org.junit.Assert.*;
import static invoke.Global.*;

import org.junit.Test;

import invoke.InvokeException;

public class TestReader {

    @Test(expected = InvokeException.class)
    public void testRightParenExpectedList() {
        eval(read("(a"));
    }

    @Test(expected = InvokeException.class)
    public void testRightParenExpectedDotPair() {
        eval(read("(a ."));
    }

    @Test(expected = InvokeException.class)
    public void testUnexpectedDot() {
        eval(read(". a"));
    }
    
    @Test
    public void testDotSymbol() {
        assertEquals(symbol(".abc"), read(".abc"));
    }

}
