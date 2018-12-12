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
        assertEquals(4000f + 5 * (1000f / 3), sub.box.width)
    }
}


class VariableUnitTest {
    @Test
    fun basic_nameCorrect() {
        val vari = Variable("x")
        assertEquals("x", vari.name)
    }


    /*
    @Test
    fun addChild_parentCorrect() {

        val pare = Variable("parent")
        val child = Variable("child")

        pare.addChild(child)

        assertNull(pare.parent)
        assertEquals(child, pare.firstChild)
        assertEquals(pare, child.parent)
    }
    */



}
