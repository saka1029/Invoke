package reflection;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Reflection {

    private Reflection() {}
    
    static Object CAST_FAILED = new Object() {
        public String toString() { return "CAST_FAILED"; };
    };

    private static Map<Class<?>, Class<?>> PRIMITIVE_TO_DERIVATIVE = new HashMap<>();
    static {
        PRIMITIVE_TO_DERIVATIVE.put(Byte.TYPE, Byte.class);
        PRIMITIVE_TO_DERIVATIVE.put(Short.TYPE, Short.class);
        PRIMITIVE_TO_DERIVATIVE.put(Character.TYPE, Character.class);
        PRIMITIVE_TO_DERIVATIVE.put(Integer.TYPE, Integer.class);
        PRIMITIVE_TO_DERIVATIVE.put(Long.TYPE, Long.class);
        PRIMITIVE_TO_DERIVATIVE.put(Float.TYPE, Float.class);
        PRIMITIVE_TO_DERIVATIVE.put(Double.TYPE, Double.class);
        PRIMITIVE_TO_DERIVATIVE.put(Void.TYPE, Void.class);
    }

    /**
     * convert class cls to derivative class.
     * @param cls
     * @return  returns corrsponding derivative class when cls is primitive class.
     *          returns cls when cls is not primitive class.
     */
    static Class<?> toDerivative(Class<?> cls) {
        return PRIMITIVE_TO_DERIVATIVE.computeIfAbsent(cls, key -> key);
    }
    
    private static class Handler implements InvocationHandler {
        
        private final Method method;
        private final Functional handler;
        
        Handler(Method method, Functional handler) {
            this.handler = handler;
            this.method = method;
        }

        @Override
        public Object invoke(Object self, Method method, Object[] args) throws Throwable {
            if (!method.equals(this.method))
                return null;
            if (args == null)
                args = new Object[0];
            return handler.apply(args);
        }
        
    }

    static Method findFunctionalMethod(Class<?> cls) {
        Method result = null;
        L: for (Method method : cls.getMethods()) {
            int m = method.getModifiers();
            if ((m & Modifier.ABSTRACT) == 0)
                continue;
            String name = method.getName();
            Class<?>[] types = method.getParameterTypes();
            for (Method om : Object.class.getMethods())
                if (om.getName().equals(name)
                    && Arrays.equals(om.getParameterTypes(), types))
                    continue L;
            if (result == null)
                result = method;
            else
                return null;
        }
        return result;
    }

    static Object cast(Object value, Class<?> type) {
        if (type.isPrimitive()) {
            if (value == null)
                return CAST_FAILED;
            type = toDerivative(type);
        }
        if (value == null)
            return null;
        Class<?> valueClass = value.getClass();
        if (type.isAssignableFrom(valueClass))
            return value;
        else if (type.isInterface() && value instanceof Functional) {
            Method method = findFunctionalMethod(type);
            if (method == null)
                return CAST_FAILED;
            else
                return Proxy.newProxyInstance(
                    valueClass.getClassLoader(),
                    new Class<?>[] {type},
                    new Handler(method, (Functional)value));
        } else
            return CAST_FAILED;
    }
    
    static Object[] cast(Object[] args, Parameter[] types) {
        int size = types.length;
        Object[] result = new Object[size];
        for (int i = 0; i < size; ++i) {
            if (i >= args.length) return null;
            Object arg = args[i];
            Parameter type = types[i];
            if (type.isVarArgs()) {
                int vsize = args.length - i;
                Class<?> vtype = type.getType().getComponentType();
                Object vresult = Array.newInstance(vtype, vsize);
                for (int j = 0; j < vsize; ++j) {
                    Object cast = cast(args[i + j], vtype);
                    if (cast == CAST_FAILED) return null;
                    Array.set(vresult, j, cast);
                }
                result[i] = vresult;
            } else {
                Object cast = cast(arg, type.getType());
                if (cast == CAST_FAILED) return null;
                result[i] = cast;
            }
        }
        return result;
    }
    
    public static Object construct(Class<?> type, Object... args)
        throws InstantiationException,
            IllegalAccessException,
            IllegalArgumentException,
            InvocationTargetException,
            NoSuchMethodException {
        for (Constructor<?> c : type.getConstructors()) {
            int size = c.getParameterCount();
            Parameter[] parms = c.getParameters();
            if (c.isVarArgs() && args.length < size - 1) continue;
            if (!c.isVarArgs() && args.length != size) continue;
            Object[] actual = cast(args, parms);
            if (actual != null) {
                c.setAccessible(true);
                return c.newInstance(actual);
            }
        }
        throw new NoSuchMethodException(String.format(
            "constructor class=%s args=%s", type, Arrays.deepToString(args)));
    }
    
    public static Object invoke(Object self, String methodName, Object... args)
        throws IllegalAccessException,
            IllegalArgumentException,
            InvocationTargetException,
            NoSuchMethodException  {
        Class<?> type = self instanceof Class<?> ? (Class<?>)self : self.getClass();
        for (Method m : type.getMethods()) {
            int size = m.getParameterCount();
            Parameter[] parms = m.getParameters();
            if (!m.getName().equals(methodName)) continue;
            if (m.isVarArgs() && args.length < size - 1) continue;
            if (!m.isVarArgs() && args.length != size) continue;
            Object[] actual = cast(args, parms);
            if (actual != null) {
                m.setAccessible(true);
                return m.invoke(self, actual);
            }
        }
        throw new NoSuchMethodException(String.format(
            "class=%s name=%s args=%s", type, methodName, Arrays.deepToString(args)));
    }
    
    public static Object field(Object self, String fieldName)
        throws IllegalArgumentException,
            IllegalAccessException,
            NoSuchFieldException,
            SecurityException {
        Class<?> type = self instanceof Class<?> ? (Class<?>)self : self.getClass();
        Field field = type.getField(fieldName);
        field.setAccessible(true);
        return field.get(self);
    }
    
}
