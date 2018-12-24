package io.github.karino2.equationpad.mathview

import android.graphics.Canvas
import android.graphics.RectF
import android.text.TextPaint
import kotlin.IllegalArgumentException

data class Box(var left:Float = 0f, var top:Float = 0f, var width:Float = 0f, var height:Float = 0f) {
    val right
        get() = left+width

    val bottom
    get() = top+height

    fun toRectF(scale : Float) = RectF(left*scale, top*scale, right*scale, bottom*scale)

    fun isInside(x: Float, y:Float) = (x >= left && x <= right && y >= top && y <= bottom)
}

inline fun StringBuilder.enclose(beg: String, end: String, body: (StringBuilder)->Unit) {
    this.append(beg)
    body(this)
    this.append(end)
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

    // do nothing for default behavior.
    open fun switch() {}

   abstract fun toLatex(builder: StringBuilder)


    fun toLatex(): String {
        val builder = StringBuilder()
        toLatex(builder)
        return builder.toString()
    }

    abstract fun draw(canvas: Canvas, scale: Float, paint: TextPaint)

    // common utilities

    fun toLatexTerm(expr: Expr, builder: java.lang.StringBuilder) {
        when(expr) {
            is Variable -> expr.toLatex(builder)
            else -> {
                builder.enclose("{ ", "}") {
                    expr.toLatex(it)

                }
            }
        }
    }

    protected fun textBase(scale: Float, paint: TextPaint): Float {
        val fmi = paint.fontMetrics
        return box.bottom * scale - fmi.bottom
    }

    protected fun TextPaint.applyTextSize(scale: Float) : TextPaint {
        return this.apply { textSize = 0.95F*box.height * scale }
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

    override fun draw(canvas: Canvas, scale: Float, paint: TextPaint) {
        child?.let { it.draw(canvas, scale, paint) }
    }
}

class Variable(val name: String) : Expr() {

    override fun toLatex(builder: StringBuilder) {
        builder.append(name)
    }

    override fun layout(left: Float, top: Float, currentSize: Float, measure: (String, Float)->Float) {
        box.left = left
        box.top = top
        box.width = measure(resolved, currentSize)
        box.height = currentSize
    }

    companion object {
        val entityMap = mapOf(
            "alpha" to "α",
            "beta" to "β",
            "gamma" to "γ",
            "delta" to "δ",
            "epsilon" to "ε",
            "eta" to "η",
            "theta" to "θ",
            "lambda" to "λ",
            "mu" to "μ",
            "xi" to "ξ",
            "pi" to "π",
            "ro" to "ρ",
            "sigma" to "σ",
            "tau" to "τ",
            "phi" to "φ",
            "chi" to "χ",
            "psi" to "ψ",
            "omega" to "ω",
            "Psi" to "Ψ")
    }


    val resolved: String
    get() {
        val tokens = name.split(" ")
        val builder = StringBuilder()
        tokens.forEach { token->
            if(token.startsWith('\\')) {
                val ref = token.substring(1)
                if(entityMap.containsKey(ref)) {
                    builder.append(entityMap[ref])
                }
                else {
                    builder.append(token)
                }
            } else {
                builder.append(token)
            }
        }
        return builder.toString()
    }

    override fun draw(canvas: Canvas, scale: Float, paint: TextPaint) {
        val _paint = paint.applyTextSize(scale)
        val y = textBase(scale, _paint)
        canvas.drawText(
            this.resolved,
            box.left* scale,
            y,
            _paint)
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

    override fun draw(canvas: Canvas, scale: Float, paint: TextPaint) {
        children.forEach{it.draw(canvas, scale, paint)}
    }

    fun addChild(child: Expr) {
        child.parent = this
        children.add(child)
    }
}

abstract class TwoExpr : ExprGroup() {
    override fun switch() {
        val (child0, child1) = Pair(children[0], children[1])
        children[0] = child1
        children[1] = child0
    }
}

class Subscript(body: Expr, sub:Expr) : TwoExpr(){
    val PADDING = 1f


    init {
        addChild(body)
        addChild(sub)
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



class Superscript(body: Expr, sup:Expr) : TwoExpr(){
    val PADDING = 1f
    init {
        addChild(body)
        addChild(sup)
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

class FuncExpr(fname:Expr, body : Expr) : TwoExpr() {
    override fun toLatex(builder: StringBuilder) {
        fname.toLatex(builder)
        builder.append("(")
        body.toLatex(builder)
        builder.append(")")
    }

    init {
        addChild(fname)
        addChild(body)
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

    override fun draw(canvas: Canvas, scale: Float, paint: TextPaint) {
        fname.draw(canvas, scale, paint)
        val _paint = paint.applyTextSize(scale)

        val y = textBase(scale, _paint)

        canvas.drawText(
            "(",
            fname.box.right*scale,
            y,
            _paint)
        body.draw(canvas, scale, _paint)
        // draw might change _paint. Just recover size for a while.
        _paint.applyTextSize(scale)

        canvas.drawText(
            ")",
            body.box.right*scale,
            y,
            _paint)
    }

}


/*
fun Expr.toLatexTerm(builder: StringBuilder, withBrace: Boolean) {
    if(withBrace) {
        builder.enclose("{", "}") {
            this.toLatex(it)
        }
    } else {
        this.toLatex(builder)
    }
}
*/

class Products(a: Expr, b:Expr) : ExprGroup() {

    override fun layout(left: Float, top: Float, currentSize: Float, measure: (String, Float) -> Float) {
        children[0].layout(left, top, currentSize, measure)
        children.windowed(2).forEach {tup -> tup[1].layout(tup[0].box.right, top, currentSize, measure)}

        box.left = left
        box.top = top
        box.width = children.last().box.right - left
        box.height = children.maxBy{it.box.height}!!.box.height
    }

    override fun toLatex(builder: StringBuilder) {
        builder.enclose("{ ", "}") {
            for(child in children) {
                toLatexTerm(child, it)
                it.append(" ")
            }
        }

    }

    init {
        addChild(a)
        addChild(b)
    }
}

// base class of sum and prod.

abstract class MathOpExpr(val name: Variable, body: Expr) : ExprGroup() {
    init {
        addChild(name)
        addChild(body)
    }



    val body: Expr
        get() = children[1]

    var subScript: Expr? = null
        set(value) {
            field?.let { parentNullIfSelf(it) }

            value?.parent = this
            field = value
        }

    var superScript: Expr? = null
        set(value) {
            field?.let { parentNullIfSelf(it) }

            value?.parent = this
            field = value
        }

    override fun findHit(x: Float, y: Float): Expr? {
        subScript?.let {
            it.findHit(x, y)?.let { hit ->
                return hit
            }
        }
        superScript?.let {
            it.findHit(x, y)?.let { hit ->
                return hit
            }
        }
        val cand = super.findHit(x, y)
        if(cand == name)
            return this
        return cand
    }

    override fun replace(org: Expr, newExp: Expr) {
        when (org) {
            subScript -> subScript = newExp
            superScript -> superScript = newExp
            else -> super.replace(org, newExp)
        }
    }


    override fun layout(left: Float, top: Float, currentSize: Float, measure: (String, Float) -> Float) {
        val (bodySize, headTailSize) = when {
            subScript != null && superScript != null -> Pair(currentSize/2.0F, currentSize/4.0F)
            subScript != null || superScript != null -> Pair(currentSize*2.0F/3.0F, currentSize/3.0F)
            else -> Pair(currentSize, 0F)
        }
        val bodyTop = headTailSize + top
        name.layout(left, bodyTop, bodySize, measure)
        body.layout(name.box.right, bodyTop, bodySize, measure)

        superScript?.let {
            // layout twice to know centering position.
            it.layout(left, top, headTailSize, measure)
            val width = it.box.width

            val supleft = calcCenteringLeft(left, width)
            it.layout(supleft, top, headTailSize, measure)
        }

        subScript?.let {
            it.layout(left, name.box.bottom, headTailSize, measure)
            val width = it.box.width

            val subleft = calcCenteringLeft(left, width)
            it.layout(subleft, name.box.bottom, headTailSize, measure)
        }
        box.left = left
        box.top = top
        box.width = name.box.width+body.box.width

        val bottomOffset = subScript?.box?.height ?: 0F
        box.height = body.box.bottom - top + bottomOffset
    }

    private fun calcCenteringLeft(left: Float, width: Float): Float {
        return Math.max(left, left + name.box.width / 2.0F - width / 2.0F)
    }

    override fun draw(canvas: Canvas, scale: Float, paint: TextPaint) {
        // TODO: name should be roman.
        name.draw(canvas, scale, paint)
        body.draw(canvas, scale, paint)
        subScript?.let {
            it.draw(canvas, scale, paint)
        }
        superScript?.let {
            it.draw(canvas, scale, paint)
        }


    }

    abstract fun toLatexBase(builder: StringBuilder) : Unit

    override fun toLatex(builder: StringBuilder) {
        toLatexBase(builder)
        superScript?.let {
            builder.append("^")
            toLatexTerm(it, builder)
        }
        subScript?.let {
            builder.append("_")
            toLatexTerm(it, builder)
        }
        builder.enclose("{ ", "}") {
            body.toLatex(it)
        }
    }
}


 class SumExpr(body: Expr) : MathOpExpr(Variable("Σ"), body){
     override fun toLatexBase(builder: StringBuilder) {
         builder.append("\\sum")
     }

 }

class ProdExpr(body: Expr) : MathOpExpr(Variable("Π"), body){
    override fun toLatexBase(builder: StringBuilder) {
        builder.append("\\prod")
    }
}



object ExprBuilder {
    fun v(name: String) = Variable(name)
    fun pro(a: Expr, b:Expr) = Products(a, b)
    fun sub(a: Expr, b:Expr) = Subscript(a, b)
    fun sup(a: Expr, b:Expr) = Superscript(a, b)
    fun sum(body: Expr, sup:Expr? = null, sub:Expr? = null) : SumExpr {
        val sumExp = SumExpr(body)
        assignSubSup(sumExp, sup, sub)
        return sumExp
    }
    fun prod(body: Expr, sup:Expr? = null, sub:Expr? = null) : ProdExpr {
        val prodExp = ProdExpr(body)
        assignSubSup(prodExp, sup, sub)
        return prodExp
    }

    private fun assignSubSup(mathExp: MathOpExpr, sup: Expr?, sub: Expr?) {
        sup?.let {
            mathExp.superScript = it
        }
        sub?.let {
            mathExp.subScript = it
        }
    }
}

fun build(body: ExprBuilder.()->Expr) = ExprBuilder.body()

