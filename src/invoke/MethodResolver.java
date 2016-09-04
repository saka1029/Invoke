package invoke;

import java.util.HashMap;
import java.util.Map;
import static invoke.Global.symbol;

public class MethodResolver {
    
    private MethodResolver() {}
    
    private static final Map<Symbol, String> NAME_MAP
        = new HashMap<>();
    
    static {
        NAME_MAP.put(symbol("+"), "plus");
        NAME_MAP.put(symbol("-"), "minus");
        NAME_MAP.put(symbol("*"), "multiply");
        NAME_MAP.put(symbol("/"), "divide");
        NAME_MAP.put(symbol("%"), "mod");
        NAME_MAP.put(symbol("^"), "xor");
        NAME_MAP.put(symbol("=="), "eq");
        NAME_MAP.put(symbol("!="), "ne");
        NAME_MAP.put(symbol(">"), "gt");
        NAME_MAP.put(symbol(">="), "ge");
        NAME_MAP.put(symbol("<"), "lt");
        NAME_MAP.put(symbol("<="), "le");
    }
    
    static String name(Symbol key) {
        String result = NAME_MAP.get(key);
        return result != null ? result : key.name;
    }

}
