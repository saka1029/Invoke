package invoke;

import java.io.StringReader;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

public class Global {

    private Global() {
    }

    private static final Env ENV = new Env(null);
    public static final Symbol QUOTE = Symbol.of("quote");
    public static final Symbol LAMBDA = Symbol.of("lambda");
    public static final Symbol INVOKE = Symbol.of("invoke");
    public static final Symbol FIELD = Symbol.of("field");
    public static final Symbol UNDEF = Symbol.selfEvaluatedOf("*UNDEF*");
    public static final List NIL = List.NIL;
    public static final Boolean TRUE = Boolean.TRUE;
    public static final Boolean FALSE = Boolean.FALSE;

    static {
        define(symbol("Boolean"), Boolean.class);
        define(symbol("Class"), Class.class);
        define(symbol("String"), String.class);
        define(symbol("Integer"), Integer.class);
        define(symbol("int"), Integer.TYPE);
        define(symbol("System"), System.class);
        define(symbol("Array"), Array.class);
    }
    
    static {
        defineSyntax(QUOTE, (args, env) -> car(args));

        defineSyntax("define", (args, env) ->
            car(args) instanceof Pair
                ? env.define((Symbol)car(car(args)), new Closure(cdr(car(args)), cdr(args), env))
                : env.define((Symbol)car(args), eval(car(cdr(args)), env)));

        defineSyntax("set!", (args, env) ->
            env.set((Symbol)car(args), eval(car(cdr(args)), env)));

        defineSyntax("if", (args, env) ->
            eval(car(args), env) != FALSE ? eval(car(cdr(args)), env)
            : cdr(cdr(args)) != NIL ? eval(car(cdr(cdr(args))), env)
            : UNDEF);

        defineSyntax(LAMBDA, (Applicable) (args, env) ->
            new Closure(car(args), cdr(args), env)
        );
    }
    
    static {
        defineProcedure(INVOKE, args -> invoke(args));
        defineProcedure(FIELD, args -> field(args));
        defineProcedure("car", args -> car(car(args)));
        defineProcedure("cdr", args -> cdr(car(args)));
        defineProcedure("cons", args -> cons(car(args), car(cdr(args))));
        defineProcedure("list", args -> args);
        defineProcedure("pair?", args -> car(args) instanceof Pair);
        defineProcedure("null?", args -> car(args) == NIL);
        defineProcedure("eq?", args -> car(args) == car(cdr(args)));
        defineProcedure("equal?", args -> car(args).equals(car(cdr(args))));
        defineProcedure("display", args -> {
            for (; args instanceof Pair; args = cdr(args))
                System.out.println(car(args));
            return UNDEF;
        });
            
    }
    
    static int compare(Object a, Object b) {
        return ((Comparable)a).compareTo((Comparable)b);
    }

    static {
        defineProcedure("=", args -> car(args).equals(car(cdr(args))));
        defineProcedure("<", args -> compare(car(args), car(cdr(args))) < 0);
        defineProcedure("<=", args -> compare(car(args), car(cdr(args))) <= 0);
        defineProcedure(">", args -> compare(car(args), car(cdr(args))) > 0);
        defineProcedure(">=", args -> compare(car(args), car(cdr(args))) >= 0);
        defineProcedure("+", args -> reduce(0, (a, b) -> (int)a + (int)b, args));
        defineProcedure("-", args -> reduce(0, (a, b) -> (int)a - (int)b, args));
        defineProcedure("*", args -> reduce(1, (a, b) -> (int)a * (int)b, args));
        defineProcedure("/", args -> reduce(1, (a, b) -> (int)a / (int)b, args));
    }
    
    static Object letStar(Object vars, Object body) {
        return vars == NIL
            ? list(cons(LAMBDA, cons(NIL, body)))
            : list(cons(LAMBDA, list(list(car(car(vars))),
                    letStar(cdr(vars), body))),
                car(cdr(car(vars))));
    }

    static Object cascade(Object object, Object methods) {
        if (methods == NIL)
            return object;
        if (car(methods) instanceof Pair)
            return cascade(
                cons(INVOKE,
                    cons(object,
                        cons(list(QUOTE, car(car(methods))),
                            cdr(car(methods))))),
                cdr(methods));
        else
            return cascade(
                list( FIELD, object, list(QUOTE, car(methods))),
                cdr(methods));
    }

    static {
        defineMacro("let", args ->
            car(args) instanceof Pair
                ? cons(cons(LAMBDA, cons(map(x -> car(x), car(args)), cdr(args))),
                    map(x -> car(cdr(x)), car(args)))
                : list(list(LAMBDA,
                    list(car(args)),
                    list(symbol("set!"), car(args),
                        cons(LAMBDA,
                            cons(map(x -> car(x), car(cdr(args))),
                                cdr(cdr(args))))),
                    cons(car(args), map(x -> car(cdr(x)), car(cdr(args))))),
                    UNDEF));

        defineMacro("let*", args ->
            letStar(car(args), cdr(args)));

        defineMacro("letrec", (Expandable) args ->
            cons(cons(LAMBDA, cons(map(x -> car(x), car(args)),
                append(map(x -> cons(symbol("set!"), x), car(args)), cdr(args)))),
                map(x -> FALSE, car(args))));

        defineMacro("begin", args -> letStar(NIL, args));
        defineMacro("@", args -> cascade(car(args), cdr(args)));
    }

    public static void define(Symbol key, Object value) {
        ENV.define(key, value);
    }

    static void defineSyntax(Symbol key, Applicable value) {
        define(key, value);
    }

    static void defineSyntax(String name, Applicable value) {
        define(symbol(name), value);
    }

    static void defineProcedure(Symbol key, Procedure value) {
        define(key, value);
    }

    static void defineProcedure(String name, Procedure value) {
        define(symbol(name), value);
    }

    static void defineMacro(String name, Expandable value) {
        define(symbol(name), value);
    }

    public static Object eval(Object obj, Env env) {
        if (obj instanceof Evaluable)
            return ((Evaluable) obj).eval(env);
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
        return ((List) obj).car();
    }

    public static Object cdr(Object obj) {
        return ((List) obj).cdr();
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
    
    public static Object append(Object... lists) {
        List.Builder b = new List.Builder();
        for (Object list : lists)
            for (Object e = list; e instanceof Pair; e = cdr(e))
                b.tail(car(e));
        return b.build();
    }

    public static Object map(UnaryOperator<Object> f, Object list) {
        List.Builder b = new List.Builder();
        for (; list instanceof Pair; list = cdr(list))
            b.tail(f.apply(car(list)));
        return b.build();
    }

    public static Object reduce(Object unit, BinaryOperator<Object> f, Object list) {
        Object result = unit;
        Object prev = null;
        int i = 0;
        for (; list instanceof Pair; list = cdr(list)) {
            switch (i++) {
            case 0: prev = car(list); break;
            case 1: result = prev;
            }
            result = f.apply(result, car(list));
        }
        return result;
    }

    public static Object field(Object args) {
        Object self = car(args);
        String name = car(cdr(args)).toString();
        try {
            return Reflection.field(self, name);
        } catch (IllegalArgumentException
            | IllegalAccessException
            | NoSuchFieldException e) {
            throw new InvokeException(e);
        }
    }

    public static Object invoke(Object args) {
        Object self = car(args);
        String name = car(cdr(args)).toString();
        Object[] arguments = ((List) cdr(cdr(args))).toArray();
        try {
            if (name.equals("new"))
                return Reflection.constructor(self, arguments);
            else
                return Reflection.method(self, name, arguments);
        } catch (InstantiationException
            | IllegalAccessException
            | IllegalArgumentException
            | InvocationTargetException
            | NoSuchMethodException e) {
            throw new InvokeException(e);
        }
    }
}
