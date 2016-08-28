package invoke;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Reflection {

    private Reflection() {}
    
    public static final Object NOT_FOUND = new Object() {
        @Override public String toString() { return "NOT_FOUND"; };
    };

//    public static Object field(Object self, String name)
//        throws IllegalArgumentException,
//            IllegalAccessException,
//            NoSuchFieldException
//    {
//        try {
//            return self.getClass().getField(name).get(self);
//        } catch (NoSuchFieldException n) {
//            if (self instanceof Class)
//                return ((Class<?>)self).getField(name).get(self);
//            else
//                throw n;
//        }
//    }

    public static Object field(Object self, String name)
        throws IllegalArgumentException,
            IllegalAccessException,
            NoSuchFieldException
    {
        Class<?> cls = self instanceof Class ? (Class<?>)self : self.getClass();
        return cls.getField(name).get(self);
    }

    private static final Map<Class<?>, Class<?>> PRIMITIVES = new HashMap<>();

    public static Class<?> getPrimitive(Class<?> cls) {
        return PRIMITIVES.get(cls);
    }
    
    private static void put(Class<?> primitive, Class<?> wrapper) {
        PRIMITIVES.put(primitive, wrapper);
        PRIMITIVES.put(wrapper, primitive);
    }

    static {
        put(Boolean.TYPE, Boolean.class);
        put(Byte.TYPE, Byte.class);
        put(Character.TYPE, Character.class);
        put(Short.TYPE, Short.class);
        put(Integer.TYPE, Integer.class);
        put(Long.TYPE, Long.class);
        put(Float.TYPE, Float.class);
        put(Double.TYPE, Double.class);
        put(Void.TYPE, Void.class);
    }

    private static class Handler implements InvocationHandler {
        
        private final Method method;
        private final Procedure procedure;
        
        Handler(Method method, Procedure procedure) {
            this.procedure = procedure;
            this.method = method;
        }

        @Override
        public Object invoke(Object self, Method method, Object[] args) throws Throwable {
//            System.out.printf("%s invoked by %s%n", this, method.getName());
            if (!method.equals(this.method))
                return null;
            if (args == null)
                args = new Object[0];
            return procedure.apply(Global.list(args));
        }
        
        @Override
        public String toString() {
            return String.format("Handler(%s)", procedure);
        }
    }

    private static Object proxy(Class<?> type, Procedure procedure) {
        Method method = null;
        for (Method e : type.getDeclaredMethods())
            if ((e.getModifiers() & Modifier.ABSTRACT) != 0)
                if (method == null)
                    method = e;
                else
                    throw new IllegalArgumentException(
                        "type (" + type +") has too many abstract methods.");
        if (method == null)
            throw new IllegalArgumentException(
                "type (" + type +") has no abstract method");
        return Proxy.newProxyInstance(
            procedure.getClass().getClassLoader(),
            new Class<?>[] {type},
            new Handler(method, procedure));
    }

    private static final Object NOT_ASSIGNABLE = new Object() {
        public String toString() { return "NOT_ASSIGNABLE"; }
    };
    
    public static Object assignableValue(Class<?> parmType, Object arg) {
        Object result;
        if (parmType.isPrimitive()) {
            if (arg == null || !PRIMITIVES.get(parmType).isInstance(arg))
                result = NOT_ASSIGNABLE;
            else
                result = arg;
        } else if (arg == null) {
            result = null;
        } else if (parmType.isInterface() && arg instanceof Closure) {
//            System.out.println(parmType + " <- " + arg);
            if (parmType.isAssignableFrom(arg.getClass())) {
//                System.out.println("is implements " + parmType);
                result = arg;
            } else
                result = proxy(parmType, (Closure)arg);
        } else if (!parmType.isInstance(arg)) {
            result = NOT_ASSIGNABLE;
        } else
            result = arg;
//        System.out.printf("assignableValue: %s, %s -> %s%n", parmType, arg, result);
        return result;
    }

    private static Object[] actualArgumentsVarArgs(Executable e, Object... args) {
        Parameter[] parms = e.getParameters();
        int parmSize = parms.length;
        if (parmSize - 1 > args.length)
            return null;
        Object[] actual = new Object[parmSize];
        for (int i = 0; i < parmSize; ++i) {
            Parameter parm = parms[i];
            Class<?> type = parm.getType();
            if (parm.isVarArgs()) {
                int varSize = args.length - i;
                if (varSize == 0 && i >= args.length)
                    actual[i] = null;   // ex. String.format("abc")
                else if (varSize == 1 && type.isInstance(args[i]))
                    actual[i] = args[i];    // ex. String.format("%s%s", new String[]{"a", "b"})
                else {
                    Class<?> ctype = type.getComponentType();
                    Object vars = Array.newInstance(ctype, varSize);
                    for (int j = 0; j < varSize; ++j) {
                        Object assignable = assignableValue(ctype, args[i + j]);
                        if (assignable == NOT_ASSIGNABLE)
                            return null;
                        Array.set(vars, j, assignable);
                    }
                    actual[i] = vars;
                }
            } else {
                Object assignable = assignableValue(type, args[i]);
                if (assignable == NOT_ASSIGNABLE)
                    return null;
                actual[i] = assignable;
            }
        }
        return actual;
    }
    
    private static Object[] actualArgumentsNoVarArgs(Executable e, Object... args) {
        int argsSize = args.length;
        int parmSize = e.getParameterCount();
        if (parmSize != args.length)
            return null;
        Object[] actual = new Object[argsSize];
        Class<?>[] parmType = e.getParameterTypes();
        for (int i = 0; i < parmSize; ++i) {
            Object assignable = assignableValue(parmType[i], args[i]);
            if (assignable == NOT_ASSIGNABLE)
                return null;
            actual[i] = assignable;
        }
        return actual;
    }

    public static Object[] actualArguments(Executable e, Object... args) {
        if (e.isVarArgs())
            return actualArgumentsVarArgs(e, args);
        else
            return actualArgumentsNoVarArgs(e, args);
    }

    public static Object constructor(Object self, Object... args)
        throws InstantiationException,
            IllegalAccessException,
            IllegalArgumentException,
            InvocationTargetException,
            NoSuchMethodException {
        for (Constructor<?> c : ((Class<?>)self).getConstructors()) {
            Object[] actual = actualArguments(c, args);
            if (actual == null)
                continue;
            return c.newInstance(actual);
        }
        throw new NoSuchMethodException(
            "Constructor not found for class=" + self + " args=" + args);
    }

    public static Object method(Class<?> clas, Object self, String name, Object... args)
        throws IllegalAccessException,
            IllegalArgumentException,
            InvocationTargetException,
            NoSuchMethodException {
        for (Method m : clas.getMethods()) {
            if (!m.getName().equals(name))
                continue;
            Object[] actual = actualArguments(m, args);
            if (actual == null)
                continue;
            m.setAccessible(true);
            return m.invoke(self, actual);
        }
        throw new NoSuchMethodException(
            "class=" + clas
            + " name=" + name
            + " args=" + Arrays.deepToString(args));
    }

//    public static Object method(Object self, String name, Object... args)
//        throws IllegalAccessException,
//            IllegalArgumentException,
//            InvocationTargetException {
//        Object result = method(self.getClass(), self, name, args);
//        if (result == NOT_FOUND && self instanceof Class)
//            result = method((Class<?>)self, self, name, args);
//        return result;
//    }

    public static Object method(Object self, String name, Object... args)
        throws IllegalAccessException,
            IllegalArgumentException,
            InvocationTargetException,
            NoSuchMethodException {
        Class<?> cls = self instanceof Class
            ? (Class<?>)self : self.getClass();
        return method(cls, self, name, args);
    }
 
    public static Set<Constructor<?>> findConstructors(Class<?> self) {
        Set<Constructor<?>> results = new HashSet<>();
        for (Constructor<?> e : self.getConstructors())
            results.add(e);
        return results;
    }

    public static Set<Method> findMethods(Object self, String name) {
        Set<Method> results = new HashSet<>();
        for (Method e : self.getClass().getMethods())
            if ((e.getModifiers() & Modifier.STATIC) == 0 && e.getName().equals(name))
                results.add(e);
        if (self instanceof Class)
            for (Method e : ((Class<?>)self).getMethods())
                if (e.getName().equals(name))
                    results.add(e);
        return results;
    }

}
