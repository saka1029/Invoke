#bcel

```
public static int fac(int n) {
    if (n == 0)
        return 1;
    else
        return n * fac(n - 1);
}
```

```
        0:  iload_0
        1:  ifne            #8
        4:  iconst_1
        5:  goto            #16
        8:  iload_0
        9:  iload_0
        10: iconst_1
        11: isub
        12: invokestatic    Factorial.fac (I)I (12)
        15: imul
        16: ireturn
```

```
public static int fac(int n) {
    @n 0 != (1) (@n dup 1 - fac *) if
}
```

```
    @n          I
    0           I I
    !=          B
    (
        1       I
    )
    (
        @n      I
        dup     I I
        1       I I I
        -       I I
        fac     I I
        *       I
     )
                B P P
     if         I           メソッドの末尾でStackにIntegerがあるのでireturnする。
````
