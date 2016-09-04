package test.invoke;

import static org.junit.Assert.*;
import static invoke.Global.*;

import org.junit.Test;

import invoke.Pair;

public class TestInvoke {

    static void test(String expected, String eval) {
        assertEquals(read(expected), eval(read(eval)));
    }

    @Test
    public void testMethod() {
        test("\"ABCDEF\"", "(invoke \"abcdef\" toUpperCase)");
        test("\"def\"", "(invoke \"abcdef\" substring 3)");
        test("\"d\"", "(invoke \"abcdef\" substring 3 4)");
        test("1", "(invoke '(1 2 3) car)");
        assertEquals(Pair.class, eval(read("(invoke '(1 2 3) getClass)")));
        assertEquals(eval(read("String")), eval(read("(invoke Class forName \"java.lang.String\")")));
        test("(2 3)", "(invoke '(1 2 3) cdr)");
    }
    
    @Test
    public void testArray() {
        test("3", "(invoke Array getLength (invoke Array newInstance int 3))");
    }

    @Test
    public void testVarargs() {
        test("\"a123bc\"", "(invoke String format \"a%d%sc\" 123 \"b\")");
    }

    @Test
    public void testConstructor() {
        test("3", "(invoke Integer new 3)");
    }

    @Test
    public void testField() {
        test(Integer.toString(Integer.MAX_VALUE), "(field Integer MAX_VALUE)");
        test(Integer.toString(Integer.MIN_VALUE), "(field 123 MIN_VALUE)");
    }
    
    public static class Person {
        public static final String NAME = "Person";
        
        public static Person make(String name) {
            return new Person(name);
        }

        public String name;
        
        public Person(String name) {
            this.name = name;
        }

        public String name() {
            return "*" + name + "*";
        }
        
        public String say() {
            return "My name is " + name;
        }
    }
    
    @Test
    public void testUserClass() {
        define(symbol("Person"), Person.class);
        eval(read("(define jhon (invoke Person new \"Jhon\"))"));
        test("\"Jhon\"", "(field jhon name)");
        test("\"*Jhon*\"", "(invoke jhon name)");
        test("\"My name is Jhon\"", "(invoke jhon say)");
        test("\"Person\"", "(field Person NAME)");
        test("\"Person\"", "(field jhon NAME)");
        test("\"Jane\"", "(field (invoke Person make \"Jane\") name)");
    }

}
