package test.invoke;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.stream.Collectors;

import static invoke.Global.*;

import org.junit.Ignore;
import org.junit.Test;

import invoke.Pair;

public class TestCascade {

    static void testRead(String expected, String eval) {
        assertEquals(read(expected), eval(read(eval)));
    }

    static void testEval(String expected, String eval) {
        assertEquals(eval(read(expected)), eval(read(eval)));
    }

    @Test
    public void testMethod() {
        testRead("\"ABCDEF\"", "(@ \"abcdef\" (toUpperCase))");
        testRead("\"def\"", "(@ \"abcdef\" (substring 3))");
        testRead("\"d\"", "(@ \"abcdef\" (substring 3 4))");
        testRead("1", "(@ '(1 2 3) (car))");
        testRead("(2 3)", "(@ '(1 2 3) (cdr))");
        assertEquals(Pair.class, eval(read("(@ '(1 2 3) (getClass))")));
        assertEquals(eval(read("String")), eval(read("(@ Class (forName \"java.lang.String\"))")));
    }
    
    @Test
    public void testArrayGetLength() {
        testRead("3", "(@ Array (getLength (@ Array (newInstance int 3))))");
    }
    
    /*
     * java.lang.NoSuchFieldException: length
     */
//    @Test
//    public void testArrayLength() {
//        testRead("3", "(@ Array (newInstance int 3) length)");
//    }

    @Test
    public void testVarargs() {
        testRead("\"a123bc\"", "(@ String (format \"a%d%sc\" 123 \"b\"))");
    }

    @Test
    public void testConstructor() {
        testRead("3", "(@ Integer (new 3))");
    }

    @Test
    public void testField() {
        testRead(Integer.toString(Integer.MAX_VALUE), "(@ Integer MAX_VALUE)");
        testRead(Integer.toString(Integer.MIN_VALUE), "(@ 123 MIN_VALUE)");
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
        testRead("*UNDEF*", "(define jhon (@ Person (new \"Jhon\")))");
        testRead("\"Jhon\"", "(@ jhon name)");
        testRead("\"*Jhon*\"", "(@ jhon (name))");
        testRead("\"My name is Jhon\"", "(@ jhon (say))");
        testRead("\"Person\"", "(@ Person NAME)");
        testRead("\"Person\"", "(@ jhon NAME)");
        testRead("\"Jane\"", "(@ Person (make \"Jane\") name)");
    }
    
    @Test
    public void testStreamFilter() {
        define(symbol("Arrays"), Arrays.class);
        testRead("6",
            "(@ Arrays"
            + "    (asList 1 2 3 4 5)"
            + "    (stream)"
            + "    (mapToInt (lambda (x) x))"
            + "    (filter (lambda (x) (< x 4)))"
            + "    (sum)  )");
    }
    
    @Test
    public void testStreamReduce() {
        define(symbol("Arrays"), Arrays.class);
        testRead("15",
            "(@ Arrays"
            + "    (asList 1 2 3 4 5)"
            + "    (stream)"
            + "    (mapToInt (lambda (x) x))"
            + "    (reduce 0 (lambda (a b) (+ a b)))  )");
    }
    
    @Test
    public void testStreamSort() {
        define(symbol("Arrays"), Arrays.class);
        define(symbol("Collectors"), Collectors.class);
        testEval("(@ Arrays (asList 5 4 3 2 1))",
            "(@ Arrays"
            + "    (asList 1 2 3 4 5)"
            + "    (stream)"
            + "    (sorted (lambda (a b) (- b a)))"
            + "    (collect (@ Collectors (toList)))  )");
    }
    
    @Ignore("suspend test")
    @Test
    public void testSwing() {
        define(symbol("JOptionPane"), javax.swing.JOptionPane.class);
        testEval("null", "(@ JOptionPane (showMessageDialog null \"Hello world\"))");
        testEval("null", "(@ JOptionPane"
            + "  (showMessageDialog null \"Hello world\" \"Welcome to Invoke\""
            + "    (@ JOptionPane INFORMATION_MESSAGE)))");
    }

}
