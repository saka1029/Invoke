package test;

import static org.junit.Assert.*;
import static invoke.Global.*;

import org.junit.Test;

public class TestSymbol {

    @Test(expected = RuntimeException.class)
    public void testGetNotDefined() {
        eval(read("UNKNOWN-VARIABLE"));
    }

    @Test(expected = RuntimeException.class)
    public void testSetNotDefined() {
        eval(read("(set! UNKNOWN-VARIABLE 123)"));
    }

}
