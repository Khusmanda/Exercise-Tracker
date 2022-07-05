package khusmanda.assignment3.mytracker

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat

class MyGraph : View {
    private var black_paint: Paint? = null
    private var white_paint: Paint? = null
    private var red_paint: Paint? = null
    var PointArray: DoubleArray? = null
    var My_Y = 0.0

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    fun init() {
        black_paint = Paint(Paint.ANTI_ALIAS_FLAG)
        white_paint = Paint(Paint.ANTI_ALIAS_FLAG)
        red_paint = Paint(Paint.ANTI_ALIAS_FLAG)
        black_paint!!.color = ContextCompat.getColor(context, R.color.black)
        white_paint!!.color = ContextCompat.getColor(context, R.color.white)
        red_paint!!.color = ContextCompat.getColor(context, R.color.red)
        white_paint!!.strokeWidth = 7f
    }

    fun graphDetail(graph_x: DoubleArray?, graph_y: Double) {
        PointArray = graph_x
        My_Y = graph_y
    }

    fun graphDetail(graph_x: DoubleArray) {
        var maxY = 0.0
        for (i in graph_x.indices) {
            if (graph_x[i] > maxY) {
                maxY = graph_x[i]
            }
        }
        graphDetail(graph_x, maxY)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), black_paint!!)
        if (PointArray == null) {
            return
        }
        val my_x = PointArray!!.size
        val factorX = width / my_x.toDouble() - 5
        val factorY = height / My_Y - 7
        for (i in 1 until PointArray!!.size) {
            val j = i - 1
            val x0 = i - 1
            val y0 = PointArray!![i - 1]
            val y1 = PointArray!![i]
            val sx = (x0 * factorX).toInt()
            val sy = height - (y0 * factorY).toInt()
            val ex = (i * factorX).toInt()
            val ey = height - (y1 * factorY).toInt()
            canvas.drawLine(sx.toFloat(), sy.toFloat(), ex.toFloat(), ey.toFloat(), white_paint!!)
            white_paint!!.textSize = 35f
            canvas.drawText(
                "" + String.format("%.03f", PointArray!![j] * 10),
                sx.toFloat(),
                sy.toFloat(),
                white_paint!!
            )
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        var width = measuredWidth
        var height = measuredHeight
        val widthWithoutPadding = width - paddingLeft - paddingRight
        val heigthWithoutPadding = height - paddingTop - paddingBottom
        val maxWidth = (heigthWithoutPadding * RATIO).toInt()
        val maxHeight = (widthWithoutPadding / RATIO).toInt()
        if (widthWithoutPadding > maxWidth) {
            width = maxWidth + paddingLeft + paddingRight
        } else {
            height = maxHeight + paddingTop + paddingBottom
        }
        if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY) {
            width = measuredWidth
        }
        if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY) {
            height = measuredHeight
        }
        setMeasuredDimension(width, height)
    }

    companion object {
        private const val RATIO = 4f / 3f
    }
}