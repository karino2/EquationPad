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

abstract class ExprGroup : Expr() {
    val children : MutableList<Expr> = mutableListOf()


    override fun findHit(x: Float, y: Float): Expr? {
        if(!box.isInside(x, y))
            return null

        for(child in children) {
            child.findHit(x, y)?.let {
                return it
            }
        }
        return this
    }

    override fun replace(org: Expr, newExp: Expr) {
        for((idx, child) in children.withIndex()) {
            if(org == child)
            {
                parentNullIfSelf(org)
                newExp.parent = this

                children[idx] = newExp
                return
            }

        }
        throw IllegalArgumentException("No org expression in this term.")
    }
}

class Subscript(body: Expr, sub:Expr) : ExprGroup(){
    val PADDING = 1f


    init {
        body.parent = this
        sub.parent = this
        children.add(body)
        children.add(sub)
    }

    var body : Expr
        get() = children[0]
        set(value) {
            children[0] = value
        }

    var sub :Expr
        get() = children[1]
        set(value) {
            children[1] = value
        }

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
}



class Superscript(body: Expr, sup:Expr) : ExprGroup(){
    val PADDING = 1f
    init {
        body.parent = this
        sup.parent = this
        children.add(body)
        children.add(sup)
    }
    var body : Expr
        get() = children[0]
        set(value) {
            children[0] = value
        }

    var sup :Expr
        get() = children[1]
        set(value) {
            children[1] = value
        }


    override fun toLatex(builder: StringBuilder) {
        toLatexTerm(body, builder)
        builder.append("^")
        toLatexTerm(sup, builder)
    }

    override fun layout(left: Float, top: Float, currentSize: Float, measure: (String, Float)->Float) {
        // determine sizze.
        sup.layout(0f, top, currentSize/2f, measure)


        body.layout(left, top+sup.box.height/5f, currentSize, measure)
        sup.box.left = body.box.right+PADDING

        box.left = left
        box.top = top
        box.width = body.box.width + sup.box.width
        // may be need to add a little.
        box.height = body.box.bottom - sup.box.top
    }
}

class FuncExpr(fname:Expr, body : Expr) : ExprGroup() {
    override fun toLatex(builder: StringBuilder) {
        fname.toLatex(builder)
        builder.append("(")
        body.toLatex(builder)
        builder.append(")")
    }

    init {
        fname.parent = this
        body.parent = this
        children.add(fname)
        children.add(body)
    }

    var fname : Expr
        get() = children[0]
        set(value) {
            children[0] = value
        }


    var body : Expr
        get() = children[1]
        set(value) {
            children[1] = value
        }

    override fun layout(left: Float, top: Float, currentSize: Float, measure: (String, Float) -> Float) {
        val leftPar = measure("(", currentSize)
        val rightPar = measure(")", currentSize)

        fname.layout(left, top, currentSize, measure)
        body.layout(fname.box.right+leftPar, top, currentSize, measure)

        box.left = left
        box.top = top
        box.width = fname.box.width+leftPar+body.box.width+rightPar
        box.height = Math.max(fname.box.height, body.box.height)
    }

}