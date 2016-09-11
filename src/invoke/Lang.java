package invoke;

import java.lang.reflect.Array;
import java.math.BigInteger;
import java.util.function.Supplier;

public class Lang {

    private Lang() {}
    
    @Proc(name="car", args=1) public static Object car(Pair a) { return a.car; }
    public static Object cdr(Pair a) { return a.cdr; }
    public static Object caar(Pair a) { return ((Pair)a.car).car; }
    public static Object cadr(Pair a) { return ((Pair)a.cdr).car; }
    public static Object cdar(Pair a) { return ((Pair)a.car).cdr; }
    public static Object cddr(Pair a) { return ((Pair)a.cdr).cdr; }
    public static Pair cons(Object a, Object b) { return new Pair(a, b); }
    public static List list(Object... a) {
        List r = Global.NIL;
        for (int i = a.length - 1; i >= 0; --i)
            r = cons(a[i], r);
        return r;
    }
    public static Object append(Object... lists) {
        List.Builder b = new List.Builder();
        for (Object list : lists)
            for (Object e = list; e instanceof Pair; e = cdr((Pair)e))
                b.tail(car((Pair)e));
        return b.build();
    }
    public static Object iif(Object cond, Supplier<Object> t) {
        return !cond.equals(Global.FALSE)
            ? t.get()
            : Global.UNDEF;
    }
    public static Object iif(Object cond, Supplier<Object> t, Supplier<Object> e) {
        return !cond.equals(Global.FALSE) ? t.get() : e.get();
    }
    public static boolean pairp(Object a) { return a instanceof Pair; }
    public static boolean nullp(Object a) { return a == Global.NIL; }
    public static boolean eqp(Object a, Object b) { return a == b; }
    public static boolean equalp(Object a, Object b) { return a.equals(b); }
    public static Object display(Object... a) {
        for (Object e : a)
            System.out.println(e);
        return Global.UNDEF;
    }
    public static boolean eq(Object a, Object b) { return a.equals(b); }
    public static boolean ne(Object a, Object b) { return !eq(a, b); }
    public static <T extends Comparable<T>> boolean lt(T a, T b) { return a.compareTo(b) < 0; }
    public static <T extends Comparable<T>> boolean le(T a, T b) { return a.compareTo(b) <= 0; }
    public static <T extends Comparable<T>> boolean gt(T a, T b) { return a.compareTo(b) > 0; }
    public static <T extends Comparable<T>> boolean ge(T a, T b) { return a.compareTo(b) >= 0; }
    public static int length(Object a) {
        if (a instanceof String)
            return ((String)a).length();
        else if (a.getClass().isArray())
            return Array.getLength(a);
        throw new IllegalArgumentException("cannot get length of " + a);
    }
    public static int plus(int a, int b) { return a + b; }
    public static BigInteger plus(BigInteger a, int b) { return a.add(BigInteger.valueOf(b)); }
    public static BigInteger plus(int a, BigInteger b) { return b.add(BigInteger.valueOf(a)); }
    public static BigInteger plus(BigInteger a, BigInteger b) { return b.add(a); }
    public static String plus(String a, Object b) { return a + b; }
    public static String plus(Object a, String b) { return a + b; }
    public static int minus(int a, int b) { return a - b; }
    public static BigInteger minus(BigInteger a, int b) { return a.subtract(BigInteger.valueOf(b)); }
    public static BigInteger minus(int a, BigInteger b) { return b.subtract(BigInteger.valueOf(a)); }
    public static BigInteger minus(BigInteger a, BigInteger b) { return b.subtract(a); }
    public static int multiply(int a, int b) { return a * b; }
    public static BigInteger multiply(BigInteger a, int b) { return a.multiply(BigInteger.valueOf(b)); }
    public static BigInteger multiply(int a, BigInteger b) { return b.multiply(BigInteger.valueOf(a)); }
    public static BigInteger multiply(BigInteger a, BigInteger b) { return b.multiply(a); }
    public static int divide(int a, int b) { return a / b; }
    public static int mod(int a, int b) { return a % b; }

}
