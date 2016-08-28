package invoke;

import static invoke.Global.*;

public interface Procedure extends Applicable {

    Object apply(Object args);
    
    static Object evlis(Object args, Env env) {
        List.Builder b = new List.Builder();
        for (; args instanceof Pair; args = cdr(args))
            b.tail(eval(car(args), env));
        return b.build();
    }

    @Override
    default Object apply(Object args, Env env) {
        return apply(evlis(args, env));
    }
}
