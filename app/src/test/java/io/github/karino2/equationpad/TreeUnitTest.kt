package io.github.karino2.equationpad

import io.github.karino2.equationpad.mathview.Subscript
import io.github.karino2.equationpad.mathview.Variable
import org.junit.Test

import org.junit.Assert.*

class SubscriptUnitTest {
    @Test
    fun constructor_parentCorrect() {

        val body = Variable("body")
        val child = Variable("child")

        val sub = Subscript(body, child)

        assertNull(sub.parent)
        assertEquals(child, sub.sub)
        assertEquals(body, sub.body)
        assertEquals(sub, child.parent)
        assertEquals(sub, body.parent)
    }

    @Test
    fun replace_childCorrect() {

        val body = Variable("body")
        val child = Variable("child")
        val child2 = Variable("child2")

        val sub = Subscript(body, child)
        sub.replace(child, child2)

        assertEquals(body, sub.body)
        assertEquals(child2, sub.sub)
    }
    @Test
    fun replace_bodyCorrect() {

        val body = Variable("body")
        val child = Variable("child")
        val body2 = Variable("body2")

        val sub = Subscript(body, child)
        sub.replace(body, body2)

        assertEquals(body2, sub.body)
        assertEquals(child, sub.sub)
    }

    @Test
    fun layout_basicCorrect() {

        val body = Variable("body")
        val child = Variable("child")

        val sub = Subscript(body, child)
        sub.layout(0f, 0f, 1000f, {text, size -> text.length*size})

        assertEquals(1000f, sub.box.height)
        assertEquals(4000f + 5 * (1000f / 2), sub.box.width)
    }
}


class ToLatexUnitTest {
    @Test
    fun subscript_basic() {
        val sub = Subscript(Variable("x"), Variable("n"))
        assertEquals("x_n", sub.toLatex())
    }

    @Test
    fun subscript_nest() {
        val sub = Subscript(Subscript(Variable("x"), Variable("n")), Variable("y"))
        assertEquals("{ x_n}_y", sub.toLatex())
    }
}

