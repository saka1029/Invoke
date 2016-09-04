package invoke;

import static invoke.Global.*;

import reflection.Functional;

public interface Procedure extends Applicable, Functional {

    Object apply(Object args);
    
    @Override
    default Object apply(Object args, Env env) {
        return apply(evlis(args, env));
    }
    
    @Override
    default Object apply(Object[] args) {
        return apply(list(args));
    }
}
