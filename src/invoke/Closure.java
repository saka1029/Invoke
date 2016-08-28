package invoke;

import static invoke.Global.*;

public class Closure implements Procedure {

    private final Object parms;
    private final Object body;
    private final Env env;
    
    Closure(Object parms, Object body, Env env) {
        this.parms = parms;
        this.body = body;
        this.env = env;
    }
    
    static void pairlis(Object parms, Object args, Env env) {
        for (; parms instanceof Pair; parms = cdr(parms), args = cdr(args))
            env.define((Symbol)car(parms), car(args));
        if (parms != NIL)
            env.define((Symbol)parms, args);
    }

    static Object progn(Object body, Env env) {
        if (cdr(body) == NIL)
            return eval(car(body), env);
        eval(car(body), env);
        return progn(cdr(body), env);
    }

    @Override
    public Object apply(Object args) {
        Env n = new Env(env);
        pairlis(parms, args, n);
        return progn(body, n);
    }
    
    @Override
    public String toString() {
        return cons(symbol("closure"), cons(parms, body)).toString();
    }
}
