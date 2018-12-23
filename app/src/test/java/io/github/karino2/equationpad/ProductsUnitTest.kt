package io.github.karino2.equationpad

import io.github.karino2.equationpad.mathview.Expr
import io.github.karino2.equationpad.mathview.Products
import io.github.karino2.equationpad.mathview.Subscript
import io.github.karino2.equationpad.mathview.Variable
import org.junit.Test

import org.junit.Assert.*


object Builder {
    fun vari(name: String) = Variable(name)
    fun pro(a: Expr, b:Expr) = Products(a, b)
    fun sub(a: Expr, b:Expr) = Subscript(a, b)
}

fun build(body: Builder.()->Expr) = Builder.body()

class ProductsUnitTest {
    @Test
    fun toLatex_twoTerm() {
        val prod = build { pro(vari("a"), vari("b")) }
        assertEquals("{ a b }", prod.toLatex())
    }

    @Test
    fun toLatex_twoTerm_SubscriptFirst() {
        val prod = build { pro(sub(vari("a"), vari("n")), vari("c"))}
        assertEquals("{ { a_n} c }", prod.toLatex())
    }

}