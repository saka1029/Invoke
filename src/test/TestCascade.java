package test;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.function.Function;

import static invoke.Global.*;

import org.junit.Test;

import invoke.Pair;

public class TestCascade {


    static void test(String expected, String eval) {
        assertEquals(read(expected), eval(read(eval)));
    }

    @Test
    public void testMethod() {
        test("\"ABCDEF\"", "(@ \"abcdef\" (toUpperCase))");
        test("\"def\"", "(@ \"abcdef\" (substring 3))");
        test("\"d\"", "(@ \"abcdef\" (substring 3 4))");
        test("\"d\"", "(@ \"abcdef\" (\"substring\" 3 4))");
        test("1", "(@ '(1 2 3) (car))");
        test("(2 3)", "(@ '(1 2 3) (cdr))");
        assertEquals(Pair.class, eval(read("(@ '(1 2 3) (getClass))")));
        assertEquals(eval(read("String")), eval(read("(@ Class (forName \"java.lang.String\"))")));
    }
    
    @Test
    public void testArray() {
        test("3", "(@ Array (getLength (@ Array (newInstance int 3))))");
    }

    @Test
    public void testVarargs() {
        test("\"a123bc\"", "(@ String (format \"a%d%sc\" 123 \"b\"))");
    }

    @Test
    public void testConstructor() {
        test("3", "(@ Integer (new 3))");
    }

    @Test
    public void testField() {
        test(Integer.toString(Integer.MAX_VALUE), "(@ Integer MAX_VALUE)");
        test(Integer.toString(Integer.MIN_VALUE), "(@ 123 MIN_VALUE)");
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
        test("*UNDEF*", "(define jhon (invoke Person 'new \"Jhon\"))");
        test("\"Jhon\"", "(@ jhon name)");
        test("\"*Jhon*\"", "(@ jhon (name))");
        test("\"My name is Jhon\"", "(@ jhon (say))");
        test("\"Person\"", "(@ Person NAME)");
        test("\"Person\"", "(@ jhon NAME)");
        test("\"Jane\"", "(@ Person (make \"Jane\") name)");
    }
    
    @Test
    public void testStreamFilter() {
        define(symbol("Arrays"), Arrays.class);
        test("6",
            "(@ (@ Arrays (asList 1 2 3 4 5))"
            + "    (stream)"
            + "    (mapToInt (lambda (x) x))"
            + "    (filter (lambda (x) (< x 4)))"
            + "    (sum))");
    }
    
    @Test
    public void testStreamReduce() {
        define(symbol("Arrays"), Arrays.class);
        define(symbol("Function"), Function.class);
        test("15",
            "(@ (@ Arrays (asList 1 2 3 4 5))"
            + "    (stream)"
            + "    (mapToInt (lambda (x) x))"
            + "    (reduce 0 (lambda (a b) (+ a b))))");
    }

}
