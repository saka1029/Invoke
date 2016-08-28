package invoke;

public class InvokeException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InvokeException(String format, Object... args) {
        super(String.format(format, args));
    }
    
    public InvokeException(Throwable t) {
        super(t);
    }
}
