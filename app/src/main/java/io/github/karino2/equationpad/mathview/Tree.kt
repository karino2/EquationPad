package io.github.karino2.equationpad.mathview

import android.graphics.Paint
import android.graphics.RectF
import kotlin.IllegalArgumentException

data class Box(var left:Float = 0f, var top:Float = 0f, var width:Float = 0f, var height:Float = 0f) {
    val right
        get() = left+width

    val bottom
    get() = top+height

    fun toRectF(scale : Float) = RectF(left*scale, top*scale, right*scale, bottom*scale)

    fun isInside(x: Float, y:Float) = (x >= left && x <= right && y >= top && y <= bottom)
}




sealed class Expr {
    companion object {
        var global_id = 1
        val id_to_expr_dict = HashMap<Int, Expr>()
    }
    val id = global_id++

    init {
        id_to_expr_dict[id] = this
    }

    var box = Box()

    fun parentNullIfSelf(elem: Expr) {
        if(elem.parent == this)
            elem.parent = null
    }


    // tree related property
    var parent: Expr? = null


    open fun replace(org: Expr, newExp: Expr) {
        throw Exception("Must not happen. Call replace to no child element.")
    }

    abstract fun layout(left: Float, top: Float, currentSize: Float, measure: (String, Float)->Float)

    open fun findHit(x: Float, y:Float) : Expr? {
        if(box.isInside(x, y))
            return this

        return null
    }

    abstract fun toLatex(builder: StringBuilder)

    fun toLatexTerm(expr: Expr, builder: java.lang.StringBuilder) {
        when(expr) {
            is Variable -> expr.toLatex(builder)
            else -> {
                builder.append("{")
                expr.toLatex(builder)
                builder.append("}")
            }
        }
    }

    fun toLatex(): String {
        val builder = StringBuilder()
        toLatex(builder)
        return builder.toString()
    }

}

class Root(var child : Expr? = null) : Expr() {
    override fun toLatex(builder: StringBuilder) {
        child?.let { it.toLatex(builder) }
    }

    init {
        child?.parent = this
    }

    override fun layout(left: Float, top: Float, currentSize: Float, measure: (String, Float) -> Float) {
        child?.let {
            it.layout(left, top, currentSize, measure)
            box = it.box
        }

    }
    override fun findHit(x: Float, y:Float) : Expr? {
        child?.let{ return it.findHit(x, y) }

        return null
    }

    override fun replace(org: Expr, newExp: Expr) {
        if(org == child)
        {
            parentNullIfSelf(org)
            newExp.parent = this

            child = newExp
            return
        }
        throw IllegalArgumentException("No org expression in root.")
    }
}

class Variable(val name: String) : Expr() {
    override fun toLatex(builder: StringBuilder) {
        builder.append(name)
    }

    override fun layout(left: Float, top: Float, currentSize: Float, measure: (String, Float)->Float) {
        box.left = left
        box.top = top
        box.width = measure(name, currentSize)
        box.height = currentSize
    }
}

class Subscript(var body: Expr, var sub:Expr) : Expr(){
    val PADDING = 1f

    override fun toLatex(builder: StringBuilder) {
        toLatexTerm(body, builder)
        builder.append("_")
        toLatexTerm(sub, builder)
    }

    override fun layout(left: Float, top: Float, currentSize: Float, measure: (String, Float)->Float) {
        body.layout(left, top, currentSize, measure)
        with(body.box) {
            sub.layout(right+PADDING, top+(height*1f)/2f, currentSize/2f, measure)
        }
        box.left = left
        box.top = top
        box.width = body.box.width + sub.box.width
        // may be need to add a little.
        box.height = body.box.height
   }

    init {
        body.parent = this
        sub.parent = this
    }

    override fun replace(org: Expr, newExp: Expr) {
        if(org == body)
        {
            parentNullIfSelf(org)
            newExp.parent = this

            body = newExp
            return
        }
        if(org == sub)
        {
            parentNullIfSelf(org)
            newExp.parent = this

            sub = newExp
            return
        }
        throw IllegalArgumentException("No org expression in this term.")
    }

    override fun findHit(x: Float, y: Float): Expr? {
        if(!box.isInside(x, y))
            return null

        body.findHit(x, y)?.let {
            return it
        }

        sub.findHit(x, y)?.let {
            return it
        }
        return this
    }

}



class Superscript(var body: Expr, var sup:Expr) : Expr(){
    val PADDING = 1f

    override fun toLatex(builder: StringBuilder) {
        toLatexTerm(body, builder)
        builder.append("^")
        toLatexTerm(sup, builder)
    }

    override fun layout(left: Float, top: Float, currentSize: Float, measure: (String, Float)->Float) {
        // determine sizze.
        sup.layout(0f, 0f, currentSize/2f, measure)


        body.layout(left, top+sup.box.height/5f, currentSize, measure)
        sup.box.left = body.box.right+PADDING

        box.left = left
        box.top = top
        box.width = body.box.width + sup.box.width
        // may be need to add a little.
        box.height = body.box.bottom - sup.box.top
    }

    init {
        body.parent = this
        sup.parent = this
    }

    override fun replace(org: Expr, newExp: Expr) {
        if(org == body)
        {
            parentNullIfSelf(org)
            newExp.parent = this

            body = newExp
            return
        }
        if(org == sup)
        {
            parentNullIfSelf(org)
            newExp.parent = this

            sup = newExp
            return
        }
        throw IllegalArgumentException("No org expression in this term.")
    }

    override fun findHit(x: Float, y: Float): Expr? {
        if(!box.isInside(x, y))
            return null

        body.findHit(x, y)?.let {
            return it
        }

        sup.findHit(x, y)?.let {
            return it
        }
        return this
    }

}