package invoke;

public class Macro implements Expandable {

    private final Closure closure;
    
    Macro(Closure closure) {
        this.closure = closure;
    }

    @Override
    public Object expand(Object args) {
        return closure.apply(args);
    }

}
