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
private val OUTLINE = Color.rgb(0x93, 0xA9, 0xD1) // pale steel: mascot silhouette on the midnight card (UX3-11)
private val ORANGE = Color.rgb(0xC9, 0xA8, 0x6A)
private val YELLOW = Color.rgb(0xFF, 0xC8, 0x00)
private val BLUE = Color.rgb(0x1C, 0xB0, 0xF6)
private val PINK = Color.rgb(0xFF, 0x86, 0xD0)
private val PURPLE = Color.rgb(0xA5, 0x60, 0xF5)
private val INK = Color.rgb(0x3C, 0x3C, 0x3C)

private val WOOD = Color.rgb(0x92, 0x6C, 0x42)
private val WOOD_DARK = Color.rgb(0x74, 0x54, 0x30)
private val GHOST = Color.rgb(0xC9, 0xCF, 0xD8)

/**
 * One bookshelf row (v0.14.0): books in the category colour fill the shelf
 * as the category's graduation ratio grows; empty slots stay as faint
 * outlines so the remaining room is visible, and a completed category's
 * last book turns gold. Board is drawn last so book bottoms tuck behind it.
 */
class ShelfView(context: Context, private val bookColor: Int, private val books: Int, private val complete: Boolean) : View(context) {
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return
        val boardH = h * 0.20f
        val boardTop = h - boardH
        val slots = Bookshelf.BOOKS_PER_SHELF
        val slotW = w / slots
        for (i in 0 until slots) {
            val cx = slotW * i + slotW / 2f
            val frac = 0.68f + ((i * 29) % 4) * 0.08f
            val bookH = (boardTop - h * 0.06f) * frac
            // Upright-book proportions: clearly taller than wide, with air
            // between neighbours (wide capsules read as pills, not books).
            val bookW = min(slotW * 0.68f, bookH * 0.66f)
            val rect = RectF(cx - bookW / 2f, boardTop - bookH, cx + bookW / 2f, boardTop + boardH * 0.3f)
            if (i < books) {
                val isGold = complete && i == books - 1
                p.style = Paint.Style.FILL
                p.color = if (isGold) ORANGE else bookColor
                canvas.drawRoundRect(rect, bookW * 0.16f, bookW * 0.16f, p)
                // A small band near the top of each spine.
                p.color = Color.argb(70, 0, 0, 0)
                canvas.drawRect(rect.left, rect.top + bookH * 0.16f, rect.right, rect.top + bookH * 0.24f, p)
            } else {
                p.style = Paint.Style.STROKE
                p.strokeWidth = maxOf(1.5f, h * 0.02f)
                p.color = GHOST
                canvas.drawRoundRect(rect, bookW * 0.16f, bookW * 0.16f, p)
                p.style = Paint.Style.FILL
            }
        }
        p.color = WOOD
        canvas.drawRoundRect(RectF(0f, boardTop, w, h), boardH * 0.4f, boardH * 0.4f, p)
        p.color = WOOD_DARK
        canvas.drawRect(0f, boardTop, w, boardTop + boardH * 0.28f, p)
    }
}

/**
 * The mascot owl (v0.14.0: grows with the level — OwlGrowth). stage 0 is
 * the egg, 1 the chick, 2 the young owl, 3-4 the branch forms: cute
 * (rounder, ribbon, cheeks, flower) or cool (tall tufts, stern brows, gold
 * scarf, emblem), with the plain adult as the undecided form.
 */
class MascotView(context: Context, private val stage: Int = 2, private val path: Int = 0) : View(context) {
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)

    private fun drawEgg(canvas: Canvas, w: Float, h: Float) {
        val cx = w / 2f
        val u = min(w, h)
        val eggR = u * 0.30f
        val eggCy = h * 0.56f
        // Egg first, nest strokes layered over its base.
        p.color = Color.rgb(0xFF, 0xF8, 0xEC)
        canvas.drawOval(RectF(cx - eggR * 0.82f, eggCy - eggR, cx + eggR * 0.82f, eggCy + eggR * 0.9f), p)
        p.style = Paint.Style.STROKE
        p.strokeWidth = u * 0.02f
        p.color = Color.rgb(0xCB, 0xB8, 0x96)
        canvas.drawOval(RectF(cx - eggR * 0.82f, eggCy - eggR, cx + eggR * 0.82f, eggCy + eggR * 0.9f), p)
        val crack = Path()
        crack.moveTo(cx - eggR * 0.30f, eggCy - eggR * 0.52f)
        crack.lineTo(cx - eggR * 0.10f, eggCy - eggR * 0.38f)
        crack.lineTo(cx + eggR * 0.08f, eggCy - eggR * 0.54f)
        crack.lineTo(cx + eggR * 0.28f, eggCy - eggR * 0.40f)
        canvas.drawPath(crack, p)
        p.strokeCap = Paint.Cap.ROUND
        for (i in 0 until 3) {
            p.color = if (i % 2 == 0) WOOD else WOOD_DARK
            p.strokeWidth = u * 0.05f
            val yy = eggCy + eggR * (0.52f + i * 0.17f)
            canvas.drawLine(cx - eggR * 1.28f, yy, cx + eggR * 1.28f, yy - u * 0.02f, p)
        }
        p.style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        val cx = w / 2f
        if (stage <= 0) { drawEgg(canvas, w, h); return }
        val stageScale = when { stage == 1 -> 0.66f; stage == 2 -> 0.9f; else -> 1f }
        val bodyR = min(w, h) * 0.40f * stageScale
        val bodyCy = if (stage == 1) h * 0.60f else h * 0.54f

        // Cool path (stage 3+): extra tall, sharp tufts behind the usual ones.
        if (path == OwlGrowth.PATH_COOL && stage >= 3) {
            p.color = GREEN_DARK
            val spike = Path()
            spike.moveTo(cx - bodyR * 0.86f, bodyCy - bodyR * 0.55f)
            spike.lineTo(cx - bodyR * 0.60f, bodyCy - bodyR * 1.12f)
            spike.lineTo(cx - bodyR * 0.34f, bodyCy - bodyR * 0.52f)
            spike.close()
            canvas.drawPath(spike, p)
            val spike2 = Path()
            spike2.moveTo(cx + bodyR * 0.86f, bodyCy - bodyR * 0.55f)
            spike2.lineTo(cx + bodyR * 0.60f, bodyCy - bodyR * 1.12f)
            spike2.lineTo(cx + bodyR * 0.34f, bodyCy - bodyR * 0.52f)
            spike2.close()
            canvas.drawPath(spike2, p)
        }

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
        // Silhouette outline (UX3-11): the body shares the header card's
        // midnight, so without this ring only the belly and eyes would read.
        p.style = Paint.Style.STROKE
        p.strokeWidth = bodyR * 0.09f
        p.color = OUTLINE
        canvas.drawCircle(cx, bodyCy, bodyR - p.strokeWidth / 2f, p)
        p.style = Paint.Style.FILL
        // Belly
        p.color = Color.rgb(0xF5, 0xED, 0xD8)
        canvas.drawOval(RectF(cx - bodyR * 0.52f, bodyCy + bodyR * 0.18f, cx + bodyR * 0.52f, bodyCy + bodyR * 0.98f), p)

        // Eyes (bigger for the chick and the cute path)
        val eyeY = bodyCy - bodyR * 0.22f
        val eyeDx = bodyR * 0.38f
        val eyeR = bodyR * 0.30f * if (stage == 1 || path == OwlGrowth.PATH_CUTE) 1.18f else 1f
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

        // Chick: a two-leaf sprout on the head.
        if (stage == 1) {
            p.color = Color.rgb(0x58, 0xA8, 0x5A)
            canvas.drawOval(RectF(cx - bodyR * 0.30f, bodyCy - bodyR * 1.22f, cx - bodyR * 0.02f, bodyCy - bodyR * 0.98f), p)
            canvas.drawOval(RectF(cx + bodyR * 0.02f, bodyCy - bodyR * 1.22f, cx + bodyR * 0.30f, bodyCy - bodyR * 0.98f), p)
        }

        // Cute path (stage 3+): pink cheeks and a ribbon; final form adds a
        // flower and sparkles.
        if (path == OwlGrowth.PATH_CUTE && stage >= 3) {
            p.color = Color.argb(150, 0xFF, 0x86, 0xD0)
            canvas.drawCircle(cx - bodyR * 0.68f, eyeY + bodyR * 0.36f, bodyR * 0.16f, p)
            canvas.drawCircle(cx + bodyR * 0.68f, eyeY + bodyR * 0.36f, bodyR * 0.16f, p)
            val rx = cx + bodyR * 0.52f
            val ry = bodyCy - bodyR * 0.94f
            p.color = PINK
            val wing1 = Path()
            wing1.moveTo(rx, ry); wing1.lineTo(rx - bodyR * 0.30f, ry - bodyR * 0.16f); wing1.lineTo(rx - bodyR * 0.30f, ry + bodyR * 0.16f); wing1.close()
            canvas.drawPath(wing1, p)
            val wing2 = Path()
            wing2.moveTo(rx, ry); wing2.lineTo(rx + bodyR * 0.30f, ry - bodyR * 0.16f); wing2.lineTo(rx + bodyR * 0.30f, ry + bodyR * 0.16f); wing2.close()
            canvas.drawPath(wing2, p)
            p.color = Color.rgb(0xE0, 0x5B, 0xA8)
            canvas.drawCircle(rx, ry, bodyR * 0.10f, p)
            if (stage >= 4) {
                val fx = cx - bodyR * 0.55f
                val fy = bodyCy - bodyR * 0.97f
                p.color = PURPLE
                for (k in 0 until 5) {
                    val a = Math.toRadians(-90.0 + k * 72.0)
                    canvas.drawCircle(fx + (bodyR * 0.14f * cos(a)).toFloat(), fy + (bodyR * 0.14f * sin(a)).toFloat(), bodyR * 0.10f, p)
                }
                p.color = ORANGE
                canvas.drawCircle(fx, fy, bodyR * 0.09f, p)
                p.color = ORANGE
                for (sx in floatArrayOf(cx - bodyR * 1.22f, cx + bodyR * 1.22f)) {
                    val spark = Path()
                    val sy = bodyCy - bodyR * 0.10f
                    val sr = bodyR * 0.13f
                    spark.moveTo(sx, sy - sr); spark.lineTo(sx + sr * 0.3f, sy - sr * 0.3f); spark.lineTo(sx + sr, sy)
                    spark.lineTo(sx + sr * 0.3f, sy + sr * 0.3f); spark.lineTo(sx, sy + sr); spark.lineTo(sx - sr * 0.3f, sy + sr * 0.3f)
                    spark.lineTo(sx - sr, sy); spark.lineTo(sx - sr * 0.3f, sy - sr * 0.3f); spark.close()
                    canvas.drawPath(spark, p)
                }
            }
        }

        // Cool path (stage 3+): stern brows and a gold scarf following the
        // body's curve; the final form adds a gold emblem on the belly.
        if (path == OwlGrowth.PATH_COOL && stage >= 3) {
            p.color = INK
            p.style = Paint.Style.STROKE
            p.strokeCap = Paint.Cap.ROUND
            p.strokeWidth = bodyR * 0.09f
            canvas.drawLine(cx - eyeDx - eyeR * 0.85f, eyeY - eyeR * 1.05f, cx - eyeDx + eyeR * 0.75f, eyeY - eyeR * 0.62f, p)
            canvas.drawLine(cx + eyeDx + eyeR * 0.85f, eyeY - eyeR * 1.05f, cx + eyeDx - eyeR * 0.75f, eyeY - eyeR * 0.62f, p)
            p.color = ORANGE
            p.strokeWidth = bodyR * 0.20f
            canvas.drawArc(RectF(cx - bodyR, bodyCy - bodyR, cx + bodyR, bodyCy + bodyR), 35f, 110f, false, p)
            p.style = Paint.Style.FILL
            p.color = ORANGE
            canvas.drawCircle(cx + bodyR * 0.66f, bodyCy + bodyR * 0.80f, bodyR * 0.13f, p)
            if (stage >= 4) {
                p.color = ORANGE
                canvas.drawCircle(cx, bodyCy + bodyR * 0.52f, bodyR * 0.21f, p)
                p.color = Color.WHITE
                val star = Path()
                val scx = cx; val scy = bodyCy + bodyR * 0.52f; val sr2 = bodyR * 0.13f
                for (i in 0 until 10) {
                    val rad = if (i % 2 == 0) sr2 else sr2 * 0.45f
                    val ang = Math.toRadians(-90.0 + i * 36.0)
                    val x = scx + (rad * cos(ang)).toFloat()
                    val y = scy + (rad * sin(ang)).toFloat()
                    if (i == 0) star.moveTo(x, y) else star.lineTo(x, y)
                }
                star.close()
                canvas.drawPath(star, p)
            }
        }
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
class ConfettiView(context: Context, seed: Int, count: Int = 42) : View(context) {
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rnd = Random(seed)
    private val colors = intArrayOf(GREEN, YELLOW, BLUE, PINK, PURPLE, ORANGE)
    private data class Piece(val fx: Float, val fy: Float, val size: Float, val rot: Float, val color: Int, val shape: Int)
    private val pieces = List(count) {
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
