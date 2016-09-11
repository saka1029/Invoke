package invoke;

import java.io.StringReader;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.function.UnaryOperator;
import reflection.Reflection;

public class Global {

    private Global() {
    }

    public static final Env ENV = new Env(null);
    public static final Symbol QUOTE = Symbol.of("quote");
    public static final Symbol LAMBDA = Symbol.of("lambda");
    public static final Symbol INVOKE = Symbol.of("invoke");
    public static final Symbol FIELD = Symbol.of("field");
    public static final Symbol UNDEF = Symbol.Keyword.of("*UNDEF*");
    public static final List NIL = List.NIL;
    public static final Boolean TRUE = Boolean.TRUE;
    public static final Boolean FALSE = Boolean.FALSE;
    public static final Symbol BACKQUOTE = Symbol.of("backquote");
    public static final Symbol UNQUOTE = Symbol.of("unquote");
    public static final Symbol SPLICE = Symbol.of("splice");
    public static final Symbol CASCADE = Symbol.of("@");
    public static final Symbol LANG = symbol("Lang");
    public static final Symbol CONS = symbol("cons");
    public static final Symbol APPEND = symbol("append");

    static {
        define(symbol("Boolean"), Boolean.class);
        define(symbol("Class"), Class.class);
        define(symbol("String"), String.class);
        define(symbol("Integer"), Integer.class);
        define(symbol("int"), Integer.TYPE);
        define(symbol("System"), System.class);
        define(symbol("Array"), Array.class);
        define(LANG, Lang.class);
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
            !eval(car(args), env).equals(FALSE) ? eval(cadr(args), env)
            : cddr(args) != NIL ? eval(caddr(args), env)
            : UNDEF);

        defineSyntax(LAMBDA, (args, env) ->
            new Closure(car(args), cdr(args), env)
        );
        
//        defineSyntax(BACKQUOTE, (args, env) -> backquote(car(args), env));
        
        defineSyntax("define-macro", (args, env) ->
            car(args) instanceof Pair
                ? ENV.define((Symbol)caar(args),
                    new UserMacro(new Closure(cdar(args), cdr(args), env)))
                : ENV.define((Symbol)car(args),
                    new UserMacro((Closure)eval(cadr(args), env))));

        defineSyntax(INVOKE, (args, env) -> invoke(args, env));
        defineSyntax(FIELD, (args, env) -> field(args, env));
    }
    
    static {
        defineMacro("car", LANG, "car", 1);
        defineMacro("cdr", LANG, "cdr", 1);
        defineMacro("caar", LANG, "caar", 1);
        defineMacro("cadr", LANG, "cadr", 1);
        defineMacro("cdar", LANG, "cdar", 1);
        defineMacro("cddr", LANG, "cddr", 1);
        defineMacro("cons", LANG, "cons", 2);
        defineMacro("list", LANG, "list", -1);
        defineMacro("append", LANG, "append", -1);
        defineMacro("pair?", LANG, "pairp", 1);
        defineMacro("null?", LANG, "nullp", 1);
        defineMacro("eq?", LANG, "eqp", 2);
        defineMacro("equal?", LANG, "equalp", 2);
        defineMacro("display", LANG, "display", -1);
        defineMacro(BACKQUOTE, (Macro) args -> backquote(car(args)));
    }
    
    static {
        defineMacro("==", LANG, "eq", 2);
        defineMacro("!=", LANG, "ne", 2);
        defineMacro("<", LANG, "lt", 2);
        defineMacro("<=", LANG, "le", 2);
        defineMacro(">", LANG, "gt", 2);
        defineMacro(">=", LANG, "ge", 2);
        defineMacro("+", LANG, "plus", -2, 0);
        defineMacro("-", LANG, "minus", -2, 0);
        defineMacro("*", LANG, "multiply", -2, 1);
        defineMacro("/", LANG, "divide", -2, 1);
    }
    
    static Object letStar(Object vars, Object body) {
        return vars == NIL
            ? list(list(LAMBDA, NIL, splice(body)))
            : list(list(LAMBDA, splice(list(list(caar(vars)), letStar(cdr(vars), body)))),
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
    static Object cascade(Object object, Object args) {
//        if (args == NIL)
//            return object;
//        if (car(args) instanceof Pair)
//            return cascade(
//                cons(INVOKE,
//                    cons(object,
//                        cons(caar(args),
//                            cdar(args)))),
//                cdr(args));
//        else
//            return cascade(
//                list( FIELD, object, car(args)),
//                cdr(args));
        return args == NIL
            ? object
            : car(args) instanceof Pair
                ? cascade(list(INVOKE, object, caar(args), splice(cdar(args))), cdr(args))
                : cascade(list(FIELD, object, car(args)), cdr(args));
    }

    /*
     * (let loop ((r 1) (i 1))
     *     (if (> i n)
     *         r
     *         (loop (* r i) (+ i 1))))
     * 
     * ->
     * 
     * (
     *     (lambda (loop)
     *         (set! loop
     *             (lambda (r i)
     *                 (if (> i n)
     *                     r
     *                     (loop (* r i) (+ i 1)))))
     *         (loop 1 1))
     *     *UNDEF*)
     * 
     * (define-macro let
     *   (lambda (args . body)
     *     (if (pair? args)
     *         `((lambda ,(map car args) ,@body) ,@(map cadr args))
     *       `(letrec ((,args (lambda ,(map car (car body)) ,@(cdr body))))
     *         (,args ,@(map cadr (car body)))))))
     */
    static {
        defineMacro("let", args ->
//            car(args) instanceof Pair
//                ? cons(cons(LAMBDA,
//                    cons(map(x -> car(x), car(args)), 
//                        list(cdr(args))),
//                    map(x -> cadr(x), car(args)))
//                : list(list(LAMBDA,
//                    list(car(args)),
//                    list(symbol("set!"), car(args),
//                        cons(LAMBDA, cons(
//                            map(x -> car(x), cadr(args)),
//                            cddr(args)))),
//                        cons(car(args), map(x -> cadr(x), cadr(args)))),
//                    UNDEF));
            car(args) instanceof Pair
                ? list(
                    list(LAMBDA, map(x -> car(x), car(args)), splice(cdr(args))),
                    splice(map(x -> cadr(x), car(args))))
                : list(
                    list(LAMBDA,
                        list(car(args)),
                        list(symbol("set!"), car(args),
                            list(LAMBDA, map(x -> car(x), cadr(args)), splice(cddr(args)))),
                        list(car(args), splice(map(x -> cadr(x), cadr(args))))),
                    UNDEF));


        defineMacro("let*", args ->
            letStar(car(args), cdr(args)));

        /*
         * (letrec (
         *     (f (lambda (i)
         *         (if (<= i 1)
         *             1
         *             (* i (f (- i 1)))))))
         *     (f n))
         * 
         * ->
         * 
         * (
         *     (lambda (f)
         *         (set! f (lambda (i)
         *             (if (<= i 1)
         *                 1
         *                 (* i (f (- i 1))))))
         *         (f n))
         *     false)
         * 
         */
        defineMacro("letrec", args ->
//            cons(
//                cons(
//                    LAMBDA,
//                    cons(
//                        map(x -> car(x), car(args)),
//                        append(
//                            map(x -> cons(symbol("set!"), x), car(args)),
//                            cdr(args)))),
//                map(x -> UNDEF, car(args))));
            list(
                list(LAMBDA,
                    map(x -> car(x), car(args)),
                    splice(map(x -> cons(symbol("set!"), x), car(args))),
                    splice(cdr(args))),
                splice(map(x -> UNDEF, car(args)))));

        defineMacro("begin", args -> letStar(NIL, args));
        defineMacro(CASCADE, args -> cascade(car(args), cdr(args)));
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

//    static void defineProcedure(Symbol key, Procedure value) {
//        define(key, value);
//    }
//
//    static void defineProcedure(String name, Procedure value) {
//        define(symbol(name), value);
//    }

    static class NamedExpandable implements Macro {
        
        Macro expandable;
        Symbol name;

        NamedExpandable(Symbol name, Macro expandable) {
            this.expandable = expandable;
            this.name = name;
        }

        @Override
        public Object apply(Object args, Env env) {
            Object expanded = expand(args);
            Object evaled = eval(expanded, env);
            System.out.println("expand: " + new Pair(name, args) + " -> " + expanded + " -> " + evaled);
            return evaled;
        }

        @Override
        public Object expand(Object args) {
            return expandable.expand(args);
        }
    }

    static void defineMacro(Symbol symbol, Macro value) {
        define(symbol, new NamedExpandable(symbol, value));
    }

    static void defineMacro(String name, Macro value) {
        defineMacro(symbol(name), value);
    }
    
    public static Object multiaryOperator(Symbol self, Symbol method, Object unit, Object args) {
        Object prev = null;
        int i = 0;
        for (; args instanceof Pair; args = cdr(args)) {
            Object e = car(args);
            switch (i++) {
            case 0: prev = e; break;
            case 1: unit = prev; break;
            }
            unit = list(INVOKE, self, method, unit, e);
        }
        return unit;
    }

    static void defineMacro(String name, Symbol cls, String methodName, int argSize, Object unit) {
        Symbol method = symbol(methodName);
        Macro value;
        switch (argSize) {
        case -2: value = args -> multiaryOperator(cls, method, unit, args); break;
        case -1: value = args -> list(INVOKE, cls, method, splice(args)); break;
        case 0: value = args -> list(INVOKE, cls, method); break;
        case 1: value = args -> list(INVOKE, cls, method, car(args)); break;
        case 2: value = args -> list(INVOKE, cls, method, car(args), cadr(args)); break;
        case 3: value = args -> list(INVOKE, cls, method, car(args), cadr(args), caddr(args)); break;
        default: throw new IllegalArgumentException("too many args");
        }
        defineMacro(name, value);
    }
    static void defineMacro(String name, Symbol cls, String methodName, int argSize) {
        defineMacro(name, cls, methodName, argSize, null);
    }

    public static Object eval(Object obj, Env env) {
        if (obj instanceof Evaluable)
            return ((Evaluable) obj).eval(env);
        else
            return obj;
    }

    public static Object eval(Object obj) {
        Object evaled = eval(expandAll(obj), ENV);
//        Object evaled = eval((obj), ENV);
        System.out.println("eval: " + obj + " -> " + evaled);
        return evaled;
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
    
    static class Splice {

        Object list;

        public Splice(Object list) {
            if (!(list instanceof List))
                throw new IllegalArgumentException("list");
            this.list = list;
        }

        @Override
        public String toString() {
            return String.format("(Splice %s)", list);
        }
    }
    
    public static Splice splice(Object element) {
        return new Splice(element);
    }

    public static Object list(Object... objects) {
//        Object r = NIL;
//        for (int i = objects.length - 1; i >= 0; --i)
//            r = cons(objects[i], r);
//        return r;
        List.Builder b = new List.Builder();
        for (Object e : objects)
            if (e instanceof Splice)
                for (Object f = ((Splice)e).list; f instanceof Pair; f = cdr(f))
                    b.tail(car(f));
            else
                b.tail(e);
        return b.build();
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

    static Object evlis(Object args, Env env) {
        List.Builder b = new List.Builder();
        for (; args instanceof Pair; args = cdr(args))
            b.tail(eval(car(args), env));
        return b.build();
    }

    static Object field(Object args, Env env) {
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

    static Object invoke(Object args, Env env) {
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

    /**
     * Syntax版backquote
     * 事前にexpandAll()を使ってマクロ展開するのが難しい
     */
//    static Object backquote(Object obj, Env env) {
//        if (obj instanceof Pair)
//            if (car(obj) instanceof Pair)
//                if (caar(obj) == UNQUOTE)
//                    return cons(
//                        eval(cadar(obj), env),
//                        backquote(cdr(obj), env));
//                else if (car(car(obj)) == SPLICE)
//                    return append(
//                        eval(cadar(obj), env),
//                        backquote(cdr(obj), env));
//                else
//                    return cons(
//                        backquote(car(obj), env),
//                        backquote(cdr(obj), env));
//            else
//                return cons(car(obj), backquote(cdr(obj), env));
//        else
//            return obj;
//    }

    /**
     * マクロ版のbackquote
     * @param obj
     * @return
     */
    public static Object backquote(Object obj) {
        if (obj instanceof Pair)
            if (car(obj) instanceof Pair)
                if (caar(obj) == UNQUOTE)
//                    return cons(
//                        eval(cadar(obj), env),
//                        backquote(cdr(obj), env));
                    return list(CONS, cadar(obj), backquote(cdr(obj)));
                else if (car(car(obj)) == SPLICE)
//                    return append(
//                        eval(cadar(obj), env),
//                        backquote(cdr(obj), env));
                    return list(APPEND, cadar(obj), backquote(cdr(obj)));
                else
//                    return cons(
//                        backquote(car(obj), env),
//                        backquote(cdr(obj), env));
                    return list(CONS, backquote(car(obj)), backquote(cdr(obj)));
            else
//                return cons(car(obj), backquote(cdr(obj), env));
                return list(CONS, list(QUOTE, car(obj)), backquote(cdr(obj)));
        else
//            return obj;
            return list(QUOTE, obj);
    }
    
    static Object expandAllArgs(Object exp) {
        return map(x -> expandAll(x), exp);
    }

    public static Object expandAll(Object exp) {
        if (!(exp instanceof Pair)) 
            return exp;
        Object head = ((Pair)exp).car();
        Object tail = ((Pair)exp).cdr();
        if (head instanceof Pair)
            return map(x -> expandAll(x), exp);
        if (!(head instanceof Symbol))
            throw new IllegalArgumentException("exp: " +exp);
        Object applicable = ENV.getValue((Symbol)head);
        if (applicable instanceof Macro)
            return expandAll(((Macro)applicable).expand(tail));
        if (head == QUOTE)
            return exp;
        if (head == symbol("invoke"))
            return list(head, expandAll(car(tail)), cadr(tail), splice(expandAllArgs(cddr(tail))));
        if (head == symbol("field"))
            return list(head, expandAll(car(tail)), cadr(tail));
        if (head == symbol("if"))
            return list(head, splice(expandAllArgs(tail)));
        if (head == symbol("lambda"))
            return list(head, car(tail), splice(expandAllArgs(cdr(tail))));
        if (head == symbol("define") || head == symbol("define-macro") || head == symbol("set!"))
            return list(head, car(tail), expandAll(cadr(tail)));
        return map(x -> expandAll(x), exp);
    }

}
