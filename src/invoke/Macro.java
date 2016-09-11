package invoke;

import static invoke.Global.*;

public interface Macro extends Applicable {
    
    Object expand(Object args);
    
    @Override
    default Object apply(Object args, Env env) {
        Object expanded = expand(args);
        System.out.println("expand: " + args + " -> " + expanded);
        return eval(expanded, env);
    }

}
