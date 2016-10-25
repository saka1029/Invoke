# Invoke

## 「+」関数はマクロで実装

「+」マクロの定義は以下のようになっている。

```
    defineMacro("+", LANG, "plus", -2, 0);
    
    public static Object multiaryOperator(Symbol self, Symbol method, Object unit, Object args) {
        Object prev = null;
        int i = 0;
        for (; args instanceof Pair; args = cdr(args), ++i) {
            Object e = car(args);
            switch (i) {
            case 0: prev = e; break;
            case 1: unit = prev; break;
            }
            unit = list(INVOKE, self, method, unit, e);
        }
        return unit;
    }

    static void defineMacro(String name, Symbol cls, String methodName, int argSize, Object unit) {
        Symbol method = symbol(methodName);
        Macro value;
        switch (argSize) {
        case -2: value = args -> multiaryOperator(cls, method, unit, args); break;
        case -1: value = args -> list(INVOKE, cls, method, splice(args)); break;
        case 0: value = args -> list(INVOKE, cls, method); break;
        case 1: value = args -> list(INVOKE, cls, method, car(args)); break;
        case 2: value = args -> list(INVOKE, cls, method, car(args), cadr(args)); break;
        case 3: value = args -> list(INVOKE, cls, method, car(args), cadr(args), caddr(args)); break;
        default: throw new IllegalArgumentException("too many args");
        }
        defineMacro(name, value);
    }

    static void defineMacro(String name, Symbol cls, String methodName, int argSize) {
        defineMacro(name, cls, methodName, argSize, null);
    }

```

これにより以下のようにマクロ展開される

```
(+) -> 0
(+ 2) -> (invoke Lang plus 0 2)
(+ 2 3) -> (invoke Lang plus 2 3)
(+ 2 3 4) -> (invoke Lang plus (invoke Lang plus 2 3) 4)
```

`(invoke OBJECT METHOD ARGS...)`は`ARGS`を`OBJECT`の`METHOD`に対して適用する。

Langクラスには以下のstaticメソッドがあるのでinvokeはこれを呼び出している。

```
    public static int plus(int a, int b) { return a + b; }
```

Langクラスに以下を追加すると

```
    public static String plus(String a, Object b) { return a + b; }
```

こんなことができるようになる。

```
(+ "abc" "def") -> "abcdef"
(+ abc 123 456) -> "abc123456"
```

「+」演算子は実質的にジェネリックな関数となる。
