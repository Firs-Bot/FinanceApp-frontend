package com.example.catatkeuangan.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

/**
 * Custom Pie Chart View — tanpa library eksternal.
 * Dipanggil dari StatisticsActivity via setData().
 */
class PieChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        textSize = 32f
        typeface = Typeface.DEFAULT_BOLD
    }

    private var values: FloatArray   = floatArrayOf()
    private var labels: Array<String> = emptyArray()
    private var colors: IntArray     = intArrayOf()

    private val oval = RectF()

    fun setData(values: FloatArray, labels: Array<String>, colors: IntArray) {
        this.values = values
        this.labels = labels
        this.colors = colors
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (values.isEmpty()) return

        val size   = min(width, height).toFloat()
        val cx     = width / 2f
        val cy     = height / 2f
        val radius = size / 2f - 16f
        val holeR  = radius * 0.45f   // donut hole

        oval.set(cx - radius, cy - radius, cx + radius, cy + radius)

        val total = values.sum()
        var startAngle = -90f

        values.forEachIndexed { i, v ->
            val sweep = (v / total) * 360f
            paint.color = colors.getOrElse(i) { Color.LTGRAY }
            paint.style = Paint.Style.FILL
            canvas.drawArc(oval, startAngle, sweep, true, paint)

            // Label persentase di tengah tiap slice (jika > 5%)
            if (v >= 5f) {
                val midAngle = Math.toRadians((startAngle + sweep / 2).toDouble())
                val labelR   = (radius + holeR) / 2f
                val lx = (cx + labelR * Math.cos(midAngle)).toFloat()
                val ly = (cy + labelR * Math.sin(midAngle)).toFloat() + textPaint.textSize / 3
                textPaint.textSize = when {
                    sweep > 40 -> 30f
                    sweep > 20 -> 24f
                    else       -> 18f
                }
                canvas.drawText("${v.toInt()}%", lx, ly, textPaint)
            }

            startAngle += sweep
        }

        // Donut hole
        paint.color = resolveBackgroundColor()
        paint.style = Paint.Style.FILL
        canvas.drawCircle(cx, cy, holeR, paint)
    }

    /** Cocokkan warna lubang dengan background parent/theme. */
    private fun resolveBackgroundColor(): Int {
        var v: View = this
        while (true) {
            val bg = v.background
            if (bg != null) {
                val bmp = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
                bg.setBounds(0, 0, 1, 1)
                bg.draw(Canvas(bmp))
                val c = bmp.getPixel(0, 0)
                bmp.recycle()
                if (Color.alpha(c) > 0) return c
            }
            val parent = v.parent as? View ?: break
            v = parent
        }
        return Color.parseColor("#1E1E2E") // fallback dark
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desired = 360
        val w = resolveSize(desired, widthMeasureSpec)
        val h = resolveSize(desired, heightMeasureSpec)
        setMeasuredDimension(w, h)
    }
}
