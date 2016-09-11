# Invoke


## intのオーバーフロー検出

```
static final int safeMultiply(int left, int right) throws ArithmeticException {
  if (right > 0 ? left > Integer.MAX_VALUE/right || left < Integer.MIN_VALUE/right :
      (right < -1 ? left > Integer.MIN_VALUE/right || left < Integer.MAX_VALUE/right :
      right == -1 && left == Integer.Min_VALUE) ) {
    throw new ArithmeticException("Integer overflow");
  }
  return left * right;
}
```