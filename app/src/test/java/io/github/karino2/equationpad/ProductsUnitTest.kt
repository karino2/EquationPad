package io.github.karino2.equationpad

import io.github.karino2.equationpad.mathview.*
import org.junit.Test

import org.junit.Assert.*


class ProductsUnitTest {
    @Test
    fun toLatex_twoTerm() {
        val prod = build { pro(v("a"), v("b")) }
        assertEquals("{ a b }", prod.toLatex())
    }

    @Test
    fun toLatex_twoTerm_SubscriptFirst() {
        val prod = build { pro(sub(v("a"), v("n")), v("c"))}
        assertEquals("{ { a_n} c }", prod.toLatex())
    }

    @Test
    fun toLatexTerm_avoidUnnecessaryParen() {
        val eqProducts = build {eq(v("a"), pro(v("b"), v("c")))}
        assertEquals("a = { b c }", eqProducts.toLatex())
    }

}