package io.github.karino2.equationpad

import io.github.karino2.equationpad.mathview.Subscript
import io.github.karino2.equationpad.mathview.Variable
import org.junit.Assert
import org.junit.Test

class ToLatexUnitTest {
    @Test
    fun subscript_basic() {
        val sub = Subscript(
            Variable("x"),
            Variable("n")
        )
        Assert.assertEquals("x_n", sub.toLatex())
    }

    @Test
    fun subscript_nest() {
        val sub = Subscript(
            Subscript(
                Variable("x"),
                Variable("n")
            ), Variable("y")
        )
        Assert.assertEquals("{ x_n}_y", sub.toLatex())
    }
}