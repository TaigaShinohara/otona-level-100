package com.taigashinohara.otonalevel100

import android.animation.ValueAnimator
import android.app.Activity
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.StateListDrawable
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.time.LocalDate
import kotlin.random.Random

/**
 * Otona Level 100 v0.14.0.
 * v0.14.0: the home screen grows a bookshelf (concept D, chosen by the user
 * from a 4-concept board): one shelf per category, books in the category
 * colour fill in with the graduation ratio, empty slots stay as faint
 * outlines, and a completed category's last book turns gold. It replaces
 * the dots + mini bars (UX3-06) that showed the same data; percentages
 * remain available in the card's content description.
 * v0.13.0: celebration release (user request: praise the user much more,
 * with every delightful reaction available). Correct answers get rotating
 * praise headlines and streak lines (Praise), a haptic tick, a soft
 * synthesized chime, a bounce on the winning button, a pop-in banner and a
 * popping "+N XP" label. Level-ups and perfect sets get a haptic fanfare
 * and a rising chime. The result screen celebrates in words at every score,
 * earned stars pop in one by one, the level-up pill pops, and the confetti
 * amount scales with the score. Wrong answers deliberately get none of it.
 * Adds the VIBRATE permission (the app's only permission).
 * v0.12.0: level design (design v1.3 §3.5) implemented. Every question now
 * carries a difficulty tag (1 easy / 2 normal / 3 hard; approved tagging
 * pass, 48/34/22). Daily new questions are picked by the level band's
 * difficulty mix with closest-difficulty fallback (SessionDesign.pickNew),
 * and the daily set is ordered into the wave — review opener, new questions,
 * the hardest at position 4, review closer (SessionDesign.orderWave) —
 * instead of a plain shuffle. The XP curve is smoothed to the title bands
 * (+20 XP per band, 10,350 total) and a stored level floor guarantees a
 * curve change never lowers an existing level. A title change on level-up
 * gets its own line on the result screen (the milestone treatment).
 * v0.11.0: UX review round 3 fixes (docs/UX_REVIEW_v0-10-0.md; verdict was
 * pass with recommendations). All six recommendations resolved: level-bar
 * fill is gold on every screen (UX3-04), card questions no longer repeat
 * their prompt/figure above the card (UX3-02), the question header states
 * the category once (UX3-03), the category rate is the bar plus exact
 * counts only (UX3-01), the review mechanics text shows in full once and
 * then behind a toggle (UX3-05), and Home gains the small category-balance
 * dots + mini bars the design specifies (UX3-06). Plus three light
 * optionals: no duplicated Lv/title line on a level-up result (UX3-08),
 * the categories footer wording matches what is counted (UX3-10), and the
 * mascot gets a pale outline so its silhouette reads on the midnight
 * card (UX3-11).
 * v0.10.0: recolour to Midnight + Gold (ART_DIRECTION v2.1, user picked
 * proposal A2 on 2026-11-25). Brand green/blue/yellow replaced by midnight
 * navy #22355E / deep shade #182747 / gold #C9A86A; correct-feedback green
 * stays as a semantic colour; the social category colour moves to teal so
 * it no longer reads as the old brand green. Mascot recoloured to match.
 * v0.2.0: UX review fixes (bar animation, gold reserved for level-up, session
 * persistence, shuffled choices, graduation box condition, due-review entry,
 * four font tiers, category mini bars, decluttered home, fixed next footer).
 * v0.3.3: UX review v0.3.2 must-fixes — IR2-1 local calendar day (was UTC,
 * which rolled over at 09:00 JST), IR2-2 graduation now leaves the review
 * pool with spot-check reappearance in the daily set; bundled small fixes:
 * IR2-7 darker yamabuki accent, IR2-8 blur radius in dp, IR2-11 production
 * memo removed from a question, IR2-12 weak-session label shows real count.
 * v0.3.2: principle revision from the Simple Glass chat — colour comes
 * through the glass as light; surfaces are not painted. Category chips and
 * the quiz pill are glass again; category colour remains only in functional
 * points (glyph, dot, progress fill).
 * v0.3.1: fixes from on-device screenshots (via the Simple Glass chat, user
 * approved): status-bar insets in both modes, category bars show the solved
 * ratio in per-category accent colours with glyph chips, the secondary button
 * no longer reads as disabled, tint/accent points added against monotony.
 * v0.3.0: Simple Glass applied app-wide (skill v1.1 canonical values).
 * v0.9.0: full bug-fix pass (user report + audit). Main fix: tabs were dead
 * during a session (showQuestion disabled them and onTabClicked early-returned)
 * — tabs now work mid-session and never discard the saved session implicitly.
 * Audit fixes: off-schedule correct answers no longer earn XP (level farming
 * via repeated genre challenges), the review screen/session now include
 * spot-check-due questions, a short daily set still gets its card, an
 * all-graduated pool falls back to a practice set instead of bouncing tabs,
 * corrupted per-question state can no longer crash stateFor, an emptied
 * restored session is dropped cleanly, and the no-parking sign's slash is
 * filled again (paint style leak in SignView).
 * v0.8.1: cross-model source check (pipeline step 3, Gemini) ran on the keigo
 * pilot batch: 16 OK, 4 flagged. keigo12 replaced (its "correct" answer had
 * divided opinion), explanations of keigo13/14/19 softened/corrected.
 * v0.8.0: question pool 20 -> 104 (user's request) — the keigo pilot batch
 * (keigo5-24, docs/問題データ_パイロット_敬語20.md) plus 16 new questions in
 * each other category, and three more code-drawn signs (noparking / speed /
 * oneway). Note: the pilot's cross-model source check (pipeline step 3) is
 * still pending; treat those 20 as pre-verification content.
 * v0.7.0: figure questions (user's request) — questions whose type needs a
 * picture now draw it in code (SignView in Art.kt): the three sign questions
 * show the actual sign (stop / arrow / warning diamond) and their prompts
 * became "この標識の意味は？". Question.figure carries the figure key.
 * v0.6.0: genre challenge (user's request) — tapping a category starts a
 * session of only that genre (due first, then new, then seen; graduated fill
 * the rest), and each category shows its completion rate (graduated share)
 * as a big % and bar. Genre sessions do not mark the daily set as done.
 * v0.5.0: drawn artwork everywhere (user: "make it exciting with
 * illustrations, shapes and icons") — a drawn owl mascot with a daily speech
 * bubble on Home, confetti + star row on the result, drawn category
 * pictograms (bubble / heart / glyph / sign / coin) and tab icons. All
 * vector-drawn in Art.kt; no image assets.
 * v0.4.0: full visual redesign to a Duolingo-style UI (user's direction;
 * ART_DIRECTION v2.0 supersedes the glass direction for this app): flat vivid
 * colours, chunky 3D-edge buttons, green unit-header home card, coloured
 * category units, green/red tint feedback banners, night-theme dark mode.
 * Logic (XP, Leitner, sessions) is unchanged from v0.3.3.
 */
class MainActivity : Activity() {

    // ---------- Palette: Midnight + Gold (v0.10.0, ART_DIRECTION v2.1) ----------
    // Chunky Duolingo-style shapes and bold type, recoloured at the user's
    // request (2026-11-25): deep midnight navy as the brand primary, muted
    // gold as the only accent (progress fill, level-up, selection). Semantic
    // feedback colours stay: green = correct, red tints = "not quite"
    // (informative, never shaming). Field names keep their v0.4.0 roles:
    // green/greenDark now hold the midnight pair.
    private inner class Palette(dark: Boolean) {
        val bgTop: Int
        val bgBottom: Int
        val surface: Int
        val border: Int
        val track: Int
        val textMain: Int
        val textSub: Int
        val heading: Int
        val green = Color.rgb(0x22, 0x35, 0x5E)
        val greenDark = Color.rgb(0x18, 0x27, 0x47)
        val greenTextOn = Color.WHITE
        val blue = Color.rgb(0x5B, 0x84, 0xC4)
        val tabSelBg: Int
        val tabSelText: Int
        val yellow = Color.rgb(0xC9, 0xA8, 0x6A)
        val yellowText = Color.rgb(0x6B, 0x55, 0x18)
        val correctBg: Int
        val correctBorder: Int
        val correctText: Int
        val wrongBgC: Int
        val wrongBorder: Int
        val wrongText: Int
        val chipBg: Int
        val chipText: Int
        /** Gold text that stays readable on this theme's surfaces (v0.17.0). */
        val goldText: Int
        /** Sub text on the tinted feedback panel (v0.17.0). */
        val feedbackSub: Int
        /** Bright per-category fills (unit colours). */
        val categoryColors: List<Int> = listOf(
            Color.rgb(0x1C, 0xB0, 0xF6), // 敬語 blue
            Color.rgb(0xFF, 0x86, 0xD0), // マナー pink
            Color.rgb(0xA5, 0x60, 0xF5), // 漢字 purple
            Color.rgb(0xFF, 0x96, 0x00), // 標識 orange
            Color.rgb(0x12, 0xA5, 0x94)) // 社会 teal (was brand green; navy is the brand now)
        init {
            if (dark) {
                bgTop = Color.rgb(0x0D, 0x15, 0x24)
                bgBottom = Color.rgb(0x0A, 0x11, 0x1E)
                surface = Color.rgb(0x16, 0x23, 0x3C)
                border = Color.rgb(0x2A, 0x3B, 0x5F)
                track = Color.rgb(0x2A, 0x3B, 0x5F)
                textMain = Color.rgb(0xF2, 0xF5, 0xFA)
                textSub = Color.rgb(0x9A, 0xA7, 0xBD)
                heading = Color.rgb(0xF2, 0xF5, 0xFA)
                tabSelBg = Color.rgb(0x22, 0x35, 0x5E)
                tabSelText = Color.rgb(0xC9, 0xA8, 0x6A)
                correctBg = Color.rgb(0x1D, 0x33, 0x27)
                correctBorder = Color.rgb(0x58, 0xCC, 0x02)
                correctText = Color.rgb(0x8C, 0xE9, 0x9A)
                // v0.17.0: lifted from #3D2428 — the old fill read as a muddy
                // brown mass on the dark background (user screenshot).
                wrongBgC = Color.rgb(0x4A, 0x28, 0x30)
                wrongBorder = Color.rgb(0xFF, 0x4B, 0x4B)
                wrongText = Color.rgb(0xFF, 0xA8, 0xB8)
                chipBg = Color.rgb(0x22, 0x35, 0x5E)
                chipText = Color.rgb(0xC9, 0xA8, 0x6A)
                goldText = Color.rgb(0xC9, 0xA8, 0x6A)
                feedbackSub = Color.rgb(0xC7, 0xD0, 0xDC)
            } else {
                bgTop = Color.WHITE
                bgBottom = Color.rgb(0xF7, 0xF7, 0xF7)
                surface = Color.WHITE
                border = Color.rgb(0xE5, 0xE5, 0xE5)
                track = Color.rgb(0xE5, 0xE5, 0xE5)
                textMain = Color.rgb(0x3C, 0x3C, 0x3C)
                textSub = Color.rgb(0x77, 0x77, 0x77)
                heading = Color.rgb(0x3C, 0x3C, 0x3C)
                tabSelBg = Color.rgb(0xEC, 0xEE, 0xF3)
                tabSelText = Color.rgb(0x8C, 0x6D, 0x1F)
                correctBg = Color.rgb(0xD7, 0xFF, 0xB8)
                correctBorder = Color.rgb(0x58, 0xCC, 0x02)
                correctText = Color.rgb(0x58, 0xA7, 0x00)
                wrongBgC = Color.rgb(0xFF, 0xDF, 0xE0)
                wrongBorder = Color.rgb(0xFF, 0x4B, 0x4B)
                wrongText = Color.rgb(0xEA, 0x2B, 0x2B)
                chipBg = Color.rgb(0xEC, 0xEE, 0xF3)
                chipText = Color.rgb(0x8C, 0x6D, 0x1F)
                goldText = Color.rgb(0x6B, 0x55, 0x18)
                feedbackSub = Color.rgb(0x77, 0x77, 0x77)
            }
        }
    }

    /** Seasonal accent (v0.15.0): the calendar card and the 季節 tag only. */
    private val seasonAccent = Color.rgb(0xC2, 0x18, 0x5B)

    private val pal: Palette by lazy {
        Palette((resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES)
    }
    private val navy get() = pal.heading
    private val grey get() = pal.textSub

    // Font tiers (UX IR-8): hero / heading / body / small only.
    private val sizeHero = 64f
    private val sizeHead = 22f
    private val sizeBody = 16f
    private val sizeSmall = 13f

    private lateinit var content: FrameLayout
    private lateinit var tabButtons: List<Button>
    private val store by lazy { ProgressStore(this) }

    // Session state (persisted via ProgressStore after every step, IR-4)
    private data class SItem(val q: Question, val order: List<Int>, val choices: List<String>, val answerIndex: Int)
    private var sItems: List<SItem> = emptyList()
    private var sIndex = 0
    private var sCorrect = 0
    private var sXp = 0
    private var sStreak = 0
    private var sStartXp = 0
    private var sMode = 0 // 0 daily, 1 weak-only, 2 due-review, 3 genre challenge
    private var sAnswered = false
    private var sChosen = -1
    private var sCounted = false
    // Result-screen growth (v0.17.0): per-item correctness and the
    // per-category graduated counts as they were when the session started.
    private var sResults: BooleanArray = BooleanArray(0)
    private var sStartGrads: List<Int> = emptyList()
    private var inQuestionPhase = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = FrameLayout(this)
        root.addView(buildBackground(), FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))

        val column = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        content = FrameLayout(this)
        column.addView(content, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))

        // Bottom tab bar: white card; the selected tab gets a blue tint pill.
        val tabBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(dp(8), dp(6), dp(8), dp(6))
            background = cardDrawable(dp(24).toFloat())
        }
        val names = listOf("ホーム", "復習", "カテゴリ")
        tabButtons = names.mapIndexed { i, name ->
            Button(this).apply {
                text = name
                textSize = sizeBody
                background = null
                setCompoundDrawablesWithIntrinsicBounds(TabIconDrawable(i, grey), null, null, null)
                compoundDrawablePadding = dp(6)
                setOnClickListener { onTabClicked(i) }
            }
        }
        tabButtons.forEach { tabBar.addView(it, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)) }
        val tabLp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        tabLp.leftMargin = dp(16); tabLp.rightMargin = dp(16); tabLp.bottomMargin = dp(12); tabLp.topMargin = dp(8)
        column.addView(tabBar, tabLp)
        root.addView(column, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        setContentView(root)

        // Edge-to-edge (targetSdk 35): keep content below the status bar and
        // above the navigation bar in BOTH colour modes (v0.3.1 fix: dark-mode
        // titles were sliding under the status bar). The background stays
        // full-bleed; only the content column takes the insets.
        column.setOnApplyWindowInsetsListener { v, insets ->
            v.setPadding(insets.systemWindowInsetLeft, insets.systemWindowInsetTop,
                insets.systemWindowInsetRight, insets.systemWindowInsetBottom)
            insets
        }
        root.requestApplyInsets()
        if ((resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_YES) {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility =
                window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        val saved = store.loadSession()
        if (saved != null && saved.items.isNotEmpty()) {
            restoreSession(saved)
            when {
                // All saved questions vanished from the pool (data update):
                // drop the stale session instead of showing an empty result.
                sItems.isEmpty() -> { store.clearSession(); showScreen(0) }
                sIndex < sItems.size -> showQuestion()
                else -> showResult()
            }
        } else {
            showScreen(0)
        }
    }

    /** Flat foundation: near-white (light) / deep blue-grey (dark night). */
    private fun buildBackground(): View {
        val frame = FrameLayout(this)
        val gradient = View(this).apply {
            background = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(pal.bgTop, pal.bgBottom))
        }
        frame.addView(gradient, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        return frame
    }

    /** Duolingo-style card: flat surface, 2dp border, generous radius. */
    private fun cardDrawable(radius: Float): GradientDrawable =
        GradientDrawable().apply {
            cornerRadius = radius
            setColor(pal.surface)
            setStroke(dp(2), pal.border)
        }

    /** Filled circle in a flat colour (category unit icon). */
    private fun circleDrawable(color: Int): GradientDrawable = GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(color)
    }

    /**
     * Chunky 3D button face: the main colour sits on a darker bottom edge
     * (5dp), which is the Duolingo signature. Pressed state drops the edge.
     */
    private fun chunkyDrawable(radius: Float, top: Int, edge: Int, stroke: Int? = null): LayerDrawable {
        val bottom = GradientDrawable().apply { cornerRadius = radius; setColor(edge) }
        val face = GradientDrawable().apply {
            cornerRadius = radius; setColor(top)
            if (stroke != null) setStroke(dp(2), stroke)
        }
        return LayerDrawable(arrayOf(bottom, face)).apply {
            setLayerInset(1, 0, 0, 0, dp(5))
        }
    }

    private fun chunkyPressed(radius: Float, top: Int): GradientDrawable =
        GradientDrawable().apply { cornerRadius = radius; setColor(top) }

    /** Flat solid fill (progress bars, pills). */
    private fun accentDrawable(radius: Float, top: Int, bottom: Int = top): GradientDrawable =
        GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(top, bottom))
            .apply { cornerRadius = radius }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun reduceMotion(): Boolean =
        Settings.Global.getFloat(contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f

    private fun onTabClicked(index: Int) {
        // v0.9.0 (user report): tabs must work even mid-session. The session is
        // persisted after every step, so leaving it via a tab keeps it
        // resumable from Home. Tab taps never discard a session implicitly —
        // it is cleared only by explicit actions (the result screen's home
        // button, or Home's "やめて、今日の5問をはじめる").
        showScreen(index)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        when {
            inQuestionPhase -> showScreen(0)
            store.hasSession() -> { store.clearSession(); showScreen(0) }
            else -> super.onBackPressed()
        }
    }

    private fun showScreen(index: Int) {
        inQuestionPhase = false
        tabButtons.forEachIndexed { i, b ->
            val selected = i == index
            b.setTextColor(if (selected) pal.tabSelText else grey)
            b.setCompoundDrawablesWithIntrinsicBounds(
                TabIconDrawable(i, if (selected) pal.tabSelText else grey), null, null, null)
            b.typeface = if (selected) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            b.background = if (selected) GradientDrawable().apply {
                cornerRadius = dp(20).toFloat(); setColor(pal.tabSelBg)
            } else null
        }
        val screen = when (index) {
            1 -> buildReview()
            2 -> buildCategories()
            else -> buildHome()
        }
        content.removeAllViews()
        content.addView(screen)
    }

    private fun setContent(view: View) {
        content.removeAllViews()
        content.addView(view)
    }

    // ---------- Level bar: grey track, flat vivid fill, thick and rounded ----------
    private inner class LevelBar(
        // Gold is the progress fill on every screen (ART_DIRECTION v2.1;
        // UX3-04: the question/result bars used to fall back to midnight).
        fillTop: Int = pal.yellow,
        fillBottom: Int = pal.yellow,
        trackColor: Int = pal.track
    ) {
        val root = FrameLayout(this@MainActivity).apply {
            background = GradientDrawable().apply {
                cornerRadius = dp(10).toFloat(); setColor(trackColor)
            }
        }
        private val fill = View(this@MainActivity).apply {
            background = accentDrawable(dp(10).toFloat(), fillTop, fillBottom)
        }
        init {
            root.addView(fill, FrameLayout.LayoutParams(0, dp(18)))
            val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(18))
            lp.topMargin = dp(8); lp.bottomMargin = dp(8)
            root.layoutParams = lp
        }
        fun describe(text: String) { root.contentDescription = text }
        fun set(progress: Int) {
            val p = progress.coerceIn(0, 100)
            root.post {
                fill.layoutParams = FrameLayout.LayoutParams(root.width * p / 100, dp(18))
            }
        }
        fun animate(from: Int, to: Int) {
            if (reduceMotion() || from == to) { set(to); return }
            root.post {
                val anim = ValueAnimator.ofInt(from.coerceIn(0, 100), to.coerceIn(0, 100))
                anim.duration = 450
                anim.addUpdateListener { a ->
                    val p = a.animatedValue as Int
                    fill.layoutParams = FrameLayout.LayoutParams(root.width * p / 100, dp(18))
                }
                anim.start()
            }
        }
    }

    private fun approxQuestionsLeft(xp: Int): Int =
        (LevelSystem.xpToNextLevel(xp, store.levelFloor) + LevelSystem.XP_NEW_CORRECT - 1) / LevelSystem.XP_NEW_CORRECT

    /** Current level with the never-decrease floor (design v1.3 §3.5). */
    private fun currentLevel(xp: Int = store.xp): Int {
        val lv = LevelSystem.levelForXp(xp, store.levelFloor)
        if (lv > store.levelFloor) store.levelFloor = lv
        return lv
    }

    // ---------- Home: the level is the star, on a green unit-header card ----------
    private fun buildHome(): View {
        val xp = store.xp
        val level = currentLevel(xp)
        val today = ProgressStore.today()
        val doneToday = store.lastDailyDay == today
        val saved = store.loadSession()
        val resumable = saved != null && saved.index < saved.items.size

        val col = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(20), dp(24), dp(20), dp(20))
        }
        // Unit-header style: a flat green card, white type, yellow progress fill.
        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(24), dp(24), dp(24), dp(20))
            background = accentDrawable(dp(24).toFloat(), pal.green)
        }
        // Mascot + speech bubble (v0.5.0): the owl cheers you on, one line a day.
        // v0.18.0: the first days get a welcome (案7), and a returning player
        // gets an おかえり before the daily rotation (案3) — coming back is
        // the event worth celebrating, never a debt to mention.
        val gapDays = if (store.lastDailyDay >= 0) today - store.lastDailyDay else 0L
        val cheer = when {
            store.sessionsDone == 0 -> "はじめまして！まずは気楽に5問、いってみよう"
            gapDays >= 2 -> "おかえり！久しぶりだね。無理なくいこう"
            store.sessionsDone in 1..6 -> listOf(
                "よく来たね！今日も気楽にいこう", "はじめの1週間は、楽しむのが仕事だよ",
                "間違えても大丈夫。段が1つ下がるだけ")[(today % 3).toInt()]
            else -> listOf(
                "今日も5問、いってみよう！", "昨日の自分より、ちょっと大人に",
                "「知らないと恥ずかしい」を1つ減らそう", "スキマ時間でレベルアップ！",
                "無理なく、楽しくいこう")[(today % 5).toInt()]
        }
        // Growth (v0.14.0): the owl's form follows the level; from the
        // branch level on, tapping it toggles the evolution-path choice.
        // v0.16.0: the owl lives in the bookshelf scene at the panel's foot
        // (the shelf is its background); the header keeps only its bubble.
        // The app-name line retired here (minimal pass: the user knows
        // which app they opened; it carried no decision).
        val owlStage = OwlGrowth.stageForLevel(level)
        val owlPath = store.evolutionPath
        var evoCard: View? = null
        val bubble = label(cheer, sizeBody, pal.greenDark, bold = true).apply {
            background = GradientDrawable().apply {
                cornerRadius = dp(16).toFloat(); setColor(Color.WHITE)
            }
            setPadding(dp(14), dp(10), dp(14), dp(10))
        }
        panel.addView(bubble)
        panel.addView(label("Lv. $level", sizeHero, Color.WHITE, bold = true))
        panel.addView(label(LevelSystem.titleForLevel(level), sizeHead, Color.WHITE, bold = true))
        val bar = LevelBar(fillTop = pal.yellow, fillBottom = pal.yellow, trackColor = pal.greenDark)
        bar.describe("レベル$level。次のレベルまであと約${approxQuestionsLeft(xp)}問")
        bar.set(LevelSystem.progressInLevel(xp, store.levelFloor))
        panel.addView(bar.root)
        if (level >= LevelSystem.MAX_LEVEL) {
            panel.addView(label("オトナの達人に到達！", sizeBody, pal.yellow, bold = true))
        } else {
            panel.addView(label("あと正解 約 ${approxQuestionsLeft(xp)} 問 でレベルアップ", sizeBody, Color.WHITE))
        }
        val nextOwl = OwlGrowth.nextStageLevel(level)
        panel.addView(label(when {
            owlStage >= 4 -> "フクロウ：${OwlGrowth.stageName(owlStage, owlPath)}（最終形）"
            OwlGrowth.canBranch(level) && owlPath == OwlGrowth.PATH_NONE ->
                "フクロウ：${OwlGrowth.stageName(owlStage, owlPath)}（タップで進化の方向を選べます）"
            nextOwl != null -> "フクロウ：${OwlGrowth.stageName(owlStage, owlPath)}（Lv. $nextOwl で進化）"
            else -> "フクロウ：${OwlGrowth.stageName(owlStage, owlPath)}"
        }, sizeSmall, Color.WHITE))

        // Bookshelf scene (v0.16.0): the shelf is the owl's background AND
        // the progress display — five shelves inside the panel, books fill
        // with each genre's graduation ratio, and the owl stands on the
        // bottom shelf. The labelled card version retired here (minimal
        // pass: same data, shown once, as a picture); exact numbers live
        // on the Categories tab, the summary in the content description.
        val statesH = store.allStates()
        val shelfSummary = StringBuilder("フクロウの本棚。カテゴリ別の卒業割合。")
        val booksArr = IntArray(Questions.categories.size)
        val completeArr = BooleanArray(Questions.categories.size)
        val colorArr = IntArray(Questions.categories.size)
        Questions.categories.forEachIndexed { idx, cat ->
            val qs = Questions.all.filter { it.category == cat }
            val grad = qs.count { q -> statesH[q.id]?.let { ReviewLogic.isGraduated(it) } == true }
            val rate = if (qs.isEmpty()) 0 else grad * 100 / qs.size
            shelfSummary.append("${cat.substringBefore("・")} ${rate}パーセント。")
            booksArr[idx] = Bookshelf.booksFor(grad, qs.size)
            completeArr[idx] = Bookshelf.isComplete(grad, qs.size)
            colorArr[idx] = pal.categoryColors[idx % pal.categoryColors.size]
        }
        val darkNow = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES
        val scene = FrameLayout(this)
        val shelfScene = ShelfSceneView(this, booksArr, completeArr, colorArr, darkNow)
        shelfScene.contentDescription = shelfSummary.toString()
        scene.addView(shelfScene, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        val sceneOwl = MascotView(this, owlStage, owlPath, level)
        sceneOwl.contentDescription = if (OwlGrowth.canBranch(level))
            "フクロウ。現在の姿は${OwlGrowth.stageName(owlStage, owlPath)}。タップで進化の方向を選べます"
        else "フクロウ。現在の姿は${OwlGrowth.stageName(owlStage, owlPath)}"
        if (OwlGrowth.canBranch(level)) {
            sceneOwl.isClickable = true
            sceneOwl.setOnClickListener {
                evoCard?.let { it.visibility = if (it.visibility == View.VISIBLE) View.GONE else View.VISIBLE }
            }
        }
        val owlLp = FrameLayout.LayoutParams(dp(76), dp(86))
        owlLp.gravity = Gravity.BOTTOM or Gravity.RIGHT
        owlLp.rightMargin = dp(14)
        owlLp.bottomMargin = dp(3)
        scene.addView(sceneOwl, owlLp)
        val sceneLp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(184))
        sceneLp.topMargin = dp(14)
        panel.addView(scene, sceneLp)
        col.addView(panel)

        // Seasonal card (v0.15.0): shown only while a calendar event is
        // active — the calendar supplies the reason to open the app, and
        // outside the window the home is exactly as before. Facts only
        // (docs/季節の常識_設計案.md: 盛らない・煽らない).
        Seasonal.activeOn(LocalDate.ofEpochDay(today))?.let { (ev, window) ->
            val seasonCard = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(16), dp(14), dp(16), dp(14))
                background = cardDrawable(dp(20).toFloat())
            }
            val seasonHead = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }
            seasonHead.addView(label("今日は何の日", sizeHead, pal.textMain, bold = true))
            val seasonChip = label("${ev.name} 〜${window.second.monthValue}/${window.second.dayOfMonth}",
                sizeSmall, Color.WHITE, bold = true).apply {
                background = accentDrawable(dp(12).toFloat(), seasonAccent)
                setPadding(dp(10), dp(3), dp(10), dp(3))
            }
            val chipLp = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            chipLp.leftMargin = dp(10)
            seasonHead.addView(seasonChip, chipLp)
            seasonCard.addView(seasonHead)
            seasonCard.addView(label(ev.note, sizeBody, pal.textMain))
            seasonCard.addView(label("今日の5問に 季節の1問 入り", sizeSmall, seasonAccent, bold = true))
            val slp = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            slp.topMargin = dp(16)
            col.addView(seasonCard, slp)
        }

        if (resumable && saved != null) {
            val resume = primaryButton("つづきから（${saved.index + 1}問目）") { resumeSavedSession() }
            val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(56))
            lp.topMargin = dp(24)
            col.addView(resume, lp)
            val fresh = Button(this).apply {
                text = "やめて、今日の5問をはじめる"
                textSize = sizeBody
                setTextColor(grey)
                background = null
                setOnClickListener { store.clearSession(); startSession(0) }
            }
            col.addView(fresh)
        } else {
            val start = primaryButton(if (doneToday) "おかわり5問" else "今日の5問をはじめる") { startSession(0) }
            val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(56))
            lp.topMargin = dp(24)
            col.addView(start, lp)
            if (doneToday) col.addView(label("今日の分は完了しています。おかわりは自由です", sizeSmall, grey))
            // v0.18.0 (design review 案4): a light 3-question set for low-
            // energy days — a small door is better than a closed one.
            val light = Button(this).apply {
                text = if (doneToday) "かるく おかわり3問" else "かるく3問で はじめる"
                textSize = sizeBody
                setTextColor(grey)
                background = null
                setOnClickListener { startSession(0, size = 3) }
            }
            col.addView(light)
        }
        // Evolution-path choice (v0.14.0): appears at the branch level; it
        // stays visible until a path is picked, then the mascot tap toggles
        // it so the user can change their mind.
        if (OwlGrowth.canBranch(level)) {
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(16), dp(14), dp(16), dp(14))
                background = cardDrawable(dp(20).toFloat())
            }
            card.addView(label(
                if (owlPath == OwlGrowth.PATH_NONE) "フクロウが進化のとき！ どっちに育てる？" else "進化の方向",
                sizeHead, pal.textMain, bold = true))
            val options = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            val previewStage = maxOf(3, owlStage)
            listOf(
                OwlGrowth.PATH_CUTE to "かわいく",
                OwlGrowth.PATH_COOL to "かっこよく",
            ).forEach { (pathOpt, name) ->
                val selected = owlPath == pathOpt
                val opt = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.CENTER
                    setPadding(dp(8), dp(10), dp(8), dp(10))
                    background = if (selected) accentDrawable(dp(16).toFloat(), pal.yellow) else cardDrawable(dp(16).toFloat())
                    isClickable = true
                    contentDescription = if (selected) "$name 進化（選択中）" else "$name 進化にする"
                    setOnClickListener { store.evolutionPath = pathOpt; showScreen(0) }
                }
                opt.addView(MascotView(this, previewStage, pathOpt, level), LinearLayout.LayoutParams(dp(64), dp(72)))
                opt.addView(label(if (selected) "$name（選択中）" else name, sizeBody, pal.textMain, bold = true))
                val lp = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                if (pathOpt == OwlGrowth.PATH_CUTE) lp.rightMargin = dp(10)
                options.addView(opt, lp)
            }
            card.addView(options)
            card.visibility = if (owlPath == OwlGrowth.PATH_NONE) View.VISIBLE else View.GONE
            val clp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            clp.topMargin = dp(16)
            col.addView(card, clp)
            evoCard = card
        }
        return ScrollView(this).apply { addView(col) }
    }

    private fun primaryButton(text: String, onClick: () -> Unit): Button = Button(this).apply {
        this.text = text
        textSize = sizeBody
        typeface = Typeface.DEFAULT_BOLD
        setTextColor(pal.greenTextOn)
        // Chunky green button; pressed = the edge drops away (pushed in).
        background = StateListDrawable().apply {
            addState(intArrayOf(android.R.attr.state_pressed), chunkyPressed(dp(16).toFloat(), pal.greenDark))
            addState(intArrayOf(), chunkyDrawable(dp(16).toFloat(), pal.green, pal.greenDark))
        }
        setOnClickListener { onClick() }
    }

    /** Secondary action: white chunky button with a grey edge (Duolingo style). */
    private fun secondaryButton(text: String, onClick: () -> Unit): Button = Button(this).apply {
        this.text = text
        textSize = sizeBody
        typeface = Typeface.DEFAULT_BOLD
        setTextColor(navy)
        background = StateListDrawable().apply {
            addState(intArrayOf(android.R.attr.state_pressed), chunkyPressed(dp(16).toFloat(), pal.tabSelBg))
            addState(intArrayOf(), chunkyDrawable(dp(16).toFloat(), pal.surface, pal.border, stroke = pal.border))
        }
        setOnClickListener { onClick() }
    }

    /** Choice/judge button: white chunky card style, blue tint when pressed. */
    private fun glassButton(text: String, onClick: () -> Unit): Button = Button(this).apply {
        this.text = text
        textSize = sizeBody
        setTextColor(navy)
        val normal = chunkyDrawable(dp(16).toFloat(), pal.surface, pal.border, stroke = pal.border)
        val pressed = chunkyPressed(dp(16).toFloat(), pal.tabSelBg)
        background = StateListDrawable().apply {
            addState(intArrayOf(android.R.attr.state_pressed), pressed)
            addState(intArrayOf(), normal)
        }
        setOnClickListener { onClick() }
    }

    // ---------- Session ----------
    private fun buildItems(questions: List<Question>): List<SItem> = questions.map { q ->
        if (q.type == QType.CARD) {
            SItem(q, emptyList(), q.choices, -1)
        } else {
            val order = q.choices.indices.shuffled(Random)
            SItem(q, order, order.map { q.choices[it] }, order.indexOf(q.answerIndex))
        }
    }

    /** Graduated count per category right now (Questions.categories order). */
    private fun currentGradsSnapshot(): List<Int> {
        val states = store.allStates()
        return Questions.categories.map { cat ->
            Questions.all.count { q -> q.category == cat && states[q.id]?.let { ReviewLogic.isGraduated(it) } == true }
        }
    }

    private fun persistSession() {
        store.saveSession(
            ProgressStore.SavedSession(
                sItems.map { ProgressStore.SavedItem(it.q.id, it.order) },
                sIndex, sCorrect, sXp, sStreak, sStartXp, sMode, sAnswered, sChosen, sCounted,
                sStartGrads, sResults.joinToString("") { if (it) "1" else "0" }
            )
        )
    }

    private fun restoreSession(saved: ProgressStore.SavedSession) {
        sItems = saved.items.mapNotNull { item ->
            val q = Questions.byId(item.qid) ?: return@mapNotNull null
            if (q.type == QType.CARD || item.order.size != q.choices.size) {
                SItem(q, emptyList(), q.choices, q.answerIndex)
            } else {
                SItem(q, item.order, item.order.map { q.choices[it] }, item.order.indexOf(q.answerIndex))
            }
        }
        sIndex = saved.index; sCorrect = saved.correct; sXp = saved.xp
        sStreak = saved.streak; sStartXp = saved.startXp; sMode = saved.mode
        sAnswered = saved.answered; sChosen = saved.chosen; sCounted = saved.counted
        // Saves from before v0.17.0 have no snapshot: fall back to "now", so
        // the growth section simply reports no delta for a resumed session.
        sStartGrads = if (saved.startGrads.size == Questions.categories.size) saved.startGrads
            else currentGradsSnapshot()
        sResults = BooleanArray(sItems.size) { i -> saved.results.getOrNull(i) == '1' }
    }

    private fun resumeSavedSession() {
        val saved = store.loadSession() ?: return
        restoreSession(saved)
        when {
            sItems.isEmpty() -> { store.clearSession(); showScreen(0) }
            sIndex < sItems.size -> showQuestion()
            else -> showResult()
        }
    }

    private fun startSession(mode: Int, category: String? = null, size: Int = 5) {
        val today = ProgressStore.today()
        val states = store.allStates()
        // v0.18.0 (design review 案3): a player returning after a gap gets
        // one extra review slot so the backlog melts a little faster — and
        // with review XP now equal to new XP (案2), coming back never pays
        // less than staying away.
        val returning = mode == 0 && store.lastDailyDay >= 0 && today - store.lastDailyDay >= 2
        val picked: List<Question> = when (mode) {
            1 -> Questions.pool.filter { q -> states[q.id]?.let { ReviewLogic.isWeak(it) } == true }
                .shuffled().take(5)
            2 -> Questions.pool.filter { q ->
                // Spot checks count as today's review too (v0.9.0 audit: the
                // review screen and this session used to ignore them while the
                // daily set included them).
                states[q.id]?.let { ReviewLogic.isDue(it, today) || ReviewLogic.isSpotCheckDue(it, today) } == true
            }.sortedBy { states[it.id]?.dueDay ?: 0L }.take(5)
            3 -> {
                // Genre challenge: only this category. Due first, then new,
                // then seen-not-graduated; graduated fill only if still short
                // (with the 20-question sample a genre has 4 questions).
                val qs = Questions.all.filter { it.category == category }
                val due = qs.filter { q ->
                    states[q.id]?.let { ReviewLogic.isDue(it, today) || ReviewLogic.isSpotCheckDue(it, today) } == true
                }.shuffled()
                val fresh = qs.filter { it.id !in due.map { p -> p.id } && states[it.id] == null }.shuffled()
                val rest = qs.filter { q ->
                    q.id !in due.map { p -> p.id } && q.id !in fresh.map { p -> p.id } &&
                        states[q.id]?.let { ReviewLogic.isGraduated(it) } != true
                }.shuffled()
                val grads = qs.filter { q ->
                    q.id !in due.map { p -> p.id } && q.id !in fresh.map { p -> p.id } &&
                        q.id !in rest.map { p -> p.id }
                }.shuffled()
                (due + fresh + rest + grads).take(5)
            }
            else -> {
                val list = mutableListOf<Question>()
                // Review slots: regular due first-class, plus graduated spot
                // checks whose day has come (IR2-2: 抜き打ち再登場).
                val due = Questions.pool.filter { q ->
                    states[q.id]?.let { ReviewLogic.isDue(it, today) || ReviewLogic.isSpotCheckDue(it, today) } == true
                }.shuffled()
                val duePicked = due.take(minOf(if (returning) 3 else 2, size - 1))
                list += duePicked
                // New slots follow the level band's difficulty mix (design
                // v1.3 §3.5 ①); the fill-up below stays difficulty-blind.
                val freshPool = Questions.all.filter { it.id !in list.map { p -> p.id } && states[it.id] == null }
                list += SessionDesign.pickNew(freshPool, size - list.size, currentLevel(),
                    sessionsDone = store.sessionsDone)
                if (list.size < size) {
                    // Fill-up excludes graduated questions (IR2-2): graduation
                    // means leaving the pool, not just the review list.
                    val rest = Questions.all.filter { q ->
                        q.id !in list.map { p -> p.id } &&
                            states[q.id]?.let { ReviewLogic.isGraduated(it) } != true
                    }.shuffled()
                    list += rest.take(size - list.size)
                }
                if (list.none { it.type == QType.CARD }) {
                    val card = Questions.all.firstOrNull { q ->
                        q.type == QType.CARD && q.id !in list.map { p -> p.id } &&
                            states[q.id]?.let { ReviewLogic.isGraduated(it) } != true
                    } ?: Questions.all.firstOrNull { it.type == QType.CARD && it.id !in list.map { p -> p.id } }
                    if (card != null) {
                        // v0.9.0 audit: a short set used to end up with no card
                        // at all; append instead of only swapping the last slot
                        // (the wave ordering below decides the final position).
                        if (list.size >= size) list[list.size - 1] = card else list += card
                    }
                }
                // Seasonal slot (v0.15.0): while a calendar event is active,
                // one new slot becomes a seasonal question — the calendar
                // supplies the reason to learn it this week. Placed after the
                // card guarantee so the swap never evicts the set's card.
                Seasonal.activeOn(LocalDate.ofEpochDay(today))?.let { (ev, _) ->
                    val inList = list.map { it.id }.toSet()
                    val candidates = ev.questionIds.mapNotNull { Questions.byId(it) }
                        .filter { it.id !in inList }
                    val pick = candidates.firstOrNull { states[it.id] == null }
                        ?: candidates.firstOrNull {
                            states[it.id]?.let { s -> ReviewLogic.isGraduated(s) } != true
                        }
                    if (pick != null) {
                        val dueIds0 = duePicked.map { it.id }.toSet()
                        val idx = list.indices.lastOrNull { i ->
                            list[i].id !in dueIds0 && list[i].type != QType.CARD
                        }
                        if (idx != null) list[idx] = pick else if (list.size < size) list += pick
                    }
                }
                if (list.isEmpty()) {
                    // Everything graduated and nothing due: still let the user
                    // play a practice set instead of bouncing to another tab.
                    list += Questions.all.shuffled().take(size)
                }
                // Wave ordering (design v1.3 §3.5 ②): opener -> new -> peak at
                // position 4 -> closer, instead of a plain shuffle.
                val dueIds = duePicked.map { it.id }.toSet()
                val reviewPart = list.filter { it.id in dueIds }
                val freshPart = list.filter { it.id !in dueIds }
                SessionDesign.orderWave(reviewPart, freshPart).take(5)
            }
        }
        if (picked.isEmpty()) { showScreen(if (mode == 3) 2 else 1); return }
        sItems = buildItems(picked)
        sIndex = 0; sCorrect = 0; sXp = 0; sStreak = 0
        sStartXp = store.xp; sMode = mode; sAnswered = false; sChosen = -1; sCounted = false
        sStartGrads = currentGradsSnapshot()
        sResults = BooleanArray(sItems.size)
        persistSession()
        showQuestion()
    }

    private fun showQuestion() {
        inQuestionPhase = true
        // Tabs stay live during a session (v0.9.0): the session is persisted,
        // so the user can leave and resume it from Home at any point.
        val item = sItems[sIndex]
        val q = item.q
        val xpNow = store.xp
        val levelNow = currentLevel(xpNow)

        val rootCol = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(16), dp(20), dp(12))
        }
        // Header: progress text + the category as a GLASS pill (v0.3.2: surfaces
        // are not painted; the category colour appears only as a small dot).
        val headerRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        headerRow.addView(label("${sIndex + 1} / ${sItems.size}", sizeSmall, grey, bold = true))
        val catIdx = Questions.categories.indexOf(q.category).coerceAtLeast(0)
        val catColor = pal.categoryColors[catIdx % pal.categoryColors.size]
        val catPill = label(q.category, sizeSmall, Color.WHITE, bold = true).apply {
            background = accentDrawable(dp(12).toFloat(), catColor)
            setPadding(dp(10), dp(3), dp(10), dp(3))
        }
        val pillLp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        pillLp.leftMargin = dp(8)
        headerRow.addView(catPill, pillLp)
        // Seasonal tag (v0.15.0): this question arrived via the calendar slot.
        if (Seasonal.eventOf(q.id) != null) {
            val seasonPill = label("季節", sizeSmall, Color.WHITE, bold = true).apply {
                background = accentDrawable(dp(12).toFloat(), seasonAccent)
                setPadding(dp(10), dp(3), dp(10), dp(3))
            }
            val sLp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            sLp.leftMargin = dp(6)
            headerRow.addView(seasonPill, sLp)
        }
        // UX3-03: the category is stated once, by the pill. The pictogram
        // disc that used to sit next to it only repeated the same category;
        // the pictograms live on as the canonical icons on the Categories tab.
        rootCol.addView(headerRow)

        val barRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        barRow.addView(label("Lv. $levelNow", sizeBody, navy, bold = true))
        val miniBar = LevelBar()
        miniBar.describe("レベル$levelNow。次のレベルまであと約${approxQuestionsLeft(xpNow)}問")
        miniBar.set(LevelSystem.progressInLevel(xpNow, store.levelFloor))
        val barLp = LinearLayout.LayoutParams(0, dp(18), 1f)
        barLp.leftMargin = dp(12)
        barRow.addView(miniBar.root, barLp)
        rootCol.addView(barRow)

        val scrollCol = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(8), 0, dp(8))
        }
        if (q.type != QType.CARD && q.figure.isNotEmpty()) {
            // Figure question (v0.7.0): the sign is drawn in code above the prompt.
            // UX3-02: card questions carry their prompt (and figure) on the
            // card face itself, so the header copy is quiz-only.
            val figRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
            }
            figRow.addView(SignView(this, q.figure), LinearLayout.LayoutParams(dp(118), dp(118)))
            val figLp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            figLp.bottomMargin = dp(6)
            scrollCol.addView(figRow, figLp)
        }
        if (q.type != QType.CARD) {
            scrollCol.addView(label(q.prompt, sizeHead, navy, bold = true))
        }

        val feedback = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            visibility = View.GONE
        }
        val nextBtn = primaryButton(if (sIndex + 1 < sItems.size) "次へ" else "結果を見る") { goNext() }
        nextBtn.visibility = View.GONE
        val nextLp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(52))
        nextLp.topMargin = dp(8)

        if (q.type == QType.CARD) {
            buildCard(item, scrollCol, feedback, nextBtn, miniBar)
        } else {
            buildQuiz(item, scrollCol, feedback, nextBtn, miniBar)
        }
        scrollCol.addView(feedback)
        rootCol.addView(ScrollView(this).apply { addView(scrollCol) },
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        rootCol.addView(nextBtn, nextLp)
        setContent(rootCol)

        if (sAnswered) renderAnswered(item, feedback, nextBtn, scrollCol, animateBar = false)
    }

    private fun buildQuiz(item: SItem, col: LinearLayout, feedback: LinearLayout, nextBtn: Button, miniBar: LevelBar) {
        val buttons = mutableListOf<Button>()
        item.choices.forEachIndexed { i, choice ->
            val b = glassButton(choice) { onQuizAnswered(item, i, buttons, feedback, nextBtn, miniBar) }
            b.gravity = Gravity.CENTER_VERTICAL
            buttons += b
            val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            lp.topMargin = dp(12)
            col.addView(b, lp)
        }
        col.tag = buttons
    }

    private fun buildCard(item: SItem, col: LinearLayout, feedback: LinearLayout, nextBtn: Button, miniBar: LevelBar) {
        val q = item.q
        col.addView(label("まず頭の中で答えてから、めくってください", sizeSmall, grey))
        val cardBox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(16), dp(24), dp(16), dp(24))
            background = cardDrawable(dp(24).toFloat())
        }
        if (q.figure.isNotEmpty()) {
            val fig = SignView(this, q.figure)
            val figLp2 = LinearLayout.LayoutParams(dp(104), dp(104))
            figLp2.bottomMargin = dp(10)
            cardBox.addView(fig, figLp2)
        }
        val frontText = label(q.prompt, sizeHead, navy, bold = true).apply { gravity = Gravity.CENTER }
        val backText = label(q.cardBack, sizeHead, navy, bold = true).apply {
            gravity = Gravity.CENTER; visibility = View.GONE
        }
        cardBox.addView(frontText); cardBox.addView(backText)
        val cardLp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        cardLp.topMargin = dp(16)
        col.addView(cardBox, cardLp)

        val judgeRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            visibility = View.GONE
        }
        val notYet = glassButton("まだだった") { }
        val remembered = glassButton("思い出せた") { }
        notYet.setOnClickListener { onCardJudged(item, false, listOf(notYet, remembered), feedback, nextBtn, miniBar) }
        remembered.setOnClickListener { onCardJudged(item, true, listOf(notYet, remembered), feedback, nextBtn, miniBar) }
        judgeRow.addView(notYet, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        val rLp = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        rLp.leftMargin = dp(12)
        judgeRow.addView(remembered, rLp)
        val jLp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        jLp.topMargin = dp(12)
        col.addView(judgeRow, jLp)
        col.tag = listOf(cardBox, frontText, backText, judgeRow, notYet, remembered)

        lateinit var flip: Button
        flip = primaryButton("カードをめくる") {
            val showBack = {
                frontText.visibility = View.GONE
                backText.visibility = View.VISIBLE
                judgeRow.visibility = View.VISIBLE
            }
            if (reduceMotion()) {
                showBack()
            } else {
                cardBox.animate().rotationY(90f).setDuration(140).withEndAction {
                    showBack()
                    cardBox.rotationY = -90f
                    cardBox.animate().rotationY(0f).setDuration(140).start()
                }.start()
            }
            flip.visibility = View.GONE
        }
        val fLp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(52))
        fLp.topMargin = dp(16)
        col.addView(flip, fLp)
        if (sAnswered) {
            flip.visibility = View.GONE
            frontText.visibility = View.GONE
            backText.visibility = View.VISIBLE
            judgeRow.visibility = View.VISIBLE
            listOf(notYet, remembered).forEach { it.isEnabled = false; it.alpha = 0.5f }
        }
    }

    private fun onQuizAnswered(item: SItem, chosen: Int, buttons: List<Button>, feedback: LinearLayout, nextBtn: Button, miniBar: LevelBar) {
        if (sAnswered) return
        sAnswered = true
        sChosen = chosen
        val xpBefore = store.xp
        applyAnswer(item.q, chosen == item.answerIndex)
        persistSession()
        renderAnswered(item, feedback, nextBtn, null, animateBar = true, xpBefore = xpBefore, buttons = buttons, miniBar = miniBar)
    }

    private fun onCardJudged(item: SItem, remembered: Boolean, judgeButtons: List<Button>, feedback: LinearLayout, nextBtn: Button, miniBar: LevelBar) {
        if (sAnswered) return
        sAnswered = true
        sChosen = if (remembered) 1 else 0
        val xpBefore = store.xp
        applyAnswer(item.q, remembered)
        persistSession()
        judgeButtons.forEach { it.isEnabled = false; it.alpha = 0.5f }
        renderAnswered(item, feedback, nextBtn, null, animateBar = true, xpBefore = xpBefore, buttons = judgeButtons, miniBar = miniBar)
    }

    private fun renderAnswered(
        item: SItem, feedback: LinearLayout, nextBtn: Button, scrollCol: LinearLayout?,
        animateBar: Boolean, xpBefore: Int = store.xp, buttons: List<Button>? = null, miniBar: LevelBar? = null
    ) {
        val q = item.q
        val correct = if (q.type == QType.CARD) sChosen == 1 else sChosen == item.answerIndex
        // Celebration (v0.13.0): correct answers feel good — a bounce on the
        // winning button, a haptic tick and a soft chime; a level-up earns
        // the fanfare. Wrong answers get none of it (never a punishment).
        if (correct && animateBar) {
            val target = if (q.type == QType.CARD) buttons?.getOrNull(1) else buttons?.getOrNull(item.answerIndex)
            if (target != null) bounce(target)
            Haptics.tick(this)
            if (currentLevel(store.xp) > currentLevel(xpBefore)) {
                Haptics.fanfare(this)
                Chime.levelUp()
            } else {
                Chime.correct()
            }
        }
        if (q.type != QType.CARD) {
            val btns = buttons ?: (scrollCol?.tag as? List<*>)?.filterIsInstance<Button>() ?: emptyList()
            btns.forEachIndexed { i, b ->
                b.isEnabled = false
                if (i == item.answerIndex) {
                    b.text = "○ ${item.choices[i]}"
                    b.setTextColor(pal.correctText)
                    b.background = GradientDrawable().apply {
                        cornerRadius = dp(16).toFloat(); setColor(pal.correctBg)
                        setStroke(dp(2), pal.correctBorder)
                    }
                } else if (i == sChosen) {
                    b.text = "× ${item.choices[i]}"
                    b.setTextColor(pal.wrongText)
                    b.background = GradientDrawable().apply {
                        cornerRadius = dp(16).toFloat(); setColor(pal.wrongBgC)
                        setStroke(dp(2), pal.wrongBorder)
                    }
                } else {
                    b.alpha = 0.55f
                }
            }
        }
        if (miniBar != null) {
            val levelBefore = currentLevel(xpBefore)
            val levelNow = currentLevel(store.xp)
            if (animateBar && correct) {
                if (levelNow > levelBefore) {
                    miniBar.animate(LevelSystem.progressInLevel(xpBefore, store.levelFloor), 100)
                    miniBar.root.postDelayed({ miniBar.set(LevelSystem.progressInLevel(store.xp, store.levelFloor)) }, 480)
                } else {
                    miniBar.animate(LevelSystem.progressInLevel(xpBefore, store.levelFloor), LevelSystem.progressInLevel(store.xp, store.levelFloor))
                }
            } else {
                miniBar.set(LevelSystem.progressInLevel(store.xp, store.levelFloor))
            }
            miniBar.describe("レベル$levelNow。次のレベルまであと約${approxQuestionsLeft(store.xp)}問")
        }
        showFeedback(item, correct, feedback, store.xp - xpBefore)
        nextBtn.visibility = View.VISIBLE
    }

    /** Small celebratory bounce (v0.13.0); skipped when motion is reduced. */
    private fun bounce(view: View) {
        if (reduceMotion()) return
        view.animate().scaleX(1.07f).scaleY(1.07f).setDuration(110)
            .withEndAction { view.animate().scaleX(1f).scaleY(1f).setDuration(170).start() }
            .start()
    }

    private fun applyAnswer(q: Question, correct: Boolean) {
        val today = ProgressStore.today()
        val before = store.stateFor(q.id)
        val isReview = before != null && before.box > 0
        val dueOrNew = before == null || ReviewLogic.isDue(before, today) ||
            ReviewLogic.isSpotCheckDue(before, today)
        // v0.18.0 (design review 案1): missing a graduated question — a spot
        // check, or one already awaiting confirmation — never revokes the
        // graduation. It only asks for a confirmation a few days later, so
        // the bookshelf never loses a book.
        val spotMiss = !correct && before != null &&
            (ReviewLogic.isGraduated(before) || store.isConfirm(q.id))
        if (spotMiss) {
            store.saveState(q.id, ReviewLogic.onSpotCheckMiss(before, today))
            store.setConfirm(q.id, true)
        } else if (!correct || dueOrNew) {
            store.saveState(q.id, ReviewLogic.onAnswer(before ?: ReviewLogic.initial(today), correct, today))
            if (correct && store.isConfirm(q.id)) store.setConfirm(q.id, false)
        }
        store.totalAnswered += 1
        if (sIndex in sResults.indices) sResults[sIndex] = correct
        if (correct) {
            store.totalCorrect += 1
            sCorrect += 1
            // v0.9.0 audit fix: XP (and the streak bonus) is earned only when
            // the answer counts for learning — first attempt, due review, or
            // a spot check. Re-answering an already-known question off-schedule
            // (e.g. repeating genre challenges) is practice and earns no XP;
            // before this fix it could be farmed to inflate the level.
            if (dueOrNew) {
                sStreak += 1
                var gain = if (isReview) LevelSystem.XP_REVIEW_CORRECT else LevelSystem.XP_NEW_CORRECT
                if (sStreak % 3 == 0) gain += LevelSystem.XP_STREAK_BONUS
                // v0.18.0 (案7): beginner bonus — the first levels fly by,
                // so the first evolution arrives within a few days.
                if (currentLevel() < LevelSystem.BEGINNER_LEVEL) gain += LevelSystem.XP_BEGINNER_BONUS
                sXp += gain
                store.xp += gain
            }
        } else {
            sStreak = 0
        }
    }

    private fun showFeedback(item: SItem, correct: Boolean, feedback: LinearLayout, xpGain: Int = 0) {
        val q = item.q
        feedback.removeAllViews()
        feedback.visibility = View.VISIBLE
        // Duolingo-style feedback banner: green tint = correct, red tint = "not
        // quite" (informative colour, no shaming).
        feedback.background = GradientDrawable().apply {
            cornerRadius = dp(16).toFloat()
            setColor(if (correct) pal.correctBg else pal.wrongBgC)
        }
        feedback.setPadding(dp(16), dp(14), dp(16), dp(14))
        // The banner pops in (v0.13.0) — a tiny bit of theatre for the reveal.
        if (!reduceMotion()) {
            feedback.scaleX = 0.97f; feedback.scaleY = 0.97f; feedback.alpha = 0.5f
            feedback.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(180).start()
        }
        val answerText = if (q.type == QType.CARD) q.cardBack else item.choices[item.answerIndex]
        // Praise (v0.13.0): a correct answer is celebrated out loud, with the
        // phrase rotating and streaks getting their own line.
        val headline = when {
            correct && q.type == QType.CARD -> Praise.forCard(store.totalAnswered)
            correct -> "正解！ ${Praise.forCorrect(sStreak, store.totalAnswered)}"
            else -> "正解は「$answerText」"
        }
        feedback.addView(label(headline, sizeBody,
            if (correct) pal.correctText else pal.wrongText, bold = true))
        if (correct && xpGain > 0) {
            val xpLabel = label("+${xpGain} XP", sizeBody, pal.goldText, bold = true)
            feedback.addView(xpLabel)
            if (!reduceMotion()) {
                xpLabel.scaleX = 0.6f; xpLabel.scaleY = 0.6f
                xpLabel.animate().scaleX(1f).scaleY(1f).setDuration(240)
                    .setInterpolator(android.view.animation.OvershootInterpolator(2.5f)).start()
            }
        }
        feedback.addView(label(q.explanation, sizeBody, pal.textMain, bodyLeading = true))
        feedback.addView(label("場面：${q.scene}", sizeSmall, pal.feedbackSub))
        feedback.addView(label("出典：${q.source}", sizeSmall, pal.feedbackSub))
    }

    private fun goNext() {
        sIndex++
        sAnswered = false
        sChosen = -1
        persistSession()
        if (sIndex < sItems.size) showQuestion() else showResult()
    }

    // ---------- Result ----------
    /** A result-screen act: a small heading over a vertical stack. */
    private fun resultSection(heading: String): LinearLayout {
        val sec = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(0, dp(20), 0, 0)
        }
        sec.addView(label(heading, sizeSmall, grey, bold = true))
        return sec
    }

    /** Fade-and-rise reveal for the result acts; instant when motion is reduced. */
    private fun stageReveal(v: View, delayMs: Long) {
        if (reduceMotion()) return
        v.alpha = 0f
        v.translationY = dp(12).toFloat()
        v.animate().alpha(1f).translationY(0f).setStartDelay(delayMs).setDuration(320).start()
    }

    private fun genreGrowthRow(cat: String, idx: Int, correct: Int, total: Int, g: SessionGrowth.GenreGrowth): View {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(5), 0, dp(5))
        }
        val accent = pal.categoryColors[idx % pal.categoryColors.size]
        val dot = View(this).apply { background = circleDrawable(accent) }
        row.addView(dot, LinearLayout.LayoutParams(dp(12), dp(12)))
        val nameLp = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        nameLp.leftMargin = dp(10)
        row.addView(label(cat.substringBefore("・"), sizeBody, pal.textMain, bold = true), nameLp)
        row.addView(label("正解 $correct/$total", sizeSmall, grey))
        fun pill(text: String, bg: Int, fg: Int) {
            val p = label(text, sizeSmall, fg, bold = true).apply {
                background = accentDrawable(dp(10).toFloat(), bg)
                setPadding(dp(8), dp(2), dp(8), dp(2))
            }
            val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            lp.leftMargin = dp(8)
            row.addView(p, lp)
        }
        if (g.gradDelta > 0) pill("卒業 +${g.gradDelta}", pal.yellow, pal.yellowText)
        if (g.bookDelta > 0) pill("本 +${g.bookDelta}冊", pal.chipBg, pal.chipText)
        row.contentDescription = "$cat。正解 $correct/$total。" +
            (if (g.gradDelta > 0) "卒業が${g.gradDelta}問増えた。" else "") +
            (if (g.bookDelta > 0) "本棚に本が${g.bookDelta}冊増えた。" else "")
        return row
    }

    private fun showResult() {
        inQuestionPhase = false
        val justFinished = !sCounted
        if (!sCounted) {
            if (sMode == 0) {
                store.sessionsDone += 1
                store.lastDailyDay = ProgressStore.today()
            }
            sCounted = true
            persistSession()
        }
        val newLevel = currentLevel(store.xp)
        val startLevel = currentLevel(sStartXp)
        val leveledUp = newLevel > startLevel
        val col = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(20), dp(24), dp(20), dp(20))
        }
        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(24), dp(24), dp(24), dp(20))
            background = cardDrawable(dp(28).toFloat())
        }
        // ---- Act 1: the score (the celebration as it was) ----
        val confetti = ConfettiView(this, seed = sStartXp + sCorrect * 31, count = Praise.confettiPieces(sCorrect))
        val confettiLp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(52))
        confettiLp.bottomMargin = dp(4)
        panel.addView(confetti, confettiLp)
        panel.addView(label("${sItems.size}問中 ${sCorrect}問 正解", sizeHead, navy, bold = true))
        val starRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        for (i in sItems.indices) {
            val star = StarView(this, if (i < sCorrect) pal.yellow else pal.track, i < sCorrect)
            val sLp = LinearLayout.LayoutParams(dp(30), dp(30))
            sLp.leftMargin = dp(2); sLp.rightMargin = dp(2)
            starRow.addView(star, sLp)
            if (i < sCorrect && !reduceMotion()) {
                star.scaleX = 0f; star.scaleY = 0f
                star.animate().scaleX(1f).scaleY(1f).setStartDelay(i * 90L).setDuration(260)
                    .setInterpolator(android.view.animation.OvershootInterpolator(2.5f)).start()
            }
        }
        val starLp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        starLp.topMargin = dp(6); starLp.bottomMargin = dp(2)
        panel.addView(starRow, starLp)
        panel.addView(label(Praise.resultLine(sCorrect, sItems.size), sizeBody, pal.goldText, bold = true))
        // Fanfare for the big finishes (v0.13.0): a level-up or a perfect
        // set, once, when the set has just been completed.
        if (justFinished && (leveledUp || (sItems.isNotEmpty() && sCorrect >= sItems.size))) {
            Haptics.fanfare(this)
            Chime.levelUp()
        }

        // ---- Act 2: how each genre in this set grew (v0.17.0) ----
        val genreSection = resultSection("ジャンルの伸び")
        val afterGrads = currentGradsSnapshot()
        val totals = Questions.categories.map { cat -> Questions.all.count { it.category == cat } }
        val growth = SessionGrowth.growth(sStartGrads, afterGrads, totals)
        Questions.categories.forEachIndexed { idx, cat ->
            val inGenre = sItems.indices.filter { sItems[it].q.category == cat }
            if (inGenre.isEmpty()) return@forEachIndexed
            val correctInGenre = inGenre.count { sResults.getOrElse(it) { false } }
            val row = genreGrowthRow(cat, idx, correctInGenre, inGenre.size,
                growth.getOrElse(idx) { SessionGrowth.GenreGrowth(0, 0) })
            genreSection.addView(row, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        }
        panel.addView(genreSection, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        // ---- Act 3: the level (the level-up pill lives here now) ----
        val levelSection = resultSection("レベル")
        if (leveledUp) {
            val pill = label("レベルアップ！ Lv. $newLevel ・ ${LevelSystem.titleForLevel(newLevel)}", sizeBody, pal.yellowText, bold = true).apply {
                background = accentDrawable(dp(20).toFloat(), pal.yellow)
                setPadding(dp(16), dp(8), dp(16), dp(8))
            }
            levelSection.addView(pill)
            if (!reduceMotion()) {
                pill.scaleX = 0.7f; pill.scaleY = 0.7f; pill.alpha = 0f
                pill.animate().scaleX(1f).scaleY(1f).alpha(1f).setStartDelay(750).setDuration(320)
                    .setInterpolator(android.view.animation.OvershootInterpolator(2f)).start()
            }
            if (LevelSystem.titleForLevel(newLevel) != LevelSystem.titleForLevel(startLevel)) {
                val titleLine = label("称号が「${LevelSystem.titleForLevel(newLevel)}」に上がった！", sizeHead, pal.goldText, bold = true)
                val tLp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                tLp.topMargin = dp(10)
                levelSection.addView(titleLine, tLp)
            }
            // v0.18.0 (案5): every 10th level is a milestone of its own, so
            // the long stretches between titles never feel empty.
            if (newLevel % 10 == 0) {
                val ms = label("Lv. $newLevel 到達！大きな節目！", sizeBody, pal.goldText, bold = true)
                val mLp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                mLp.topMargin = dp(6)
                levelSection.addView(ms, mLp)
            }
        }
        levelSection.addView(label("+${sXp} XP", sizeBody, navy, bold = true))
        if (!leveledUp) {
            levelSection.addView(label("Lv. $newLevel ・ ${LevelSystem.titleForLevel(newLevel)}", sizeBody, navy, bold = true))
        }
        val bar = LevelBar()
        bar.describe("レベル$newLevel。次のレベルまであと約${approxQuestionsLeft(store.xp)}問")
        levelSection.addView(bar.root)
        panel.addView(levelSection, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        // ---- Act 4: the owl — did it evolve? (v0.17.0) ----
        val owlSection = resultSection("フクロウ")
        val owlStageNow = OwlGrowth.stageForLevel(newLevel)
        val owlStageStart = OwlGrowth.stageForLevel(startLevel)
        val owlPath = store.evolutionPath
        val evolved = owlStageNow > owlStageStart
        val owlRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val owlView = MascotView(this, owlStageNow, owlPath, newLevel)
        owlView.contentDescription = "フクロウ。現在の姿は${OwlGrowth.stageName(owlStageNow, owlPath)}"
        owlRow.addView(owlView, LinearLayout.LayoutParams(dp(72), dp(80)))
        val owlTexts = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), 0, 0, 0)
        }
        val branchReady = OwlGrowth.canBranch(newLevel) && owlPath == OwlGrowth.PATH_NONE
        when {
            evolved -> {
                owlTexts.addView(label("進化した！", sizeHead, pal.goldText, bold = true))
                owlTexts.addView(label("${OwlGrowth.stageName(owlStageNow, owlPath)} になった", sizeBody, pal.textMain))
                if (!reduceMotion()) {
                    owlView.scaleX = 0.5f; owlView.scaleY = 0.5f
                    owlView.animate().scaleX(1f).scaleY(1f).setStartDelay(1250).setDuration(380)
                        .setInterpolator(android.view.animation.OvershootInterpolator(2.2f)).start()
                }
            }
            branchReady -> {
                owlTexts.addView(label("進化の方向を選べるよ", sizeBody, pal.textMain, bold = true))
                owlTexts.addView(label("下の2つから選んでね", sizeSmall, grey))
            }
            else -> {
                owlTexts.addView(label("フクロウ：${OwlGrowth.stageName(owlStageNow, owlPath)}", sizeBody, pal.textMain, bold = true))
                val nextOwl = OwlGrowth.nextStageLevel(newLevel)
                owlTexts.addView(label(
                    if (nextOwl != null) "Lv. $nextOwl で進化" else "最終形", sizeSmall, grey))
            }
        }
        // v0.18.0 (案5): crossing Lv.40 / Lv.50 in this set earns the owl a
        // new accessory — say so here, where the owl is on stage.
        if (OwlGrowth.accessoryFor(newLevel) > OwlGrowth.accessoryFor(startLevel)) {
            owlTexts.addView(label("新しい飾りがついた！", sizeSmall, pal.goldText, bold = true))
        }
        owlRow.addView(owlTexts)
        owlSection.addView(owlRow)
        if (branchReady) {
            val options = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            listOf(
                OwlGrowth.PATH_CUTE to "かわいく",
                OwlGrowth.PATH_COOL to "かっこよく",
            ).forEach { (pathOpt, name) ->
                val opt = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.CENTER
                    setPadding(dp(8), dp(10), dp(8), dp(10))
                    background = cardDrawable(dp(16).toFloat())
                    isClickable = true
                    contentDescription = "$name 進化にする"
                    setOnClickListener { store.evolutionPath = pathOpt; showResult() }
                }
                opt.addView(MascotView(this, maxOf(3, owlStageNow), pathOpt, newLevel), LinearLayout.LayoutParams(dp(56), dp(64)))
                opt.addView(label(name, sizeBody, pal.textMain, bold = true))
                val lp = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                if (pathOpt == OwlGrowth.PATH_CUTE) lp.rightMargin = dp(10)
                options.addView(opt, lp)
            }
            val oLp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            oLp.topMargin = dp(10)
            owlSection.addView(options, oLp)
        }
        panel.addView(owlSection, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        val closing = label(
            when (sMode) {
                1 -> "苦手の復習、おつかれさま"
                2 -> "今日の復習、おつかれさま"
                3 -> "ジャンルの挑戦、おつかれさま"
                else -> "今日の分は完了。よくできました"
            }, sizeBody, grey)
        val closingLp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        closingLp.topMargin = dp(20)
        panel.addView(closing, closingLp)
        col.addView(panel)
        val home = primaryButton("ホームへ") {
            store.clearSession()
            showScreen(0)
        }
        val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(52))
        lp.topMargin = dp(24)
        col.addView(home, lp)
        setContent(ScrollView(this).apply { addView(col) })

        // The acts arrive in order — score, genres, level, owl — and the
        // level bar animates as its act lands (all instant if motion is off).
        stageReveal(genreSection, 350)
        stageReveal(levelSection, 750)
        stageReveal(owlSection, 1150)
        panel.postDelayed({
            if (leveledUp) {
                bar.animate(LevelSystem.progressInLevel(sStartXp, store.levelFloor), 100)
                bar.root.postDelayed({ bar.set(LevelSystem.progressInLevel(store.xp, store.levelFloor)) }, 480)
            } else {
                bar.animate(LevelSystem.progressInLevel(sStartXp, store.levelFloor), LevelSystem.progressInLevel(store.xp, store.levelFloor))
            }
        }, if (reduceMotion()) 0L else 750L)
    }

    // ---------- Review ----------
    private fun buildReview(): View {
        val today = ProgressStore.today()
        val states = store.allStates()
        val due = Questions.pool.filter { q ->
            states[q.id]?.let { ReviewLogic.isDue(it, today) || ReviewLogic.isSpotCheckDue(it, today) } == true
        }
        val weak = Questions.pool.filter { q -> states[q.id]?.let { ReviewLogic.isWeak(it) } == true }
        val col = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(20), dp(20), dp(20))
        }
        col.addView(label("復習", sizeHead, navy, bold = true))
        // Counts as pale-tint chips: the numbers get a solid accent point on this screen.
        val chipRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        fun countChip(text: String): TextView = label(text, sizeBody, pal.chipText, bold = true).apply {
            background = GradientDrawable().apply {
                cornerRadius = dp(16).toFloat(); setColor(pal.chipBg)
            }
            setPadding(dp(14), dp(6), dp(14), dp(6))
        }
        chipRow.addView(countChip("今日の復習 ${due.size}問"))
        val weakChip = countChip("苦手 ${weak.size}問")
        val wLp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        wLp.leftMargin = dp(12)
        chipRow.addView(weakChip, wLp)
        val cLp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        cLp.topMargin = dp(8); cLp.bottomMargin = dp(4)
        col.addView(chipRow, cLp)
        // How review works (UX3-05): in full on the first visit only; after
        // that it sits behind a one-tap toggle instead of standing text.
        val intro = label("間隔は 1日 → 3日 → 7日 → 21日。3回連続正解で卒業です", sizeSmall, grey)
        if (!store.reviewIntroSeen) {
            col.addView(intro)
            store.reviewIntroSeen = true
        } else {
            intro.visibility = View.GONE
            val toggle = label("仕組みについて", sizeSmall, grey).apply {
                setPadding(0, dp(4), 0, dp(4))
                setOnClickListener {
                    intro.visibility = if (intro.visibility == View.VISIBLE) View.GONE else View.VISIBLE
                }
            }
            col.addView(toggle)
            col.addView(intro)
        }
        if (due.isNotEmpty()) {
            val b = primaryButton("今日の復習をはじめる（${minOf(due.size, 5)}問）") { startSession(2) }
            val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(50))
            lp.topMargin = dp(16)
            col.addView(b, lp)
        }
        if (weak.isEmpty()) {
            col.addView(label("いま苦手な問題はありません", sizeBody, grey))
        } else {
            weak.take(5).forEach { q ->
                col.addView(label("・${q.prompt}", sizeSmall, pal.textMain))
            }
            // Label reflects the actual session size (IR2-12).
            val b = secondaryButton(if (weak.size >= 5) "苦手だけ5問" else "苦手だけ${weak.size}問") { startSession(1) }
            val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(50))
            lp.topMargin = dp(16)
            col.addView(b, lp)
        }
        return ScrollView(this).apply { addView(col) }
    }

    // ---------- Categories ----------
    private fun buildCategories(): View {
        val states = store.allStates()
        val col = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(20), dp(20), dp(20))
        }
        col.addView(label("カテゴリ", sizeHead, navy, bold = true))
        // Minimal pass (v0.16.0): the how-to line retired — each row's
        // 「挑戦」 pill already says the row is tappable.
        Questions.categories.forEachIndexed { idx, cat ->
            val qs = Questions.all.filter { it.category == cat }
            val graduated = qs.count { q -> states[q.id]?.let { ReviewLogic.isGraduated(it) } == true }
            val touched = qs.count { states[it.id] != null }
            val accent = pal.categoryColors[idx % pal.categoryColors.size]
            val box = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(16), dp(16), dp(16), dp(16))
                background = cardDrawable(dp(20).toFloat())
            }
            // Category unit icon: a filled colour circle with a drawn white
            // pictogram (speech bubble / heart / glyph / sign / coin).
            val chip = android.widget.ImageView(this).apply {
                background = circleDrawable(accent)
                setImageDrawable(CategoryIconDrawable(idx, accent))
                setPadding(dp(9), dp(9), dp(9), dp(9))
                contentDescription = cat
            }
            box.addView(chip, LinearLayout.LayoutParams(dp(48), dp(48)))
            val inner = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(12), 0, 0, 0)
            }
            inner.addView(label(cat, sizeBody, navy, bold = true))
            // Completion rate = graduated share (v0.6.0, user's request).
            // UX3-01: the bar is the one expression of the rate; the counts
            // below carry the exact numbers. (The big % numeral was removed.)
            val rate = if (qs.isEmpty()) 0 else graduated * 100 / qs.size
            val mini = LevelBar(accent, accent)
            mini.describe("$cat。コンプリート率 ${rate}パーセント。卒業 ${graduated}問 / 全 ${qs.size}問。解いた ${touched}問")
            mini.set(rate)
            inner.addView(mini.root)
            inner.addView(label("卒業 ${graduated}/${qs.size}問 ・ 解いた ${touched}問", sizeSmall, grey))
            box.addView(inner, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            // Right side: just the "挑戦" pill (UX3-01 removed the big %).
            // The whole box is the tap target for the genre challenge.
            val right = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER_HORIZONTAL
                setPadding(dp(10), 0, 0, 0)
            }
            val goPill = label("挑戦", sizeSmall, Color.WHITE, bold = true).apply {
                background = accentDrawable(dp(12).toFloat(), pal.green)
                setPadding(dp(12), dp(4), dp(12), dp(4))
            }
            val goLp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            right.addView(goPill, goLp)
            box.addView(right)
            box.isClickable = true
            box.contentDescription = "$cat に挑戦。コンプリート率 ${rate}パーセント"
            box.setOnClickListener { startSession(3, cat) }
            val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            lp.topMargin = dp(12)
            col.addView(box, lp)
        }
        // Minimal pass (v0.16.0): the lifetime-stats footer retired — it
        // was a standing statistic unrelated to this tab's job (picking a
        // genre); overall progress lives on Home (level + bookshelf).
        return ScrollView(this).apply { addView(col) }
    }

    private fun label(text: String, sizeSp: Float, color: Int, bold: Boolean = false, bodyLeading: Boolean = false): TextView =
        TextView(this).apply {
            this.text = text
            textSize = sizeSp
            setTextColor(color)
            if (bold) typeface = Typeface.DEFAULT_BOLD
            if (bodyLeading) setLineSpacing(0f, 1.6f)
            setPadding(0, dp(4), 0, dp(4))
        }
}
