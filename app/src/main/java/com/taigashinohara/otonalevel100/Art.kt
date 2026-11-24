package com.taigashinohara.otonalevel100

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.view.View
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

/**
 * Drawn artwork for v0.5.0 (no image assets): the mascot, confetti, stars and
 * the category / tab icons. Everything is vector shapes painted on Canvas, in
 * the Duolingo-style palette of ART_DIRECTION v2.0.
 */

// Midnight + Gold brand (v0.10.0): GREEN/GREEN_DARK now hold the midnight
// pair (mascot body/wings), ORANGE holds the gold (beak/feet, confetti).
private val GREEN = Color.rgb(0x22, 0x35, 0x5E)
private val GREEN_DARK = Color.rgb(0x18, 0x27, 0x47)
private val ORANGE = Color.rgb(0xC9, 0xA8, 0x6A)
private val YELLOW = Color.rgb(0xFF, 0xC8, 0x00)
private val BLUE = Color.rgb(0x1C, 0xB0, 0xF6)
private val PINK = Color.rgb(0xFF, 0x86, 0xD0)
private val PURPLE = Color.rgb(0xA5, 0x60, 0xF5)
private val INK = Color.rgb(0x3C, 0x3C, 0x3C)

/** The mascot: a round green owl, drawn from circles and simple paths. */
class MascotView(context: Context) : View(context) {
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        val cx = w / 2f
        val bodyR = min(w, h) * 0.40f
        val bodyCy = h * 0.54f

        // Ear tufts
        p.color = GREEN_DARK
        val tuft = Path()
        tuft.moveTo(cx - bodyR * 0.78f, bodyCy - bodyR * 0.62f)
        tuft.lineTo(cx - bodyR * 0.42f, bodyCy - bodyR * 0.78f)
        tuft.lineTo(cx - bodyR * 0.55f, bodyCy - bodyR * 0.30f)
        tuft.close()
        canvas.drawPath(tuft, p)
        val tuft2 = Path()
        tuft2.moveTo(cx + bodyR * 0.78f, bodyCy - bodyR * 0.62f)
        tuft2.lineTo(cx + bodyR * 0.42f, bodyCy - bodyR * 0.78f)
        tuft2.lineTo(cx + bodyR * 0.55f, bodyCy - bodyR * 0.30f)
        tuft2.close()
        canvas.drawPath(tuft2, p)

        // Wings
        p.color = GREEN_DARK
        canvas.drawOval(RectF(cx - bodyR * 1.08f, bodyCy - bodyR * 0.05f, cx - bodyR * 0.62f, bodyCy + bodyR * 0.72f), p)
        canvas.drawOval(RectF(cx + bodyR * 0.62f, bodyCy - bodyR * 0.05f, cx + bodyR * 1.08f, bodyCy + bodyR * 0.72f), p)

        // Body
        p.color = GREEN
        canvas.drawCircle(cx, bodyCy, bodyR, p)
        // Belly
        p.color = Color.rgb(0xF5, 0xED, 0xD8)
        canvas.drawOval(RectF(cx - bodyR * 0.52f, bodyCy + bodyR * 0.18f, cx + bodyR * 0.52f, bodyCy + bodyR * 0.98f), p)

        // Eyes
        val eyeY = bodyCy - bodyR * 0.22f
        val eyeDx = bodyR * 0.38f
        val eyeR = bodyR * 0.30f
        p.color = Color.WHITE
        canvas.drawCircle(cx - eyeDx, eyeY, eyeR, p)
        canvas.drawCircle(cx + eyeDx, eyeY, eyeR, p)
        p.color = INK
        canvas.drawCircle(cx - eyeDx, eyeY + eyeR * 0.08f, eyeR * 0.52f, p)
        canvas.drawCircle(cx + eyeDx, eyeY + eyeR * 0.08f, eyeR * 0.52f, p)
        p.color = Color.WHITE
        canvas.drawCircle(cx - eyeDx - eyeR * 0.16f, eyeY - eyeR * 0.10f, eyeR * 0.16f, p)
        canvas.drawCircle(cx + eyeDx - eyeR * 0.16f, eyeY - eyeR * 0.10f, eyeR * 0.16f, p)

        // Beak
        p.color = ORANGE
        val beak = Path()
        beak.moveTo(cx - bodyR * 0.16f, bodyCy + bodyR * 0.10f)
        beak.lineTo(cx + bodyR * 0.16f, bodyCy + bodyR * 0.10f)
        beak.lineTo(cx, bodyCy + bodyR * 0.34f)
        beak.close()
        canvas.drawPath(beak, p)

        // Feet
        p.color = ORANGE
        canvas.drawOval(RectF(cx - bodyR * 0.48f, bodyCy + bodyR * 0.94f, cx - bodyR * 0.12f, bodyCy + bodyR * 1.10f), p)
        canvas.drawOval(RectF(cx + bodyR * 0.12f, bodyCy + bodyR * 0.94f, cx + bodyR * 0.48f, bodyCy + bodyR * 1.10f), p)
    }
}

/** A single star, filled or empty (track colour). */
class StarView(context: Context, private val filledColor: Int, private val filled: Boolean) : View(context) {
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cx = width / 2f
        val cy = height / 2f
        val r = min(width, height) / 2f * 0.92f
        val path = Path()
        for (i in 0 until 10) {
            val rad = if (i % 2 == 0) r else r * 0.45f
            val ang = Math.toRadians(-90.0 + i * 36.0)
            val x = cx + (rad * cos(ang)).toFloat()
            val y = cy + (rad * sin(ang)).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        p.color = filledColor
        canvas.drawPath(path, p)
    }
}

/** Scattered celebration confetti (seeded, so a redraw looks the same). */
class ConfettiView(context: Context, seed: Int) : View(context) {
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rnd = Random(seed)
    private val colors = intArrayOf(GREEN, YELLOW, BLUE, PINK, PURPLE, ORANGE)
    private data class Piece(val fx: Float, val fy: Float, val size: Float, val rot: Float, val color: Int, val shape: Int)
    private val pieces = List(42) {
        Piece(rnd.nextFloat(), rnd.nextFloat(), 0.5f + rnd.nextFloat(), rnd.nextFloat() * 360f,
            colors[it % colors.size], it % 3)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val unit = min(width, height) / 9f
        for (pc in pieces) {
            val x = pc.fx * width
            val y = pc.fy * height
            val s = unit * pc.size
            p.color = pc.color
            canvas.save()
            canvas.translate(x, y)
            canvas.rotate(pc.rot)
            when (pc.shape) {
                0 -> canvas.drawRect(-s / 2, -s / 4, s / 2, s / 4, p)
                1 -> canvas.drawCircle(0f, 0f, s / 3, p)
                else -> {
                    val tri = Path()
                    tri.moveTo(0f, -s / 2); tri.lineTo(s / 2, s / 2); tri.lineTo(-s / 2, s / 2); tri.close()
                    canvas.drawPath(tri, p)
                }
            }
            canvas.restore()
        }
    }
}

/**
 * A road sign drawn in code, for figure questions (v0.7.0). Keys:
 * "sign_stop" （止まれ）, "sign_arrow" （指定方向以外進行禁止）, "sign_warning" （警戒）.
 * Stylised after the real signs (警察庁 道路標識）.
 */
class SignView(context: Context, private val key: String) : View(context) {
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cx = width / 2f
        val cy = height / 2f
        val r = min(width, height) / 2f * 0.92f
        when (key) {
            "sign_stop" -> {
                // White border triangle, red inside, white 止まれ.
                val outer = Path()
                outer.moveTo(cx, cy + r)
                outer.lineTo(cx - r, cy - r * 0.82f)
                outer.lineTo(cx + r, cy - r * 0.82f)
                outer.close()
                p.color = Color.WHITE
                p.style = Paint.Style.FILL
                canvas.drawPath(outer, p)
                val inner = Path()
                inner.moveTo(cx, cy + r * 0.86f)
                inner.lineTo(cx - r * 0.84f, cy - r * 0.68f)
                inner.lineTo(cx + r * 0.84f, cy - r * 0.68f)
                inner.close()
                p.color = Color.rgb(0xE6, 0x00, 0x12)
                canvas.drawPath(inner, p)
                p.color = Color.WHITE
                p.textAlign = Paint.Align.CENTER
                p.typeface = Typeface.DEFAULT_BOLD
                p.textSize = r * 0.52f
                val fm = p.fontMetrics
                canvas.drawText("止まれ", cx, cy - r * 0.05f - (fm.ascent + fm.descent) / 2, p)
            }
            "sign_arrow" -> {
                p.color = Color.rgb(0x00, 0x4B, 0xA5)
                p.style = Paint.Style.FILL
                canvas.drawCircle(cx, cy, r, p)
                // White arrow pointing up: shaft + head.
                p.color = Color.WHITE
                canvas.drawRect(cx - r * 0.13f, cy - r * 0.05f, cx + r * 0.13f, cy + r * 0.62f, p)
                val head = Path()
                head.moveTo(cx, cy - r * 0.68f)
                head.lineTo(cx + r * 0.42f, cy - r * 0.02f)
                head.lineTo(cx - r * 0.42f, cy - r * 0.02f)
                head.close()
                canvas.drawPath(head, p)
            }
            "sign_noparking" -> {
                // Blue disc, red ring and diagonal slash = 駐車禁止.
                p.color = Color.rgb(0x00, 0x4B, 0xA5)
                p.style = Paint.Style.FILL
                canvas.drawCircle(cx, cy, r, p)
                p.color = Color.rgb(0xE6, 0x00, 0x12)
                p.style = Paint.Style.STROKE
                p.strokeWidth = r * 0.24f
                canvas.drawCircle(cx, cy, r * 0.86f, p)
                // v0.9.0 audit fix: the slash must be a filled bar — the paint
                // was still in STROKE style, which drew only its outline.
                p.style = Paint.Style.FILL
                canvas.save()
                canvas.rotate(45f, cx, cy)
                canvas.drawRect(cx - r * 0.10f, cy - r * 0.95f, cx + r * 0.10f, cy + r * 0.95f, p)
                canvas.restore()
            }
            "sign_speed" -> {
                // White disc, red ring, black number = 最高速度.
                p.color = Color.WHITE
                p.style = Paint.Style.FILL
                canvas.drawCircle(cx, cy, r, p)
                p.color = Color.rgb(0xE6, 0x00, 0x12)
                p.style = Paint.Style.STROKE
                p.strokeWidth = r * 0.20f
                canvas.drawCircle(cx, cy, r * 0.88f, p)
                p.color = INK
                p.style = Paint.Style.FILL
                p.textAlign = Paint.Align.CENTER
                p.typeface = Typeface.DEFAULT_BOLD
                p.textSize = r * 0.95f
                val fm2 = p.fontMetrics
                canvas.drawText("50", cx, cy - (fm2.ascent + fm2.descent) / 2, p)
            }
            "sign_oneway" -> {
                // Blue rounded rectangle, white arrow to the right = 一方通行.
                val rect = android.graphics.RectF(cx - r, cy - r * 0.62f, cx + r, cy + r * 0.62f)
                p.color = Color.rgb(0x00, 0x4B, 0xA5)
                p.style = Paint.Style.FILL
                canvas.drawRoundRect(rect, r * 0.18f, r * 0.18f, p)
                p.color = Color.WHITE
                canvas.drawRect(cx - r * 0.62f, cy - r * 0.14f, cx + r * 0.18f, cy + r * 0.14f, p)
                val head = Path()
                head.moveTo(cx + r * 0.72f, cy)
                head.lineTo(cx + r * 0.12f, cy - r * 0.40f)
                head.lineTo(cx + r * 0.12f, cy + r * 0.40f)
                head.close()
                canvas.drawPath(head, p)
            }
            else -> { // sign_warning: yellow diamond with black "!"
                val dia = Path()
                dia.moveTo(cx, cy - r)
                dia.lineTo(cx + r, cy)
                dia.lineTo(cx, cy + r)
                dia.lineTo(cx - r, cy)
                dia.close()
                p.color = Color.rgb(0xFF, 0xD2, 0x00)
                p.style = Paint.Style.FILL
                canvas.drawPath(dia, p)
                p.color = INK
                p.style = Paint.Style.STROKE
                p.strokeWidth = r * 0.07f
                canvas.drawPath(dia, p)
                p.style = Paint.Style.FILL
                p.textAlign = Paint.Align.CENTER
                p.typeface = Typeface.DEFAULT_BOLD
                p.textSize = r * 1.05f
                val fm = p.fontMetrics
                canvas.drawText("!", cx, cy - (fm.ascent + fm.descent) / 2, p)
            }
        }
    }
}

/** White pictogram drawn over a category colour circle. kind = category index. */
class CategoryIconDrawable(private val kind: Int, private val accent: Int) : Drawable() {
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun draw(canvas: Canvas) {
        val b = bounds
        val cx = b.exactCenterX()
        val cy = b.exactCenterY()
        val u = min(b.width(), b.height()) / 100f // unit: 100 = full size
        p.color = Color.WHITE
        p.style = Paint.Style.FILL
        when (kind) {
            0 -> { // 敬語: speech bubble with dots
                val rect = RectF(cx - 34 * u, cy - 26 * u, cx + 34 * u, cy + 16 * u)
                canvas.drawRoundRect(rect, 14 * u, 14 * u, p)
                val tail = Path()
                tail.moveTo(cx - 14 * u, cy + 14 * u)
                tail.lineTo(cx - 4 * u, cy + 32 * u)
                tail.lineTo(cx + 8 * u, cy + 14 * u)
                tail.close()
                canvas.drawPath(tail, p)
                p.color = accent
                for (dx in listOf(-18f, 0f, 18f)) canvas.drawCircle(cx + dx * u, cy - 5 * u, 5.5f * u, p)
            }
            1 -> { // マナー: heart
                val heart = Path()
                heart.moveTo(cx, cy + 30 * u)
                heart.cubicTo(cx - 52 * u, cy + 2 * u, cx - 30 * u, cy - 34 * u, cx, cy - 14 * u)
                heart.cubicTo(cx + 30 * u, cy - 34 * u, cx + 52 * u, cy + 2 * u, cx, cy + 30 * u)
                canvas.drawPath(heart, p)
            }
            2 -> { // 漢字: the glyph 文 in white
                p.textAlign = Paint.Align.CENTER
                p.typeface = Typeface.DEFAULT_BOLD
                p.textSize = 62 * u
                val fm = p.fontMetrics
                canvas.drawText("文", cx, cy - (fm.ascent + fm.descent) / 2, p)
            }
            3 -> { // 標識: warning triangle with "!"
                val tri = Path()
                tri.moveTo(cx, cy - 34 * u)
                tri.lineTo(cx + 36 * u, cy + 28 * u)
                tri.lineTo(cx - 36 * u, cy + 28 * u)
                tri.close()
                canvas.drawPath(tri, p)
                p.color = accent
                canvas.drawRect(cx - 3.5f * u, cy - 8 * u, cx + 3.5f * u, cy + 12 * u, p)
                canvas.drawCircle(cx, cy + 20 * u, 4 * u, p)
            }
            else -> { // 社会: yen coin
                canvas.drawCircle(cx, cy, 32 * u, p)
                p.color = accent
                p.textAlign = Paint.Align.CENTER
                p.typeface = Typeface.DEFAULT_BOLD
                p.textSize = 56 * u
                val fm = p.fontMetrics
                canvas.drawText("¥", cx, cy - (fm.ascent + fm.descent) / 2, p)
            }
        }
    }

    override fun setAlpha(alpha: Int) { p.alpha = alpha }
    override fun setColorFilter(filter: android.graphics.ColorFilter?) { p.colorFilter = filter }
    @Deprecated("Drawable opacity")
    override fun getOpacity(): Int = android.graphics.PixelFormat.TRANSLUCENT
}

/** Small white/grey pictograms for the tab bar. kind: 0 home, 1 review, 2 grid. */
class TabIconDrawable(private val kind: Int, private val color: Int) : Drawable() {
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun draw(canvas: Canvas) {
        val b = bounds
        val cx = b.exactCenterX()
        val cy = b.exactCenterY()
        val u = min(b.width(), b.height()) / 100f
        p.color = color
        p.style = Paint.Style.FILL
        when (kind) {
            0 -> { // house
                val roof = Path()
                roof.moveTo(cx - 38 * u, cy - 2 * u)
                roof.lineTo(cx, cy - 36 * u)
                roof.lineTo(cx + 38 * u, cy - 2 * u)
                roof.lineTo(cx + 26 * u, cy - 2 * u)
                roof.lineTo(cx, cy - 24 * u)
                roof.lineTo(cx - 26 * u, cy - 2 * u)
                roof.close()
                canvas.drawPath(roof, p)
                canvas.drawRect(cx - 24 * u, cy - 2 * u, cx + 24 * u, cy + 32 * u, p)
                p.color = Color.WHITE
                canvas.drawRect(cx - 7 * u, cy + 10 * u, cx + 7 * u, cy + 32 * u, p)
            }
            1 -> { // circular arrows (review)
                p.style = Paint.Style.STROKE
                p.strokeWidth = 13 * u
                p.strokeCap = Paint.Cap.ROUND
                canvas.drawArc(RectF(cx - 28 * u, cy - 28 * u, cx + 28 * u, cy + 28 * u), -40f, 280f, false, p)
                p.style = Paint.Style.FILL
                val arrow = Path()
                arrow.moveTo(cx + 30 * u, cy - 40 * u)
                arrow.lineTo(cx + 44 * u, cy - 12 * u)
                arrow.lineTo(cx + 12 * u, cy - 14 * u)
                arrow.close()
                canvas.drawPath(arrow, p)
            }
            else -> { // 2x2 grid (categories)
                val r = 12 * u
                for (dx in listOf(-20f, 20f)) for (dy in listOf(-20f, 20f)) {
                    canvas.drawRoundRect(RectF(cx + dx * u - 16 * u, cy + dy * u - 16 * u,
                        cx + dx * u + 16 * u, cy + dy * u + 16 * u), r, r, p)
                }
            }
        }
    }

    override fun setAlpha(alpha: Int) { p.alpha = alpha }
    override fun setColorFilter(filter: android.graphics.ColorFilter?) { p.colorFilter = filter }
    @Deprecated("Drawable opacity")
    override fun getOpacity(): Int = android.graphics.PixelFormat.TRANSLUCENT
}
