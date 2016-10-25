package types;

import java.util.HashSet;
import java.util.Set;

public interface Domain {
    
    public static Domain ANY = new DomainAny();

    public static Domain of(Class<?>... elements) {
        return new DomainSet(elements);
    }
}

class DomainAny implements Domain {
    
}

class DomainSet implements Domain {
    
    final Set<Class<?>> elements = new HashSet<>();
    
    DomainSet(Class<?>... elements) {
        for (Class<?> e : elements)
            this.elements.add(e);
    }

}
