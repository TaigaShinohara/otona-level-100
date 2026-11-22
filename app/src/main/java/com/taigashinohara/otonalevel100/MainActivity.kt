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
import kotlin.random.Random

/**
 * Otona Level 100 v0.6.0.
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

    // ---------- Duolingo-style palette (v0.4.0, ART_DIRECTION v2.0) ----------
    // Vivid flat colours, chunky shapes, bold type. Dark follows Duolingo's
    // night theme (deep blue-grey). Colour roles: green = primary / progress /
    // correct, blue = selection & info, yellow = level-up only, red tints =
    // "not quite" feedback (informative, never shaming).
    private inner class Palette(dark: Boolean) {
        val bgTop: Int
        val bgBottom: Int
        val surface: Int
        val border: Int
        val track: Int
        val textMain: Int
        val textSub: Int
        val heading: Int
        val green = Color.rgb(0x58, 0xCC, 0x02)
        val greenDark = Color.rgb(0x58, 0xA7, 0x00)
        val greenTextOn = Color.WHITE
        val blue = Color.rgb(0x1C, 0xB0, 0xF6)
        val tabSelBg: Int
        val tabSelText: Int
        val yellow = Color.rgb(0xFF, 0xC8, 0x00)
        val yellowText = Color.rgb(0x7C, 0x5E, 0x00)
        val correctBg: Int
        val correctBorder: Int
        val correctText: Int
        val wrongBgC: Int
        val wrongBorder: Int
        val wrongText: Int
        val chipBg: Int
        val chipText: Int
        /** Bright per-category fills (unit colours). */
        val categoryColors: List<Int> = listOf(
            Color.rgb(0x1C, 0xB0, 0xF6), // 敬語 blue
            Color.rgb(0xFF, 0x86, 0xD0), // マナー pink
            Color.rgb(0xA5, 0x60, 0xF5), // 漢字 purple
            Color.rgb(0xFF, 0x96, 0x00), // 標識 orange
            Color.rgb(0x58, 0xCC, 0x02)) // 社会 green
        init {
            if (dark) {
                bgTop = Color.rgb(0x13, 0x1F, 0x24)
                bgBottom = Color.rgb(0x0E, 0x18, 0x1C)
                surface = Color.rgb(0x1F, 0x2A, 0x30)
                border = Color.rgb(0x37, 0x46, 0x4F)
                track = Color.rgb(0x37, 0x46, 0x4F)
                textMain = Color.rgb(0xF1, 0xF7, 0xFB)
                textSub = Color.rgb(0xA8, 0xB6, 0xBC)
                heading = Color.rgb(0xF1, 0xF7, 0xFB)
                tabSelBg = Color.rgb(0x1E, 0x3A, 0x4C)
                tabSelText = Color.rgb(0x7C, 0xD4, 0xFC)
                correctBg = Color.rgb(0x1D, 0x33, 0x27)
                correctBorder = green
                correctText = Color.rgb(0x8C, 0xE9, 0x9A)
                wrongBgC = Color.rgb(0x3D, 0x24, 0x28)
                wrongBorder = Color.rgb(0xFF, 0x4B, 0x4B)
                wrongText = Color.rgb(0xFF, 0xA8, 0xB8)
                chipBg = Color.rgb(0x1E, 0x3A, 0x4C)
                chipText = Color.rgb(0x7C, 0xD4, 0xFC)
            } else {
                bgTop = Color.WHITE
                bgBottom = Color.rgb(0xF7, 0xF7, 0xF7)
                surface = Color.WHITE
                border = Color.rgb(0xE5, 0xE5, 0xE5)
                track = Color.rgb(0xE5, 0xE5, 0xE5)
                textMain = Color.rgb(0x3C, 0x3C, 0x3C)
                textSub = Color.rgb(0x77, 0x77, 0x77)
                heading = Color.rgb(0x3C, 0x3C, 0x3C)
                tabSelBg = Color.rgb(0xDD, 0xF4, 0xFF)
                tabSelText = Color.rgb(0x18, 0x99, 0xD6)
                correctBg = Color.rgb(0xD7, 0xFF, 0xB8)
                correctBorder = green
                correctText = Color.rgb(0x58, 0xA7, 0x00)
                wrongBgC = Color.rgb(0xFF, 0xDF, 0xE0)
                wrongBorder = Color.rgb(0xFF, 0x4B, 0x4B)
                wrongText = Color.rgb(0xEA, 0x2B, 0x2B)
                chipBg = Color.rgb(0xDD, 0xF4, 0xFF)
                chipText = Color.rgb(0x18, 0x99, 0xD6)
            }
        }
    }

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
            if (sIndex < sItems.size) showQuestion() else showResult()
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
        if (inQuestionPhase) return
        if (store.hasSession()) store.clearSession()
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

    private fun setTabsEnabled(enabled: Boolean) {
        tabButtons.forEach { it.isEnabled = enabled; it.alpha = if (enabled) 1f else 0.45f }
    }

    private fun showScreen(index: Int) {
        inQuestionPhase = false
        setTabsEnabled(true)
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
        fillTop: Int = pal.green,
        fillBottom: Int = pal.green,
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
        (LevelSystem.xpToNextLevel(xp) + LevelSystem.XP_NEW_CORRECT - 1) / LevelSystem.XP_NEW_CORRECT

    // ---------- Home: the level is the star, on a green unit-header card ----------
    private fun buildHome(): View {
        val xp = store.xp
        val level = LevelSystem.levelForXp(xp)
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
        val cheers = listOf(
            "今日も5問、いってみよう！", "昨日の自分より、ちょっと大人に",
            "「知らないと恥ずかしい」を1つ減らそう", "スキマ時間でレベルアップ！",
            "無理なく、楽しくいこう")
        val cheer = cheers[(today % cheers.size).toInt()]
        val mascotRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        mascotRow.addView(MascotView(this), LinearLayout.LayoutParams(dp(84), dp(96)))
        val bubble = label(cheer, sizeBody, pal.greenDark, bold = true).apply {
            background = GradientDrawable().apply {
                cornerRadius = dp(16).toFloat(); setColor(Color.WHITE)
            }
            setPadding(dp(14), dp(10), dp(14), dp(10))
        }
        val bubbleLp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        bubbleLp.leftMargin = dp(12)
        mascotRow.addView(bubble, bubbleLp)
        panel.addView(mascotRow)
        panel.addView(label("オトナレベル100", sizeBody, Color.WHITE, bold = true))
        panel.addView(label("Lv. $level", sizeHero, Color.WHITE, bold = true))
        panel.addView(label(LevelSystem.titleForLevel(level), sizeHead, Color.WHITE, bold = true))
        val bar = LevelBar(fillTop = pal.yellow, fillBottom = pal.yellow, trackColor = pal.greenDark)
        bar.describe("レベル$level。次のレベルまであと約${approxQuestionsLeft(xp)}問")
        bar.set(LevelSystem.progressInLevel(xp))
        panel.addView(bar.root)
        if (level >= LevelSystem.MAX_LEVEL) {
            panel.addView(label("オトナの達人に到達！", sizeBody, pal.yellow, bold = true))
        } else {
            panel.addView(label("あと正解 約 ${approxQuestionsLeft(xp)} 問 でレベルアップ", sizeBody, Color.WHITE))
        }
        col.addView(panel)

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
        }
        col.addView(label("1セット5問・約1分。レベルは下がりません", sizeSmall, grey))
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

    private fun persistSession() {
        store.saveSession(
            ProgressStore.SavedSession(
                sItems.map { ProgressStore.SavedItem(it.q.id, it.order) },
                sIndex, sCorrect, sXp, sStreak, sStartXp, sMode, sAnswered, sChosen, sCounted
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
    }

    private fun resumeSavedSession() {
        val saved = store.loadSession() ?: return
        restoreSession(saved)
        if (sIndex < sItems.size) showQuestion() else showResult()
    }

    private fun startSession(mode: Int, category: String? = null) {
        val today = ProgressStore.today()
        val states = store.allStates()
        val picked: List<Question> = when (mode) {
            1 -> Questions.all.filter { q -> states[q.id]?.let { ReviewLogic.isWeak(it) } == true }
                .shuffled().take(5)
            2 -> Questions.all.filter { q -> states[q.id]?.let { ReviewLogic.isDue(it, today) } == true }
                .sortedBy { states[it.id]?.dueDay ?: 0L }.take(5)
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
                val due = Questions.all.filter { q ->
                    states[q.id]?.let { ReviewLogic.isDue(it, today) || ReviewLogic.isSpotCheckDue(it, today) } == true
                }.shuffled()
                list += due.take(2)
                val fresh = Questions.all.filter { it.id !in list.map { p -> p.id } && states[it.id] == null }.shuffled()
                list += fresh.take(5 - list.size)
                if (list.size < 5) {
                    // Fill-up excludes graduated questions (IR2-2): graduation
                    // means leaving the pool, not just the review list.
                    val rest = Questions.all.filter { q ->
                        q.id !in list.map { p -> p.id } &&
                            states[q.id]?.let { ReviewLogic.isGraduated(it) } != true
                    }.shuffled()
                    list += rest.take(5 - list.size)
                }
                if (list.none { it.type == QType.CARD }) {
                    val card = Questions.all.firstOrNull { q ->
                        q.type == QType.CARD && q.id !in list.map { p -> p.id } &&
                            states[q.id]?.let { ReviewLogic.isGraduated(it) } != true
                    } ?: Questions.all.firstOrNull { it.type == QType.CARD && it.id !in list.map { p -> p.id } }
                    if (card != null && list.size >= 5) list[4] = card
                }
                list.take(5)
            }
        }
        if (picked.isEmpty()) { showScreen(if (mode == 3) 2 else 1); return }
        sItems = buildItems(picked)
        sIndex = 0; sCorrect = 0; sXp = 0; sStreak = 0
        sStartXp = store.xp; sMode = mode; sAnswered = false; sChosen = -1; sCounted = false
        persistSession()
        showQuestion()
    }

    private fun showQuestion() {
        inQuestionPhase = true
        setTabsEnabled(false)
        val item = sItems[sIndex]
        val q = item.q
        val xpNow = store.xp
        val levelNow = LevelSystem.levelForXp(xpNow)

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
        // Small category pictogram next to the pill (v0.5.0).
        val catIcon = android.widget.ImageView(this).apply {
            background = circleDrawable(catColor)
            setImageDrawable(CategoryIconDrawable(catIdx, catColor))
            setPadding(dp(5), dp(5), dp(5), dp(5))
            contentDescription = q.category
        }
        val iconLp = LinearLayout.LayoutParams(dp(26), dp(26))
        iconLp.leftMargin = dp(6)
        headerRow.addView(catIcon, iconLp)
        rootCol.addView(headerRow)

        val barRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        barRow.addView(label("Lv. $levelNow", sizeBody, navy, bold = true))
        val miniBar = LevelBar()
        miniBar.describe("レベル$levelNow。次のレベルまであと約${approxQuestionsLeft(xpNow)}問")
        miniBar.set(LevelSystem.progressInLevel(xpNow))
        val barLp = LinearLayout.LayoutParams(0, dp(18), 1f)
        barLp.leftMargin = dp(12)
        barRow.addView(miniBar.root, barLp)
        rootCol.addView(barRow)

        val scrollCol = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(8), 0, dp(8))
        }
        scrollCol.addView(label(q.prompt, sizeHead, navy, bold = true))

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
        renderAnswered(item, feedback, nextBtn, null, animateBar = true, xpBefore = xpBefore, miniBar = miniBar)
    }

    private fun renderAnswered(
        item: SItem, feedback: LinearLayout, nextBtn: Button, scrollCol: LinearLayout?,
        animateBar: Boolean, xpBefore: Int = store.xp, buttons: List<Button>? = null, miniBar: LevelBar? = null
    ) {
        val q = item.q
        val correct = if (q.type == QType.CARD) sChosen == 1 else sChosen == item.answerIndex
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
            val levelBefore = LevelSystem.levelForXp(xpBefore)
            val levelNow = LevelSystem.levelForXp(store.xp)
            if (animateBar && correct) {
                if (levelNow > levelBefore) {
                    miniBar.animate(LevelSystem.progressInLevel(xpBefore), 100)
                    miniBar.root.postDelayed({ miniBar.set(LevelSystem.progressInLevel(store.xp)) }, 480)
                } else {
                    miniBar.animate(LevelSystem.progressInLevel(xpBefore), LevelSystem.progressInLevel(store.xp))
                }
            } else {
                miniBar.set(LevelSystem.progressInLevel(store.xp))
            }
            miniBar.describe("レベル$levelNow。次のレベルまであと約${approxQuestionsLeft(store.xp)}問")
        }
        showFeedback(item, correct, feedback)
        nextBtn.visibility = View.VISIBLE
    }

    private fun applyAnswer(q: Question, correct: Boolean) {
        val today = ProgressStore.today()
        val before = store.stateFor(q.id)
        val isReview = before != null && before.box > 0
        val dueOrNew = before == null || ReviewLogic.isDue(before, today) ||
            ReviewLogic.isSpotCheckDue(before, today)
        if (!correct || dueOrNew) {
            store.saveState(q.id, ReviewLogic.onAnswer(before ?: ReviewLogic.initial(today), correct, today))
        }
        store.totalAnswered += 1
        if (correct) {
            store.totalCorrect += 1
            sCorrect += 1
            sStreak += 1
            var gain = if (isReview) LevelSystem.XP_REVIEW_CORRECT else LevelSystem.XP_NEW_CORRECT
            if (sStreak % 3 == 0) gain += LevelSystem.XP_STREAK_BONUS
            sXp += gain
            store.xp += gain
        } else {
            sStreak = 0
        }
    }

    private fun showFeedback(item: SItem, correct: Boolean, feedback: LinearLayout) {
        val q = item.q
        feedback.removeAllViews()
        feedback.visibility = View.VISIBLE
        // Duolingo-style feedback banner: green tint = correct, red tint = "not
        // quite" (informative colour, no shaming).
        feedback.background = GradientDrawable().apply {
            cornerRadius = dp(16).toFloat()
            setColor(if (correct) pal.correctBg else pal.wrongBgC)
        }
        feedback.setPadding(dp(14), dp(10), dp(14), dp(10))
        val answerText = if (q.type == QType.CARD) q.cardBack else item.choices[item.answerIndex]
        feedback.addView(label(if (correct) "正解" else "正解は「$answerText」", sizeBody,
            if (correct) pal.correctText else pal.wrongText, bold = true))
        feedback.addView(label(q.explanation, sizeBody, pal.textMain, bodyLeading = true))
        feedback.addView(label("場面：${q.scene}", sizeSmall, grey))
        feedback.addView(label("出典：${q.source}", sizeSmall, grey))
    }

    private fun goNext() {
        sIndex++
        sAnswered = false
        sChosen = -1
        persistSession()
        if (sIndex < sItems.size) showQuestion() else showResult()
    }

    // ---------- Result ----------
    private fun showResult() {
        inQuestionPhase = false
        setTabsEnabled(true)
        if (!sCounted) {
            if (sMode == 0) {
                store.sessionsDone += 1
                store.lastDailyDay = ProgressStore.today()
            }
            sCounted = true
            persistSession()
        }
        val newLevel = LevelSystem.levelForXp(store.xp)
        val startLevel = LevelSystem.levelForXp(sStartXp)
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
        if (leveledUp) {
            // Yellow lives here only: the level-up pill (Duolingo achievement yellow).
            val pill = label("レベルアップ！ Lv. $newLevel ・ ${LevelSystem.titleForLevel(newLevel)}", sizeBody, pal.yellowText, bold = true).apply {
                background = accentDrawable(dp(20).toFloat(), pal.yellow)
                setPadding(dp(16), dp(8), dp(16), dp(8))
            }
            panel.addView(pill)
        }
        // Celebration (v0.5.0): confetti across the top of the panel, and a
        // star per question — filled for each correct answer.
        val confetti = ConfettiView(this, seed = sStartXp + sCorrect * 31)
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
        }
        val starLp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        starLp.topMargin = dp(6); starLp.bottomMargin = dp(2)
        panel.addView(starRow, starLp)
        panel.addView(label("+${sXp} XP", sizeBody, navy, bold = true))
        panel.addView(label("Lv. $newLevel ・ ${LevelSystem.titleForLevel(newLevel)}", sizeBody, navy, bold = true))
        val bar = LevelBar()
        bar.describe("レベル$newLevel。次のレベルまであと約${approxQuestionsLeft(store.xp)}問")
        panel.addView(bar.root)
        if (leveledUp) {
            bar.animate(LevelSystem.progressInLevel(sStartXp), 100)
            bar.root.postDelayed({ bar.set(LevelSystem.progressInLevel(store.xp)) }, 480)
        } else {
            bar.animate(LevelSystem.progressInLevel(sStartXp), LevelSystem.progressInLevel(store.xp))
        }
        panel.addView(label(
            when (sMode) {
                1 -> "苦手の復習、おつかれさま"
                2 -> "今日の復習、おつかれさま"
                3 -> "ジャンルの挑戦、おつかれさま"
                else -> "今日の分は完了。よくできました"
            }, sizeBody, grey))
        col.addView(panel)
        val home = primaryButton("ホームへ") {
            store.clearSession()
            showScreen(0)
        }
        val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(52))
        lp.topMargin = dp(24)
        col.addView(home, lp)
        setContent(ScrollView(this).apply { addView(col) })
    }

    // ---------- Review ----------
    private fun buildReview(): View {
        val today = ProgressStore.today()
        val states = store.allStates()
        val due = Questions.all.filter { q -> states[q.id]?.let { ReviewLogic.isDue(it, today) } == true }
        val weak = Questions.all.filter { q -> states[q.id]?.let { ReviewLogic.isWeak(it) } == true }
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
        col.addView(label("間隔は 1日 → 3日 → 7日 → 21日。3回連続正解で卒業です", sizeSmall, grey))
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
        col.addView(label("ジャンルをタップすると、そのジャンルだけで挑戦できます", sizeSmall, grey))
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
            // Completion rate = graduated share (v0.6.0, user's request). The
            // bar and the big % both show it; the solved count stays as a sub-line.
            val rate = if (qs.isEmpty()) 0 else graduated * 100 / qs.size
            val mini = LevelBar(accent, accent)
            mini.describe("$cat。コンプリート率 ${rate}パーセント。卒業 ${graduated}問 / 全 ${qs.size}問。解いた ${touched}問")
            mini.set(rate)
            inner.addView(mini.root)
            inner.addView(label("卒業 ${graduated}/${qs.size}問 ・ 解いた ${touched}問", sizeSmall, grey))
            box.addView(inner, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            // Right side: the big completion % and a "挑戦" pill. The whole box
            // is the tap target for the genre challenge.
            val right = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER_HORIZONTAL
                setPadding(dp(10), 0, 0, 0)
            }
            right.addView(label("$rate%", sizeHead, accent, bold = true))
            val goPill = label("挑戦", sizeSmall, Color.WHITE, bold = true).apply {
                background = accentDrawable(dp(12).toFloat(), pal.green)
                setPadding(dp(12), dp(4), dp(12), dp(4))
            }
            val goLp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            goLp.topMargin = dp(6)
            right.addView(goPill, goLp)
            box.addView(right)
            box.isClickable = true
            box.contentDescription = "$cat に挑戦。コンプリート率 ${rate}パーセント"
            box.setOnClickListener { startSession(3, cat) }
            val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            lp.topMargin = dp(12)
            col.addView(box, lp)
        }
        val totalGraduated = states.values.count { ReviewLogic.isGraduated(it) }
        col.addView(label("これまで解いた問題 ${store.totalAnswered} ・ 卒業 ${totalGraduated}問", sizeSmall, grey))
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
