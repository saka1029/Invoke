package invoke;

public class UserMacro implements Macro {

    private final Closure closure;
    
    UserMacro(Closure closure) {
        this.closure = closure;
    }

    @Override
    public Object expand(Object args) {
        return closure.apply(args);
    }

}
