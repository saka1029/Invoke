package test.invoke;

import static org.junit.Assert.*;

import static invoke.Global.*;

import org.junit.Test;

/*
 * 全マクロ展開は単純なマクロ展開と違って、
 * 評価される部分式についてもマクロ展開を行う。
 * 
 * [Syntaxの場合]
 * 評価される部分式がどこにあるかわからないので、
 * それぞれ個別の展開機能が必要となる。
 * Expandableインタフェースを作成する。
 * 評価される個別部分式を展開した自分自身を返す。
 * (quote A) -> `(quote A)
 * (if A B C) -> `(if ,(expand A) ,(expand B) ,(expand C))
 * (if A B) -> `(if ,(expand A) ,(expand B))
 * (define a A) -> `(define a ,(expand A))
 * (lambda (a b) C D) -> `(lambda (a b) ,(expand C) ,(expand D))
 * 
 * [Macroの場合]
 * (1) 単純にマクロ展開する。
 * (2) その結果をもう一度展開する。 
 * これはMacroインタフェースにデフォルト実装できる。
 * 
 * (car A) -> `(invoke Lang car A) -> `(invoke Lang car ,(expand A))
 * (cons A B) -> `(invoke Lang cons A B) -> `(invoke Lang cons ,(expand A) ,(expand B))
 * (let ((a A) (b B) ...) C D ...) -> ((lambda (a b) C D ...) A B)
 * -> `((lambda (a b) ,(expand C) ,(expand D)) ,(expand A) ,(expand B))
 * 
 * [Procedureの場合]
 * car部およびすべての引数をそれぞれマクロ展開すればよい。
 */
public class TestExpandAll {

    @Test
    public void test() {
        assertEquals(read("(if (invoke Lang lt 1 n) x y)"), expandAll(read("(if (< 1 n) x y)")));
        assertEquals(read("'(+ 1 2)"), expandAll(read("'(+ 1 2)")));
        assertEquals(read("(invoke Lang car '(a b))"), expandAll(read("(car '(a b))")));
        assertEquals(read("((lambda (x) x) 'a)"), expandAll(read("(let ((x 'a)) x)")));
        assertEquals(read("(invoke Lang plus (invoke Lang plus 1 2))"), expandAll(read("(invoke Lang plus (+ 1 2))")));
        assertEquals(read("(define s (invoke Lang plus 1 2))"), expandAll(read("(define s (+ 1 2))")));
        assertEquals(read("(define (list x) x)"), expandAll(read("(define (list x) x)")));
        assertEquals(read("((lambda () (invoke Lang minus 1 2) (invoke Lang plus 1 2)))"),
            expandAll(read("(begin (- 1 2) (+ 1 2))")));
        assertEquals(read("((if true + -) 1 2)"), expandAll(read("((if true + -) 1 2)")));
        assertEquals(read("(define (fact n)"
            + "  (if (invoke Lang le n 1)"
            + "      1"
            + "      (invoke Lang multiply n (fact (invoke Lang minus n 1)))))"),
            expandAll(read("(define (fact n)"
                + "  (if (<= n 1)"
                + "      1"
                + "      (* n (fact (- n 1)))))")));
    }
    
}
