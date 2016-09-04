package test.reflection;

import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import org.junit.Test;

import reflection.Functional;
import reflection.Reflection;

public class TestReflection {

    @Test
    public void testInvokeStatic()
        throws IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, NoSuchMethodException {
        assertEquals(Math.sin(3), Reflection.invoke(Math.class, "sin", 3.0));
        // cannot cast Integer to Double
//        assertEquals(Math.sin(3), Reflection.invoke(Math.class, "sin", 3));
    }

    @Test
    public void testvInvokeStaticVarArgs()
        throws IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, NoSuchMethodException {
        assertEquals("x=3", Reflection.invoke(String.class, "format", "%s=%d", "x", 3));
        assertEquals(Arrays.asList("a", "b", 3), Reflection.invoke(Arrays.class, "asList", "a", "b", 3));
    }
 
    @Test
    public void testInvokeInstance()
        throws IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, NoSuchMethodException {
        assertEquals("bc", Reflection.invoke("abc", "substring", 1));
        assertEquals("b", Reflection.invoke("abc", "substring", 1, 2));
    }
    
    public int sum(int... args) {
        return IntStream.of(args).sum();
    }

    @Test
    public void testInvokeInstanceVarArgs()
        throws IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, NoSuchMethodException {
        assertEquals(6, Reflection.invoke(this, "sum", 1, 2, 3));
        assertEquals(6, Reflection.invoke(this, "sum", new Object[] {1, 2, 3}));
    }
    
    public int gen(Supplier<Integer> s) {
        return s.get();
    }
    
    @Test
    public void testInvokeInstanceNoArgsFunctional()
        throws IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, NoSuchMethodException {
        assertEquals(6, Reflection.invoke(this, "gen", (Functional) a -> 6));
    }
 
    @Test
    public void testInvokeStaticFunctionalArgs()
        throws IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, NoSuchMethodException {
        Integer[] array = {1, 3, 2};
        Reflection.invoke(Arrays.class, "sort", array, (Functional) a -> (int)a[1] - (int)a[0]);
        assertArrayEquals(new Integer[] {3, 2, 1}, array);
    }

    public static <T> T prog1(T obj, Consumer<T>... consumers) {
        for (Consumer<T> c : consumers)
            c.accept(obj);
        return obj;
    }
    
    @Test
    public void testInvokeStaticFunctionalVarArgs()
        throws IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, NoSuchMethodException {
        List<Integer> list = new ArrayList<>();
        Reflection.invoke(this.getClass(), "prog1", list,
            (Functional) a -> ((List)a[0]).add(1),
            (Functional) a -> ((List)a[0]).add(2));
        assertEquals(Arrays.asList(1, 2), list);
    }
    
    public static int size(int[] array) { return array.length; }
    public static int get(int[] array, int index) { return array[index]; }
    public static int size(Object[] array) { return array.length; }
    public static Object get(Object[] array, int index) { return array[index]; }

    @Test
    public void testInvokeStaticArray()
        throws IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, NoSuchMethodException {
        int[] ints = {1, 2, 3};
        assertEquals(3, Reflection.invoke(this.getClass(), "size", ints));
        assertEquals(3, Reflection.invoke(this.getClass(), "get", ints, 2));
        Integer[] integers = {1, 2, 3};
        // cannot cast Integer[] to Object[]
//        assertEquals(3, Reflection.invoke(this.getClass(), "size", integers));
        assertEquals(3, Reflection.invoke(this.getClass(), "size", new Object[] {integers}));
        assertEquals(3, Reflection.invoke(this.getClass(), "get", integers, 2));
    }
    
    @Test(expected = NoSuchMethodException.class)
    public void testInvokeStaticError()
        throws IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, NoSuchMethodException {
        Reflection.invoke(Math.class, "sin", 3.0, 5.0);
    }

    @Test
    public void testConstructOneArgs()
        throws InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException,
            NoSuchMethodException {
        assertEquals(3, Reflection.construct(Integer.class, 3));
    }
    
    @Test
    public void testConstructNoArgs()
        throws InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException,
            NoSuchMethodException {
        assertEquals(Arrays.asList(), Reflection.construct(ArrayList.class));
    }
    
    @Test(expected = NoSuchMethodException.class)
    public void testConstrucError()
        throws InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException,
            NoSuchMethodException {
        Reflection.construct(Integer.class);
    }
    
    @Test
    public void testFieldStatic()
        throws IllegalArgumentException, IllegalAccessException,
            NoSuchFieldException, SecurityException {
        assertEquals(Integer.MAX_VALUE, Reflection.field(Integer.class, "MAX_VALUE"));
    }
    
    public static class Person {
        public String name;
        public Person(String name) { this.name = name; }
    }

    @Test
    public void testFieldInstance()
        throws IllegalArgumentException, IllegalAccessException,
            NoSuchFieldException, SecurityException,
            InstantiationException, InvocationTargetException,
            NoSuchMethodException {
        Person p = (Person) Reflection.construct(Person.class, "Jhon");
        assertEquals("Jhon", Reflection.field(p, "name"));
    }

    @Test(expected = NoSuchFieldException.class)
    public void testFieldInstanceError()
        throws IllegalArgumentException, IllegalAccessException,
            NoSuchFieldException, SecurityException,
            InstantiationException, InvocationTargetException,
            NoSuchMethodException {
        Person p = (Person) Reflection.construct(Person.class, "Jhon");
        Reflection.field(p, "fullName");
    }
}
