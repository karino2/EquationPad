package io.github.karino2.equationpad

import io.github.karino2.equationpad.mathview.Variable
import org.junit.Assert
import org.junit.Test

class VariableUnitTest {
    @Test
    fun basic_nameCorrect() {
        val vari = Variable("x")
        Assert.assertEquals("x", vari.name)
    }


    @Test
    fun resolve_normal() {
        val vari = Variable("x")
        Assert.assertEquals("x", vari.resolved)
    }

    // space should be supported officially in multiply (so that subScript, etc should work).
    // but add test for behavior document purpose.
    @Test
    fun resolve_spaceRemoved() {
        val vari = Variable("a b")
        Assert.assertEquals("ab", vari.resolved)
    }


    @Test
    fun resolve_lambda() {
        val vari = Variable("\\lambda")
        Assert.assertEquals("λ", vari.resolved)

    }

    @Test
    fun toLatex_basic() {
        val vari = Variable("x")
        Assert.assertEquals("x", vari.toLatex())
    }

    @Test
    fun toLatex_entity_shouldKeepOriginal() {
        val vari = Variable("\\lambda")
        Assert.assertEquals("\\lambda", vari.toLatex())
    }

}