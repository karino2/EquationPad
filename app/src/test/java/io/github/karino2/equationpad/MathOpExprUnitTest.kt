package io.github.karino2.equationpad

import io.github.karino2.equationpad.mathview.*
import org.junit.Test

import org.junit.Assert.*

class MathOpExprUnitTest {
    @Test
    fun sum_toLatex_noSubSup() {
        val expect = "\\sum{ a}"

        val exp = build {
            sum(v("a"))
        }

        val actual = exp.toLatex()
        assertEquals(expect, actual)
    }

    @Test
    fun sum_toLatex_sub() {
        val expect = "\\sum_n{ a}"

        val exp = build {
            sum(v("a"), null, v("n"))
        }

        val actual = exp.toLatex()
        assertEquals(expect, actual)
    }

    @Test
    fun sum_toLatex_subComplex() {
        val expect = "\\sum_{ n_x}{ a}"

        val exp = build {
            sum(v("a"), null, sub(v("n"), v("x")))
        }

        val actual = exp.toLatex()
        assertEquals(expect, actual)
    }

    @Test
    fun sum_toLatex_super() {
        val expect = "\\sum^n{ a}"

        val exp = build {
            sum(v("a"), v("n"))
        }

        val actual = exp.toLatex()
        assertEquals(expect, actual)
    }

    @Test
    fun sum_toLatex_subSuper() {
        val expect = "\\sum^a_b{ c}"

        val exp = build {
            sum(v("c"), v("a"), v("b"))
        }

        val actual = exp.toLatex()
        assertEquals(expect, actual)
    }

    @Test
    fun sum_toLatex_subSuperComplex() {
        val expect = "\\sum^{ { a_n}^2}_{ { b_n}^2}{ { c_n}^2}"

        val exp = build {
            sum(sup(sub(v("c"), v("n")), v("2")),
                sup(sub(v("a"), v("n")), v("2")),
                sup(sub(v("b"), v("n")), v("2")))
        }

        val actual = exp.toLatex()
        assertEquals(expect, actual)
    }

    @Test
    fun prod_toLatex_super() {
        val expect = "\\prod^n{ a}"

        val exp = build {
            prod(v("a"), v("n"))
        }

        val actual = exp.toLatex()
        assertEquals(expect, actual)
    }

}