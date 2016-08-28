package invoke;

import java.io.StringReader;

public class Global {

    private Global() {}
    
    private static final Env ENV = new Env(null);
    public static final Symbol QUOTE = Symbol.of("quote");
    public static final List NIL = List.NIL;
    public static final Boolean TRUE = Boolean.TRUE;
    public static final Boolean FALSE = Boolean.FALSE;
    
    static {
        defineSyntax(QUOTE, (args, env) -> car(args));
        defineProcedure("car", args -> car(car(args)));
        defineProcedure("cdr", args -> cdr(car(args)));
        defineProcedure("cons", args -> cons(car(args), car(cdr(args))));
        defineProcedure("pair?", args -> car(args) instanceof Pair);
        defineProcedure("null?", args -> car(args) == NIL);
        defineProcedure("eq?", args -> car(args) == car(cdr(args)));
        defineProcedure("equal?", args -> car(args).equals(car(cdr(args))));
    }
 
    static void defineSyntax(Symbol key, Applicable value) {
        ENV.define(key, value);
    }
    
    static void defineSyntax(String name, Applicable value) {
        defineSyntax(symbol(name), value);
    }
    
    static void defineProcedure(String name, Procedure value) {
        ENV.define(symbol(name), value);
    }

    public static Object eval(Object obj, Env env) {
        if (obj instanceof Evaluable)
            return ((Evaluable)obj).eval(env);
        else
            return obj;
    }
    
    public static Object eval(Object obj) {
        return eval(obj, ENV);
    }
    
    public static Object read(String s) {
        return new Reader(new StringReader(s)).read();
    }
    
    public static Symbol symbol(String name) {
        return Symbol.of(name);
    }

    public static Object car(Object obj) {
        return ((List)obj).car();
    }
    
    public static Object cdr(Object obj) {
        return ((List)obj).cdr();
    }
    
    public static Pair cons(Object car, Object cdr) {
        return new Pair(car, cdr);
    }
    
    public static Object list(Object... objects) {
        Object r = NIL;
        for (int i = objects.length - 1; i >= 0; --i)
            r = cons(objects[i], r);
        return r;
    }
}
