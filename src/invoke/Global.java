package invoke;

import java.io.StringReader;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;
import reflection.Reflection;

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
    public static final Symbol BACKQUOTE = Symbol.of("backquote");
    public static final Symbol UNQUOTE = Symbol.of("unquote");
    public static final Symbol SPLICE = Symbol.of("splice");

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
                ? env.define((Symbol)caar(args), new Closure(cdar(args), cdr(args), env))
                : env.define((Symbol)car(args), eval(cadr(args), env)));

        defineSyntax("set!", (args, env) ->
            env.set((Symbol)car(args), eval(cadr(args), env)));

        defineSyntax("if", (args, env) ->
            eval(car(args), env) != FALSE ? eval(cadr(args), env)
            : cddr(args) != NIL ? eval(caddr(args), env)
            : UNDEF);

        defineSyntax(LAMBDA, (args, env) ->
            new Closure(car(args), cdr(args), env)
        );
        
        defineSyntax(BACKQUOTE, (args, env) -> backquote(car(args), env));
        
        defineSyntax("define-macro", (args, env) ->
            car(args) instanceof Pair
                ? env.define((Symbol)caar(args),
                    new Macro(new Closure(cdar(args), cdr(args), env)))
                : env.define((Symbol)car(args),
                    new Macro((Closure)eval(cadr(args), env))));

        defineSyntax(INVOKE, (args, env) -> invoke(args, env));
        defineSyntax(FIELD, (args, env) -> field(args, env));
    }
    
    static {
        defineProcedure("car", args -> caar(args));
        defineProcedure("cdr", args -> cdar(args));
        defineProcedure("caar", args -> caaar(args));
        defineProcedure("cadr", args -> cadar(args));
        defineProcedure("cdar", args -> cdaar(args));
        defineProcedure("cddr", args -> cddar(args));
        defineProcedure("caaar", args -> caaar(car(args)));
        defineProcedure("caadr", args -> caadr(car(args)));
        defineProcedure("cadar", args -> cadar(car(args)));
        defineProcedure("caddr", args -> caddr(car(args)));
        defineProcedure("cdaar", args -> cdaar(car(args)));
        defineProcedure("cdadr", args -> cdadr(car(args)));
        defineProcedure("cddar", args -> cddar(car(args)));
        defineProcedure("cdddr", args -> cdddr(car(args)));
        defineProcedure("cons", args -> cons(car(args), cadr(args)));
        defineProcedure("list", args -> args);
        defineProcedure("pair?", args -> car(args) instanceof Pair);
        defineProcedure("null?", args -> car(args) == NIL);
        defineProcedure("eq?", args -> car(args) == cadr(args));
        defineProcedure("equal?", args -> car(args).equals(cadr(args)));
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
        defineProcedure("=", args -> car(args).equals(cadr(args)));
        defineProcedure("<", args -> compare(car(args), cadr(args)) < 0);
        defineProcedure("<=", args -> compare(car(args), cadr(args)) <= 0);
        defineProcedure(">", args -> compare(car(args), cadr(args)) > 0);
        defineProcedure(">=", args -> compare(car(args), cadr(args)) >= 0);
        defineProcedure("+", args -> reduce(0, (a, b) -> (int)a + (int)b, args));
        defineProcedure("-", args -> reduce(0, (a, b) -> (int)a - (int)b, args));
        defineProcedure("*", args -> reduce(1, (a, b) -> (int)a * (int)b, args));
        defineProcedure("/", args -> reduce(1, (a, b) -> (int)a / (int)b, args));
    }
    
    static Object quote(Object obj) {
        return list(QUOTE, obj);
    }

    static Object letStar(Object vars, Object body) {
        return vars == NIL
            ? list(cons(LAMBDA, cons(NIL, body)))
            : list(cons(LAMBDA, list(list(caar(vars)),
                    letStar(cdr(vars), body))),
                cadar(vars));
    }

    /*
     * (define-macro (@ obj .args)
     *    (if (null? args)"
     *        obj"
     *        (if (pair? (car args))"
     *            `(cascade (invoke ,obj ',(caar args) ,@(cdar args)) ,@(cdr args))"
     *            `(cascade (field ,obj ',(car args)) ,@(cdr args)))))
     */
    static Object cascade(Object object, Object methods) {
        if (methods == NIL)
            return object;
        if (car(methods) instanceof Pair)
            return cascade(
                cons(INVOKE,
                    cons(object,
                        cons(caar(methods),
                            cdar(methods)))),
                cdr(methods));
        else
            return cascade(
                list( FIELD, object, car(methods)),
                cdr(methods));
    }

    static {
        defineMacro("let", args ->
            car(args) instanceof Pair
                ? cons(
                    cons(
                        LAMBDA,
                        cons(
                            map(x -> car(x), car(args)),
                            cdr(args))),
                    map(x -> cadr(x), car(args)))
                : list(
                    list(
                        LAMBDA,
                        list(car(args)),
                        list(
                            symbol("set!"),
                            car(args),
                            cons(
                                LAMBDA,
                                cons(
                                    map(x -> car(x), cadr(args)),
                                    cddr(args)))),
                        cons(car(args), map(x -> car(cdr(x)), cadr(args)))),
                    UNDEF));

        defineMacro("let*", args ->
            letStar(car(args), cdr(args)));

        defineMacro("letrec", (Expandable) args ->
            cons(
                cons(
                    LAMBDA,
                    cons(
                        map(x -> car(x), car(args)),
                        append(
                            map(x -> cons(symbol("set!"), x), car(args)),
                            cdr(args)))),
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

    public static Object car(Object obj) { return ((List) obj).car(); }
    public static Object cdr(Object obj) { return ((List) obj).cdr(); }
    public static Object caar(Object obj) { return car(car(obj)); }
    public static Object cadr(Object obj) { return car(cdr(obj)); }
    public static Object cdar(Object obj) { return cdr(car(obj)); }
    public static Object cddr(Object obj) { return cdr(cdr(obj)); }
    public static Object caaar(Object obj) { return car(car(car(obj))); }
    public static Object caadr(Object obj) { return car(car(cdr(obj))); }
    public static Object cadar(Object obj) { return car(cdr(car(obj))); }
    public static Object caddr(Object obj) { return car(cdr(cdr(obj))); }
    public static Object cdaar(Object obj) { return cdr(car(car(obj))); }
    public static Object cdadr(Object obj) { return cdr(car(cdr(obj))); }
    public static Object cddar(Object obj) { return cdr(cdr(car(obj))); }
    public static Object cdddr(Object obj) { return cdr(cdr(cdr(obj))); }

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

    static Object evlis(Object args, Env env) {
        List.Builder b = new List.Builder();
        for (; args instanceof Pair; args = cdr(args))
            b.tail(eval(car(args), env));
        return b.build();
    }

    public static Object field(Object args, Env env) {
        Object self = eval(car(args), env);
        String name = ((Symbol)cadr(args)).name;
        try {
            return Reflection.field(self, name);
        } catch (IllegalArgumentException
            | IllegalAccessException
            | NoSuchFieldException e) {
            throw new InvokeException(e);
        }
    }

    public static Object invoke(Object args, Env env) {
        Object self = eval(car(args), env);
        String name = ((Symbol)cadr(args)).name;
        Object[] arguments = ((List)evlis(cddr(args), env)).toArray();
        try {
            if (name.equals("new"))
                return Reflection.construct((Class<?>)self, arguments);
            else
                return Reflection.invoke(self, name, arguments);
        } catch (InstantiationException
            | IllegalAccessException
            | IllegalArgumentException
            | InvocationTargetException
            | NoSuchMethodException e) {
            throw new InvokeException(e);
        }
    }

    static Object backquote(Object obj, Env env) {
        if (obj instanceof Pair)
            if (car(obj) instanceof Pair)
                if (caar(obj) == UNQUOTE)
                    return cons(
                        eval(cadar(obj), env),
                        backquote(cdr(obj), env));
                else if (car(car(obj)) == SPLICE)
                    return append(
                        eval(cadar(obj), env),
                        backquote(cdr(obj), env));
                else
                    return cons(
                        backquote(car(obj), env),
                        backquote(cdr(obj), env));
            else
                return cons(car(obj), backquote(cdr(obj), env));
        else
            return obj;
    }

}
