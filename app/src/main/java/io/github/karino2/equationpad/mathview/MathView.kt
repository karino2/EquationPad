package io.github.karino2.equationpad.mathview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.util.AttributeSet
import android.view.View
import kotlin.math.roundToInt

class MathView(context :Context, attrSet: AttributeSet) : View(context, attrSet) {

    val expr = Subscript(Variable("a"), Variable("x"))
    var selectedExpr : Expr? = expr.body

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

    val paint = Paint(ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
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

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val initialSize = 1000f
        expr.layout(0f, 0f, INITIAL_SIZE) { text, size-> (paint.apply { textSize =   (size*DEFAULT_SCALE).toFloat() }.measureText(text)/DEFAULT_SCALE).toFloat()}

        val intrinsicWidthF = (expr.box.width*DEFAULT_SCALE)
        val intrinsicHeightF = (expr.box.height*DEFAULT_SCALE)

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
        if(!done && heightResizable) {
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

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // expr.box.width
        val scale = Math.min(measuredWidth/expr.box.width, measuredHeight/expr.box.height)

        paint.apply { textSize = expr.box.height * scale }
        var fmi = paint.fontMetrics

        drawExpr(canvas, scale, expr, fmi.bottom)

        selectedExpr?.let {
            canvas.drawRect(it.box.toRectF(scale).apply {left+=PADDING}, selectionPaint)
        }
    }



    fun drawExpr(canvas: Canvas, scale : Float, expr: Expr, bottomMargin: Float) {
        when(expr) {
            is Variable -> {
                drawVariable(canvas, scale, expr, bottomMargin)
            }
            is Subscript -> {
                drawExpr(canvas, scale, expr.body, bottomMargin)
                drawExpr(canvas, scale, expr.sub, bottomMargin)
            }
        }
    }

    val PADDING = 5

    private fun drawVariable(
        canvas: Canvas,
        scale: Float,
        expr: Variable,
        bottomMargin: Float
    ) {

        val _paint = paint.apply { textSize = expr.box.height * scale }
        val y = expr.box.bottom*scale-bottomMargin-PADDING

        canvas.drawText(
            expr.name,
            expr.box.left*scale+PADDING,
             y,
            _paint)
    }

}