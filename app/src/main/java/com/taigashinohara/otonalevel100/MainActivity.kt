package com.taigashinohara.otonalevel100

import android.animation.ValueAnimator
import android.app.Activity
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.RenderEffect
import android.graphics.Shader
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
 * Otona Level 100 v0.3.0.
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
 * v0.3.0: Simple Glass applied app-wide (skill v1.1 canonical values):
 * gradient foundation + up to 3 blurred light blobs, reading-glass surfaces
 * (white 62%->54% light / 19%->13% dark) with specular edge highlight,
 * gentle top-to-bottom gradients on every surface, solid navy for the main
 * action, dark-mode values prepared independently. Colour roles are unchanged
 * from ART_DIRECTION (navy = the star, gold = level-up only).
 */
class MainActivity : Activity() {

    // ---------- Simple Glass palette (roles fixed by ART_DIRECTION) ----------
    private inner class Palette(dark: Boolean) {
        val bgGrad: IntArray
        val blobColors: IntArray // argb tints, max 3 lights
        val glassTop: Int
        val glassBottom: Int
        val sheenTop: Int
        val textMain: Int
        val textSub: Int
        val heading: Int
        val accentTop: Int
        val accentBottom: Int
        val accentTextOn = Color.WHITE
        val tintPill: Int
        val goldTop: Int
        val goldBottom: Int
        val goldText: Int
        val tintStrong: Int
        /** Per-category accents (v0.3.1 colour-coding; v0.3.2: used only for
         *  functional points — glyph, dot, bar fill — never as a surface fill.
         *  categoryTints is kept as the light-tint reference for future use). */
        val categoryTints: List<Int>
        val categoryAccents: List<Int>
        val wrongBg = Color.rgb(0x6E, 0x6E, 0x6E)
        init {
            if (dark) {
                bgGrad = intArrayOf(
                    Color.rgb(0x13, 0x18, 0x2E), Color.rgb(0x15, 0x1D, 0x36), Color.rgb(0x0E, 0x13, 0x24))
                blobColors = intArrayOf(
                    Color.argb(84, 96, 120, 190), Color.argb(34, 201, 162, 39), Color.argb(58, 96, 138, 205))
                glassTop = Color.argb(48, 255, 255, 255)    // white 19%
                glassBottom = Color.argb(33, 255, 255, 255) // white 13%
                sheenTop = Color.argb(97, 255, 255, 255)    // white 38%
                textMain = Color.rgb(0xF2, 0xEF, 0xE7)
                textSub = Color.rgb(0xD8, 0xCF, 0xC2)       // skill canonical (dark sub)
                heading = Color.rgb(0xA9, 0xBC, 0xEF)
                accentTop = Color.rgb(0x54, 0x67, 0x9F)
                accentBottom = Color.rgb(0x3A, 0x4E, 0x86)
                tintPill = Color.argb(48, 255, 255, 255)
                goldTop = Color.rgb(0xD9, 0xBA, 0x55)
                goldBottom = Color.rgb(0xBE, 0x98, 0x23)
                goldText = Color.rgb(0xE3, 0xC5, 0x65)
                tintStrong = Color.rgb(0x33, 0x43, 0x6F)
                categoryTints = listOf(
                    Color.rgb(0x2B, 0x35, 0x57), Color.rgb(0x40, 0x2E, 0x38), Color.rgb(0x2A, 0x3B, 0x31),
                    Color.rgb(0x40, 0x38, 0x24), Color.rgb(0x26, 0x39, 0x4A))
                categoryAccents = listOf(
                    Color.rgb(0x8F, 0xA3, 0xDC), Color.rgb(0xD8, 0x9A, 0xAC), Color.rgb(0x93, 0xC4, 0xA6),
                    Color.rgb(0xE0, 0xBE, 0x6A), Color.rgb(0x8F, 0xBC, 0xD9))
            } else {
                bgGrad = intArrayOf(
                    Color.rgb(0xFF, 0xFB, 0xF2), Color.rgb(0xF6, 0xF1, 0xE7), Color.rgb(0xED, 0xF1, 0xF8))
                blobColors = intArrayOf(
                    Color.argb(84, 110, 132, 185), Color.argb(40, 201, 162, 39), Color.argb(56, 130, 165, 215))
                glassTop = Color.argb(158, 255, 255, 255)    // white 62%
                glassBottom = Color.argb(138, 255, 255, 255) // white 54%
                sheenTop = Color.argb(191, 255, 255, 255)    // white 75%
                textMain = Color.rgb(0x2B, 0x2B, 0x2B)
                textSub = Color.rgb(0x55, 0x4D, 0x44)        // skill canonical (light sub)
                heading = Color.rgb(0x1B, 0x2A, 0x52)
                accentTop = Color.rgb(0x27, 0x39, 0x66)
                accentBottom = Color.rgb(0x18, 0x26, 0x4A)
                tintPill = Color.rgb(0xE1, 0xE8, 0xF6)
                goldTop = Color.rgb(0xD9, 0xBA, 0x55)
                goldBottom = Color.rgb(0xBE, 0x98, 0x23)
                goldText = Color.rgb(0x7A, 0x5C, 0x00)
                tintStrong = Color.rgb(0xD7, 0xE1, 0xF4)
                categoryTints = listOf(
                    Color.rgb(0xE3, 0xE7, 0xF7), Color.rgb(0xF7, 0xE8, 0xEC), Color.rgb(0xE6, 0xEF, 0xE4),
                    Color.rgb(0xF7, 0xEF, 0xDA), Color.rgb(0xE2, 0xEF, 0xF7))
                categoryAccents = listOf(
                    Color.rgb(0x54, 0x67, 0x9F), Color.rgb(0xB8, 0x5C, 0x74), Color.rgb(0x5E, 0x8F, 0x74),
                    Color.rgb(0x9A, 0x74, 0x14), Color.rgb(0x4E, 0x84, 0xA8))
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
    private var sMode = 0 // 0 daily, 1 weak-only, 2 due-review
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

        // Floating reading-glass tab bar (parts.md: tab bar = reading glass).
        val tabBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(dp(8), dp(6), dp(8), dp(6))
            background = glassDrawable(dp(24).toFloat())
        }
        val names = listOf("ホーム", "復習", "カテゴリ")
        tabButtons = names.mapIndexed { i, name ->
            Button(this).apply {
                text = name
                textSize = sizeBody
                background = null
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

    /** Standard foundation (quality.md §1): 3-point gradient + up to 3 blurred light blobs. */
    private fun buildBackground(): View {
        val frame = FrameLayout(this)
        val gradient = View(this).apply {
            background = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, pal.bgGrad)
        }
        frame.addView(gradient, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        val blobs = object : View(this) {
            override fun onDraw(canvas: Canvas) {
                super.onDraw(canvas)
                val paint = Paint(Paint.ANTI_ALIAS_FLAG)
                val r = dp(300).toFloat()
                // Template: top-left = main colour, bottom-right = sub colour, bottom-centre = third.
                val spots = listOf(
                    Triple(-r * 0.25f, -r * 0.3f, 0),
                    Triple(width - r * 0.7f, height - r * 0.75f, 1),
                    Triple(width * 0.5f - r / 2, height - r * 0.55f, 2)
                )
                for ((x, y, idx) in spots) {
                    val c = pal.blobColors[idx]
                    paint.shader = RadialGradient(
                        x + r / 2, y + r / 2, r / 2,
                        c, Color.argb(0, Color.red(c), Color.green(c), Color.blue(c)),
                        Shader.TileMode.CLAMP)
                    canvas.drawCircle(x + r / 2, y + r / 2, r / 2, paint)
                }
            }
        }
        if (Build.VERSION.SDK_INT >= 31) {
            // Spec is 60-80dp; RenderEffect takes physical px, so convert (IR2-8).
            val blurPx = dp(70).toFloat()
            blobs.setRenderEffect(RenderEffect.createBlurEffect(blurPx, blurPx, Shader.TileMode.CLAMP))
        }
        frame.addView(blobs, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        return frame
    }

    /** Reading-glass surface: white gradient (62%->54% / 19%->13%) + specular top sheen. */
    private fun glassDrawable(radius: Float): LayerDrawable {
        val glass = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(pal.glassTop, pal.glassBottom)).apply { cornerRadius = radius }
        val sheen = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(pal.sheenTop, Color.argb(0, 255, 255, 255))).apply { cornerRadius = radius }
        return LayerDrawable(arrayOf(glass, sheen))
    }

    /** Circular reading-glass surface (v0.3.2: chips are glass; colour lives in the glyph, not the fill). */
    private fun glassOval(): GradientDrawable = GradientDrawable(
        GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(pal.glassTop, pal.glassBottom)
    ).apply {
        shape = GradientDrawable.OVAL
        setStroke(dp(1), pal.sheenTop)
    }

    /** Solid role-colour surface with the gentle gradient (white +8% -> black -4%). */
    private fun accentDrawable(radius: Float, top: Int = pal.accentTop, bottom: Int = pal.accentBottom): GradientDrawable =
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
            b.setTextColor(if (selected) navy else grey)
            b.typeface = if (selected) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            b.background = if (selected) GradientDrawable().apply {
                cornerRadius = dp(20).toFloat(); setColor(pal.tintPill)
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

    // ---------- Level bar (parts.md: track = reading glass, progress = solid role colour) ----------
    private inner class LevelBar(fillTop: Int = pal.accentTop, fillBottom: Int = pal.accentBottom) {
        val root = FrameLayout(this@MainActivity).apply { background = glassDrawable(dp(10).toFloat()) }
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

    // ---------- Home: the level is the star, on a reading-glass panel ----------
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
        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(24), dp(24), dp(24), dp(20))
            background = glassDrawable(dp(28).toFloat())
        }
        panel.addView(label("オトナレベル100", sizeBody, grey, bold = true))
        panel.addView(label("Lv. $level", sizeHero, navy, bold = true))
        panel.addView(label(LevelSystem.titleForLevel(level), sizeHead, navy, bold = true))
        val bar = LevelBar()
        bar.describe("レベル$level。次のレベルまであと約${approxQuestionsLeft(xp)}問")
        bar.set(LevelSystem.progressInLevel(xp))
        panel.addView(bar.root)
        if (level >= LevelSystem.MAX_LEVEL) {
            panel.addView(label("オトナの達人に到達！", sizeBody, pal.goldText, bold = true))
        } else {
            panel.addView(label("あと正解 約 ${approxQuestionsLeft(xp)} 問 でレベルアップ", sizeBody, grey))
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
        setTextColor(pal.accentTextOn)
        // Pressed state defined (quality.md §3): pressed = the darker end of the gradient.
        background = StateListDrawable().apply {
            addState(intArrayOf(android.R.attr.state_pressed), accentDrawable(dp(28).toFloat(), pal.accentBottom, pal.accentBottom))
            addState(intArrayOf(), accentDrawable(dp(28).toFloat()))
        }
        setOnClickListener { onClick() }
    }

    /**
     * Secondary action (one solid primary per screen, gate 5). v0.3.1: a plain
     * glass button read as "disabled", so this uses a solid strong tint with a
     * defined edge and bold heading-coloured text — clearly tappable.
     */
    private fun secondaryButton(text: String, onClick: () -> Unit): Button = Button(this).apply {
        this.text = text
        textSize = sizeBody
        typeface = Typeface.DEFAULT_BOLD
        setTextColor(navy)
        val normal = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(pal.tintStrong, pal.tintStrong)).apply {
            cornerRadius = dp(28).toFloat()
            setStroke(dp(1), Color.argb(140, Color.red(navy), Color.green(navy), Color.blue(navy)))
        }
        val pressed = GradientDrawable().apply {
            cornerRadius = dp(28).toFloat(); setColor(pal.tintPill)
            setStroke(dp(1), navy)
        }
        background = StateListDrawable().apply {
            addState(intArrayOf(android.R.attr.state_pressed), pressed)
            addState(intArrayOf(), normal)
        }
        setOnClickListener { onClick() }
    }

    /** Glass choice/judge button with a defined pressed state (quality.md §3). */
    private fun glassButton(text: String, onClick: () -> Unit): Button = Button(this).apply {
        this.text = text
        textSize = sizeBody
        setTextColor(navy)
        val normal = glassDrawable(dp(20).toFloat())
        val pressed = LayerDrawable(arrayOf(
            GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(pal.tintPill, pal.tintPill)).apply { cornerRadius = dp(20).toFloat() }
        ))
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

    private fun startSession(mode: Int) {
        val today = ProgressStore.today()
        val states = store.allStates()
        val picked: List<Question> = when (mode) {
            1 -> Questions.all.filter { q -> states[q.id]?.let { ReviewLogic.isWeak(it) } == true }
                .shuffled().take(5)
            2 -> Questions.all.filter { q -> states[q.id]?.let { ReviewLogic.isDue(it, today) } == true }
                .sortedBy { states[it.id]?.dueDay ?: 0L }.take(5)
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
        if (picked.isEmpty()) { showScreen(1); return }
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
        val catAccent = pal.categoryAccents[catIdx % pal.categoryAccents.size]
        val pillText = android.text.SpannableString("● ${q.category}").apply {
            setSpan(android.text.style.ForegroundColorSpan(catAccent), 0, 1,
                android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        val catPill = label("", sizeSmall, navy, bold = true).apply {
            text = pillText
            background = glassDrawable(dp(12).toFloat())
            setPadding(dp(10), dp(3), dp(10), dp(3))
        }
        val pillLp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        pillLp.leftMargin = dp(8)
        headerRow.addView(catPill, pillLp)
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
            background = glassDrawable(dp(24).toFloat())
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
                    b.setTextColor(pal.accentTextOn)
                    b.background = accentDrawable(dp(20).toFloat())
                } else if (i == sChosen) {
                    b.text = "× ${item.choices[i]}"
                    b.setTextColor(pal.accentTextOn)
                    b.background = accentDrawable(dp(20).toFloat(), pal.wrongBg, pal.wrongBg)
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
        val answerText = if (q.type == QType.CARD) q.cardBack else item.choices[item.answerIndex]
        feedback.addView(label(if (correct) "正解" else "正解は「$answerText」", sizeBody, navy, bold = true))
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
            background = glassDrawable(dp(28).toFloat())
        }
        if (leveledUp) {
            // Gold lives here only (IR-2): solid with the gentle gradient, fixed dark-navy text (IR-3).
            val pill = label("レベルアップ！ Lv. $newLevel ・ ${LevelSystem.titleForLevel(newLevel)}", sizeBody, Color.rgb(0x1B, 0x2A, 0x52), bold = true).apply {
                background = accentDrawable(dp(20).toFloat(), pal.goldTop, pal.goldBottom)
                setPadding(dp(16), dp(8), dp(16), dp(8))
            }
            panel.addView(pill)
        }
        panel.addView(label("${sItems.size}問中 ${sCorrect}問 正解", sizeHead, navy, bold = true))
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
        fun countChip(text: String): TextView = label(text, sizeBody, navy, bold = true).apply {
            background = GradientDrawable().apply {
                cornerRadius = dp(16).toFloat(); setColor(pal.tintStrong)
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
        Questions.categories.forEachIndexed { idx, cat ->
            val qs = Questions.all.filter { it.category == cat }
            val graduated = qs.count { q -> states[q.id]?.let { ReviewLogic.isGraduated(it) } == true }
            val touched = qs.count { states[it.id] != null }
            val accent = pal.categoryAccents[idx % pal.categoryAccents.size]
            val box = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(16), dp(16), dp(16), dp(16))
                background = glassDrawable(dp(20).toFloat())
            }
            // Category glyph chip (v0.3.2 principle: colour comes through the
            // glass as light, surfaces are not painted). The chip itself is
            // reading glass; the category colour lives only in the glyph.
            val chip = TextView(this).apply {
                text = cat.take(1)
                textSize = sizeHead
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(accent)
                gravity = Gravity.CENTER
                background = glassOval()
            }
            box.addView(chip, LinearLayout.LayoutParams(dp(44), dp(44)))
            val inner = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(12), 0, 0, 0)
            }
            inner.addView(label(cat, sizeBody, navy, bold = true))
            // v0.3.1: the bar shows the SOLVED ratio in the category accent colour
            // (it previously showed graduations only, so 2/4 solved looked empty).
            val mini = LevelBar(accent, accent)
            mini.describe("$cat。解いた ${touched}問 / 全 ${qs.size}問。卒業 ${graduated}問")
            mini.set(if (qs.isEmpty()) 0 else touched * 100 / qs.size)
            inner.addView(mini.root)
            inner.addView(label("解いた ${touched}/${qs.size} ・ 卒業 ${graduated}問", sizeSmall, grey))
            box.addView(inner, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
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
