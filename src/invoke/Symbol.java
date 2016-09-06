package invoke;

import java.util.HashMap;
import java.util.Map;

public class Symbol implements Evaluable {

    private static final Map<String, Symbol> map = new HashMap<>();
    final String name;
    
    private Symbol(String name) {
        this.name = name;
    }
    
    public static Symbol of(String name) {
        return map.computeIfAbsent(name, n -> new Symbol(n));
    }
    
    public static class Keyword extends Symbol {
        private Keyword(String name) {
            super(name);
        }
        
        public static Symbol of(String name) {
            return map.computeIfAbsent(name, n -> new Keyword(n));
        }
        
        @Override
        public Object eval(Env env) {
            return this;
        }
    }
    
    @Override
    public String toString() {
        return name;
    }

    @Override
    public Object eval(Env env) {
        return env.get(this);
    }
    
}
