package invoke;

import java.util.HashMap;
import java.util.Map;

public class Symbol implements Evaluable {

    private static final Map<String, Symbol> map = new HashMap<>();
    private final String name;
    
    private Symbol(String name) {
        this.name = name;
    }
    
    public static Symbol of(String name) {
        return map.computeIfAbsent(name, n -> new Symbol(n));
    }
    
    public static Symbol selfEvaluatedOf(String name) {
        return map.computeIfAbsent(name, n -> new SelfEvaluatedSymbol(n));
    }
    
    public static class SelfEvaluatedSymbol extends Symbol {
        private SelfEvaluatedSymbol(String name) {
            super(name);
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
