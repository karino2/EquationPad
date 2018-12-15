package io.github.karino2.equationpad.mathview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_DOWN
import android.view.View
import kotlin.math.roundToInt

class MathView(context :Context, attrSet: AttributeSet) : View(context, attrSet) {

    val expr = Root(Variable("x"))
    var selectedExpr : Expr? = null
        set(value) {
            field = value
            requestLayout()
        }

    fun ifSelected(proc: (Expr)->Unit) = selectedExpr?.let{ proc(it) }


    fun _resolveSize(desiredSize: Int, measureSpec: Int) : Int {
        val specMode = MeasureSpec.getMode(measureSpec)
        val specSize = MeasureSpec.getSize(measureSpec)
        return when(specMode) {
            MeasureSpec.UNSPECIFIED -> {
                desiredSize
            }
            MeasureSpec.AT_MOST -> {
                Math.min(specSize, desiredSize)
            }
            // MeasureSpec.EXACTLY
            else -> {
                specSize
            }
        }

    }

    val textPaint = Paint(ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        typeface = Typeface.create("serif-monospace", Typeface.NORMAL)
    }

    val selectionPaint = Paint(ANTI_ALIAS_FLAG).apply {
        color = Color.argb(0xff, 0x91, 0x50, 0x0c)
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }


    // 1000f for Top expression.
    // I first hard code default height of top variable as 100dp.
    val INITIAL_SIZE = 1000f
    val DEFAULT_SCALE = 400.0/INITIAL_SIZE
    val MARGIN = 5f

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        expr.layout(0f, 0f, INITIAL_SIZE) { text, size-> (textPaint.apply { textSize =   (size*DEFAULT_SCALE).toFloat() }.measureText(text)/DEFAULT_SCALE).toFloat()}

        val intrinsicWidthF = (expr.box.width*DEFAULT_SCALE)+MARGIN
        val intrinsicHeightF = (expr.box.height*DEFAULT_SCALE)+MARGIN

        val intrinsicWidth = intrinsicWidthF.roundToInt()
        val intrinsicHeight = intrinsicHeightF.roundToInt()


        val widthSpecMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightSpecMode = MeasureSpec.getMode(heightMeasureSpec)

        var widthSize = _resolveSize(intrinsicWidth, widthMeasureSpec)
        var heightSize = _resolveSize(intrinsicHeight, heightMeasureSpec)


        val widthResizable = widthSpecMode != MeasureSpec.EXACTLY
        val heightResizable = heightSpecMode != MeasureSpec.EXACTLY

        var done = false

        if(widthResizable) {
            // adjust width to meet height.
            val widthCand = (heightSize*intrinsicWidthF/intrinsicHeightF).roundToInt()

            if(!heightResizable) {
                widthSize = _resolveSize(widthCand, widthMeasureSpec)
            }
            if(widthCand <= widthSize) {
                widthSize = widthCand
                done = true
            }
        }

        if(!done && heightResizable
            // if height is larger than intrinsicHeight, just keep as is.
            // wide mathview is OK for most of the case.
            && heightSize < intrinsicHeight) {
            val heightCand = (widthSize*intrinsicHeightF/intrinsicWidthF).roundToInt()
            if(!widthResizable) {
                heightSize = _resolveSize(heightCand, heightMeasureSpec)
            }
            if(heightCand <= heightSize) {
                heightSize = heightCand
            }
        }
        setMeasuredDimension(widthSize, heightSize)
    }

    val boxToViewScale
        get() = Math.min(measuredWidth/expr.box.width, measuredHeight/expr.box.height)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // for debug
        canvas.drawRect(0f, 0f, measuredWidth.toFloat(), measuredHeight.toFloat(), Paint().apply{color=Color.LTGRAY})

        // expr.box.width

        textPaint.apply { textSize = expr.box.height * boxToViewScale }
        drawExpr(canvas, boxToViewScale, expr)

        selectedExpr?.let {
            canvas.drawRect(it.box.toRectF(boxToViewScale).apply{ offset(5f, 5f); bottom -= 10f; right -=10f }, selectionPaint)
        }
    }



    fun drawExpr(canvas: Canvas, scale: Float, expr: Expr) {
        when(expr) {
            is Variable -> {
                drawVariable(canvas, scale, expr)
            }
            is ExprGroup -> {
                expr.children.forEach{drawExpr(canvas, scale, it)}
            }
            is Root -> {
                expr.child?.let { drawExpr(canvas, scale, it) }
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when(event.action) {
            ACTION_DOWN-> {
                val x = event.x/boxToViewScale
                val y = (event.y)/boxToViewScale
                selectedExpr = expr.findHit(x, y)
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun drawVariable(
        canvas: Canvas,
        scale: Float,
        expr: Variable
    ) {

        val _paint = textPaint.apply { textSize = expr.box.height * scale }

        var fmi = _paint.fontMetrics
        val y = expr.box.bottom*scale-fmi.bottom

        canvas.drawText(
            expr.name,
            expr.box.left*scale,
             y,
            _paint)
    }

}