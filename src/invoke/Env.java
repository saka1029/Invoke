package invoke;

import java.util.HashMap;
import java.util.Map;

public class Env {
    
    private final Env next;
    private final Map<Symbol, Object> map = new HashMap<>();
    
    Env(Env next) {
        this.next = next;
    }
    
    private Env find(Symbol key) {
        for (Env env = this; env != null; env = env.next)
            if (env.map.containsKey(key))
                return env;
        throw new InvokeException("Variable %s not defined", key);
    }

    public Object get(Symbol key) {
        return find(key).map.get(key);
    }
    
    public Object set(Symbol key, Object value) {
        find(key).map.put(key, value);
        return Global.UNDEF;
    }

    public Object define(Symbol key, Object value) {
        map.put(key, value);
        return Global.UNDEF;
    }
}
