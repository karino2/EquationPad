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


    /*

    val children by lazy{
        ArrayList<Expr>()
    }


    // next sibling
    val next : Expr?
    get() {
        val pare = parent ?: return null
        if(this == pare.children.last())
            return null

        // parent must contain this, and
        val idx = pare.children.indexOf(this)
        return pare.children[idx+1]
    }

    val firstChild: Expr?
    get() {
        if(children.size == 0)
            return null
        return children[0]
    }

    fun addChild(child : Expr) {
        child.parent = this
        children.add(child)
    }

    fun removeChild(child: Expr) {
        children.remove(child)
        child.parent = null
    }
    */

}

data class Variable(val name: String) : Expr() {
    override fun layout(left: Float, top: Float, currentSize: Float, measure: (String, Float)->Float) {
        box.left = left
        box.top = top
        box.width = measure(name, currentSize)
        box.height = currentSize
    }
}

data class Subscript(var body: Expr, var sub:Expr) : Expr(){
    val PADDING = 1f

    override fun layout(left: Float, top: Float, currentSize: Float, measure: (String, Float)->Float) {
        body.layout(left, top, currentSize, measure)
        with(body.box) {
            sub.layout(right+PADDING, top+(height*2f)/3f, currentSize/3, measure)
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
            body = newExp
            return
        }
        if(org == sub)
        {
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