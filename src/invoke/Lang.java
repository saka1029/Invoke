package invoke;

public class Lang {

    private Lang() {}
    
    public static Object car(Pair a) { return a.car; }
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
    public static int plus(int a, int b) { return a + b; }
    public static int minus(int a, int b) { return a - b; }
    public static int multiply(int a, int b) { return a * b; }
    public static int divide(int a, int b) { return a / b; }
    public static int mod(int a, int b) { return a % b; }

}
