

```
.class public Hello
.super java/lang/Object

.method public ()V
   aload_0
   invokespecial java/lang/Object/()V
   return
.end method

.method public static main([Ljava/lang/String;)V
   .limit stack 2
   getstatic java/lang/System/out Ljava/io/PrintStream;
   ldc "Hello Jasmin!!"
   invokevirtual java/io/PrintStream/println(Ljava/lang/String;)V
   return
.end method
```


```
public class Hello {

    public static void main(String[] args) {
        @java.lang.System.out "Hello Jasmin!!" println
    }
}
```

`java.lang`パッケージはデフォルトでインポートされる。

```
public class Hello {

    public static void main(String[] args) {
        @System.out "Hello Jasmin!!" println
    }
}
```

`println`がメソッド呼び出しなのはわかるが、
呼び出しの対象となるオブジェクトが
`@System.out`なのか`"Hello Jasmin!!"`なのかがわからない。
リフレクションで判別できることはできるが、不明なケースはありうる。

```
public class A {
    static class B {
        public void foo() {}
    }
    static class C extends B {
        public void foo() {}
    }
    public static void main(String[] args) {
        B c = new C();
        c.foo();
    }
}
```

これを逆アセンブルするとこうなる。


```
> javap -p -c A.class
Compiled from "A.java"
public class A {
  public A();
    Code:
       0: aload_0
       1: invokespecial #1                  // Method java/lang/Object."<init>":()V
       4: return

  public static void main(java.lang.String[]);
    Code:
       0: new           #2                  // class A$C
       3: dup
       4: invokespecial #3                  // Method A$C."<init>":()V
       7: astore_1
       8: aload_1
       9: invokevirtual #4                  // Method A$B.foo:()V
      12: return
}
```

`c.foo()`の呼び出しは`B.foo()`の`invokevirtual`で行われる。
つまり実体の型とは関係なく、宣言した型のメソッドが呼び出される。
