package com.taigashinohara.otonalevel100

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.io.FileOutputStream

/**
 * Pre-delivery UI verification (AGENTS.md standard, 2026-11-23): the main
 * screens are rendered on the JVM with Robolectric (native graphics), checked
 * for the layout-collapse failure mode (zero-height / missing content), driven
 * through the real tab/session navigation, and captured as PNG screenshots
 * (Roborazzi + a plain bitmap dump under build/outputs/ui-check/ for eyeball
 * review). This app has no onboarding yet (pre-store backlog), so the covered
 * set is: home (first screen), question, categories, review, result.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w360dp-h640dp")
class UiVerificationTest {

    // Note: captures use the file overload of captureRoboImage. The
    // RoborazziRule + LastImage combination requires a Compose capture root
    // and throws for plain View hierarchies like this app's.

    private val tabLabels = setOf("ホーム", "復習", "カテゴリ")

    private fun launch(): MainActivity {
        val activity = Robolectric.buildActivity(MainActivity::class.java).setup().get()
        settle(activity)
        return activity
    }

    /** Idle the main looper and force a full measure/layout pass at phone size. */
    private fun settle(activity: MainActivity) {
        shadowOf(Looper.getMainLooper()).idle()
        val decor = activity.window.decorView
        val w = 1080
        val h = 1920
        decor.measure(
            View.MeasureSpec.makeMeasureSpec(w, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(h, View.MeasureSpec.EXACTLY),
        )
        decor.layout(0, 0, w, h)
        shadowOf(Looper.getMainLooper()).idle()
    }

    private fun allViews(root: View): List<View> {
        val out = ArrayList<View>()
        fun walk(v: View) {
            out += v
            if (v is ViewGroup) for (i in 0 until v.childCount) walk(v.getChildAt(i))
        }
        walk(root)
        return out
    }

    private fun texts(activity: MainActivity): List<String> =
        allViews(activity.window.decorView).filterIsInstance<TextView>().map { it.text.toString() }

    private fun findButton(activity: MainActivity, text: String): Button? =
        allViews(activity.window.decorView).filterIsInstance<Button>()
            .firstOrNull { it.text.toString() == text && it.isShown }

    private fun click(activity: MainActivity, text: String) {
        val b = findButton(activity, text)
            ?: throw AssertionError("button not found or not shown: $text (texts=${texts(activity)})")
        b.performClick()
        settle(activity)
    }

    /** The Migakeru guard: key visible labels must have real height. */
    private fun assertNoCollapse(activity: MainActivity, vararg labels: String) {
        val views = allViews(activity.window.decorView)
        assertTrue("decorView has no height", activity.window.decorView.height > 0)
        for (label in labels) {
            val v = views.filterIsInstance<TextView>().firstOrNull { it.text.toString().contains(label) && it.isShown }
            assertTrue("label not visible: $label (texts=${texts(activity)})", v != null)
            assertTrue("label has zero height (layout collapse?): $label", v!!.height > 0)
        }
    }

    /** Roborazzi capture + a plain PNG dump that always lands on disk. */
    private fun capture(activity: MainActivity, name: String) {
        val decor = activity.window.decorView
        // The test JVM runs with the app module dir as cwd (see run_tests.sh).
        val roboDir = File("build/outputs/roborazzi").apply { mkdirs() }
        decor.captureRoboImage(File(roboDir, "$name.png"))
        val dir = File("build/outputs/ui-check").apply { mkdirs() }
        val bmp = Bitmap.createBitmap(decor.width, decor.height, Bitmap.Config.ARGB_8888)
        decor.draw(Canvas(bmp))
        FileOutputStream(File(dir, "$name.png")).use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    @Test
    fun home_firstScreen_renders() {
        val a = launch()
        assertNoCollapse(a, "今日の5問", "今日の5問をはじめる")
        capture(a, "home")
    }

    @Test
    fun session_tabsStayEnabled_andSessionSurvivesTabLeave() {
        val a = launch()
        click(a, "今日の5問をはじめる")
        // Question screen is up (progress label "n / 5") ...
        assertTrue("question screen not shown: ${texts(a)}", texts(a).any { it.contains("/ 5") })
        // ... and the v0.9.0 fix: every tab is enabled mid-session.
        for (label in tabLabels) {
            val tab = findButton(a, label)
            assertTrue("tab missing mid-session: $label", tab != null)
            assertTrue("tab disabled mid-session (v0.9.0 regression): $label", tab!!.isEnabled)
        }
        capture(a, "question")
        // Leaving via a tab must keep the session: Home offers resume.
        click(a, "カテゴリ")
        assertNoCollapse(a, "カテゴリ")
        capture(a, "categories")
        click(a, "ホーム")
        assertTrue("resume not offered after tab leave: ${texts(a)}", texts(a).any { it.contains("つづきから") })
        // And resuming lands back on the question screen.
        click(a, texts(a).first { it.contains("つづきから") })
        assertTrue("resume did not return to the question: ${texts(a)}", texts(a).any { it.contains("/ 5") })
    }

    @Test
    fun reviewScreen_renders() {
        val a = launch()
        click(a, "復習")
        assertNoCollapse(a, "復習", "今日の復習", "苦手")
        capture(a, "review")
    }

    @Test
    fun fullSession_reachesResult() {
        val a = launch()
        click(a, "今日の5問をはじめる")
        var guard = 0
        while (guard++ < 30) {
            if (texts(a).any { it.contains("問中") && it.contains("正解") }) break
            val views = allViews(a.window.decorView).filterIsInstance<Button>().filter { it.isShown && it.isEnabled }
            val next = views.firstOrNull { it.text.toString() == "次へ" || it.text.toString() == "結果を見る" }
            val flip = views.firstOrNull { it.text.toString() == "カードをめくる" }
            when {
                next != null -> { next.performClick(); settle(a) }
                flip != null -> {
                    flip.performClick(); settle(a)
                    click(a, "思い出せた")
                }
                else -> {
                    val choice = views.firstOrNull {
                        val t = it.text.toString()
                        t !in tabLabels && t !in setOf("次へ", "結果を見る", "カードをめくる", "思い出せた", "まだだった")
                    } ?: throw AssertionError("no clickable choice on question screen: ${texts(a)}")
                    choice.performClick(); settle(a)
                }
            }
        }
        assertTrue("result screen not reached: ${texts(a)}", texts(a).any { it.contains("問中") && it.contains("正解") })
        assertNoCollapse(a, "今日の分は完了")
        capture(a, "result")
    }
}
