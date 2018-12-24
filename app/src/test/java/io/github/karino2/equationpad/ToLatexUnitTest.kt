package io.github.karino2.equationpad

import io.github.karino2.equationpad.mathview.Subscript
import io.github.karino2.equationpad.mathview.Variable
import io.github.karino2.equationpad.mathview.build
import junit.framework.Assert.assertEquals
import org.junit.Assert
import org.junit.Test

class ToLatexUnitTest {
    @Test
    fun subscript_basic() {
        val sub = build {
            sub(v("x"), v("n"))
        }
        Assert.assertEquals("x_n", sub.toLatex())
    }

    @Test
    fun subscript_nest() {
        val sub = build {
            sub(
                sub(
                    v("x"),
                    v("n")
                ), v("y")
            )
        }
        Assert.assertEquals("{ x_n}_y", sub.toLatex())
    }

    @Test
    fun vertbar() {
        val expect = "x \\mid y"

        val vbar = build { vbar(v("x"), v("y")) }
        val actual = vbar.toLatex()

        assertEquals(expect, actual)


    }
}