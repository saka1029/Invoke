package test.invoke;

import static org.junit.Assert.*;

import org.junit.Test;

import static invoke.Global.*;

public class TestIif {

    @Test
    public void test() {
        assertEquals(read("a"), eval(read("(iif true 'a 'b)")));
        assertEquals(read("b"), eval(read("(iif false 'a 'b)")));
        assertEquals(read("*UNDEF*"), eval(read("(iif (< 3 1) 'a)")));
        assertEquals(read("a"), eval(read("(iif (> 3 1) 'a)")));
    }

}
