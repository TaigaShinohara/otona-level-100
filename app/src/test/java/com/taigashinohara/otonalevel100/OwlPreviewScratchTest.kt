package com.taigashinohara.otonalevel100

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.io.FileOutputStream

/** SCRATCH (not in run_tests.sh): renders every owl stage/path to one PNG
 *  so the growth forms can be eyeballed before a user ever reaches them. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w360dp-h640dp")
class OwlPreviewScratchTest {
    @Test fun renderAllForms() {
        val ctx = RuntimeEnvironment.getApplication()
        val root = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(0x22, 0x35, 0x5E))
            setPadding(24, 24, 24, 24)
        }
        fun row(label: String, forms: List<Pair<Int, Int>>, level: Int = 0) {
            val tv = TextView(ctx).apply { text = label; setTextColor(Color.WHITE); textSize = 16f }
            root.addView(tv)
            val r = LinearLayout(ctx).apply { orientation = LinearLayout.HORIZONTAL }
            forms.forEach { (stage, path) ->
                r.addView(MascotView(ctx, stage, path, level), LinearLayout.LayoutParams(300, 330))
            }
            root.addView(r)
        }
        row("stage0 egg", listOf(0 to 0))
        row("stage1 chick", listOf(1 to 0))
        row("stage2 young", listOf(2 to 0))
        row("stage3 none/cute/cool", listOf(3 to 0, 3 to 1, 3 to 2))
        row("stage4 none/cute/cool", listOf(4 to 0, 4 to 1, 4 to 2))
        // v0.18.0 milestone accessories (verified visually 2026-11-28):
        // Lv.40 gold badge, Lv.50 badge + crown, on stages 3/4, all paths.
        row("Lv40 badge stage3 none/cute/cool", listOf(3 to 0, 3 to 1, 3 to 2), level = 40)
        row("Lv40 badge stage4 none/cute/cool", listOf(4 to 0, 4 to 1, 4 to 2), level = 40)
        row("Lv50 badge+crown stage3 none/cute/cool", listOf(3 to 0, 3 to 1, 3 to 2), level = 50)
        row("Lv50 badge+crown stage4 none/cute/cool", listOf(4 to 0, 4 to 1, 4 to 2), level = 50)
        val w = 1000
        root.measure(
            View.MeasureSpec.makeMeasureSpec(w, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
        )
        root.layout(0, 0, w, root.measuredHeight)
        val bmp = Bitmap.createBitmap(w, root.measuredHeight, Bitmap.Config.ARGB_8888)
        root.draw(Canvas(bmp))
        val dir = File("build/outputs/ui-check").apply { mkdirs() }
        FileOutputStream(File(dir, "owl-preview.png")).use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
}
