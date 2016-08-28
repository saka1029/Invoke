package invoke;

public interface List extends Evaluable {
    
    default Object car() { throw new InvokeException("cannot car of %s", this); }
    default Object cdr() { throw new InvokeException("cannot cdr of %s", this); }
    
    static final List NIL = new List() {

        public String toString() {
            return "()";
        }

        @Override
        public Object eval(Env env) {
            return this;
        };
    };

    public static class Builder {

        Object head = NIL;
        Object tail = NIL;
        
        public Builder head(Object e) {
            head = new Pair(e, head);
            if (!(tail instanceof Pair))
                tail = head;
            return this;
        }
        
        public Builder tail(Object e) {
            if (tail instanceof Pair)
                tail = ((Pair)tail).cdr = new Pair(e, ((Pair)tail).cdr);
            else
                head = tail = new Pair(e, tail);
            return this;
        }
        
        public Builder last(Object e) {
            if (tail instanceof Pair)
                ((Pair)tail).cdr = e;
            else
                head = tail = e;
            return this;
        }
        
        public Object build() {
            return head;
        }

    }
}
