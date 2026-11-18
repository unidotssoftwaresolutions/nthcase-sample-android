package com.nthcase.sample.lobby

import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.drawable.Drawable
import androidx.core.graphics.PathParser

/**
 * One of the shared drawings (art/, through ArtPaths) for the Views build: the stroked SVG path on an optional
 * rounded tile, scaled from its [viewport] units to the drawable's bounds. Compose draws the same strings as
 * ImageVectors, so both Lobbies show the same art.
 */
class GlyphDrawable(
    pathData: String,
    private val viewport: Float,
    private val strokeUnits: Float,
    private var ink: Int,
    private val tile: Int? = null,
    private val corner: Float = 0f,
    /** The share of the bounds the drawing takes, centred: 0.62 for a glyph on its tile, as the Compose GlyphTile. */
    private val scale: Float = 1f,
) : Drawable() {
    private val source: Path = PathParser.createPathFromPathData(pathData)
    private val drawn = Path()
    private val matrix = Matrix()
    private val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val rect = RectF()

    override fun draw(canvas: Canvas) {
        val b = bounds
        if (b.isEmpty) return
        tile?.let {
            fill.color = it
            rect.set(b)
            canvas.drawRoundRect(rect, corner, corner, fill)
        }
        val side = minOf(b.width(), b.height()) * scale
        val unit = side / viewport
        matrix.setScale(unit, unit)
        matrix.postTranslate(b.exactCenterX() - side / 2f, b.exactCenterY() - side / 2f)
        source.transform(matrix, drawn)
        stroke.color = ink
        stroke.strokeWidth = strokeUnits * unit
        canvas.drawPath(drawn, stroke)
    }

    private var tints: ColorStateList? = null

    override fun setTintList(tint: ColorStateList?) {
        tints = tint
        tint?.let { ink = it.getColorForState(state, it.defaultColor) }
        invalidateSelf()
    }

    override fun isStateful(): Boolean = tints?.isStateful == true

    override fun onStateChange(state: IntArray): Boolean {
        val t = tints ?: return false
        val next = t.getColorForState(state, t.defaultColor)
        if (next == ink) return false
        ink = next
        return true
    }

    override fun setAlpha(alpha: Int) {
        stroke.alpha = alpha
        fill.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        stroke.colorFilter = colorFilter
    }

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    override fun getIntrinsicWidth(): Int = -1
    override fun getIntrinsicHeight(): Int = -1
}
