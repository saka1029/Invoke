package invoke;

public class Pair implements List {
    
    Object car, cdr;
    
    public Pair(Object car, Object cdr) {
        this.car = car;
        this.cdr = cdr;
    }
    
    public Object car() { return car; }
    public Object cdr() { return cdr; }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Pair))
            return false;
        Pair o = (Pair)obj;
        return o.car.equals(car) && o.cdr.equals(cdr);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(").append(car);
        Object obj = cdr;
        for (; obj instanceof Pair; obj = ((Pair)obj).cdr)
            sb.append(" ").append(((Pair)obj).car);
        if (obj != NIL)
            sb.append(" . ").append(obj);
        sb.append(")");
        return sb.toString();
    }

    @Override
    public Object eval(Env env) {
        return ((Applicable)Global.eval(car, env)).apply(cdr, env);
    }

}
