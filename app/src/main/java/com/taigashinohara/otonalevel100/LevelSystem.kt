package com.taigashinohara.otonalevel100

/** Adult level 1..100 and its XP curve (design v1.3 §1/§3.5). Levels never go down. */
object LevelSystem {
    const val MAX_LEVEL = 100
    const val XP_NEW_CORRECT = 10
    // v0.18.0 (design review 案2): review corrects earn the same as new
    // ones — mastery must not slow the level down.
    const val XP_REVIEW_CORRECT = 10
    const val XP_STREAK_BONUS = 5
    // v0.18.0 (案7 序盤ブースト): while the player is a beginner (below
    // BEGINNER_LEVEL), every counted correct answer earns a bonus so the
    // first evolution arrives within the first few days.
    const val XP_BEGINNER_BONUS = 5
    const val BEGINNER_LEVEL = 10

    /**
     * XP needed to advance from [level] to level+1. The bands match the
     * title bands. v0.19.0 (user request): retuned so a steady daily player
     * (one 5-question set a day, roughly 45 XP/day) reaches the top rank in
     * about 3 months — the v1.3 curve (10,350 XP, ~8.5 months) made Lv.100
     * feel out of sight. Total to Lv100 is now 4,140 XP. The floor rule in
     * [levelForXp] keeps existing players from ever dropping.
     */
    fun xpToNext(level: Int): Int = when {
        level >= MAX_LEVEL -> 0
        level < 10 -> 20
        level < 25 -> 28
        level < 45 -> 36
        level < 65 -> 44
        level < 85 -> 52
        else -> 60
    }

    /** Cumulative XP at which [level] starts (level 1 starts at 0). */
    fun cumulativeXp(level: Int): Int {
        var sum = 0
        for (l in 1 until level.coerceIn(1, MAX_LEVEL)) sum += xpToNext(l)
        return sum
    }

    /**
     * Level for [xp], never below [floor]. The floor is the highest level the
     * user has ever reached; design v1.3 §3.5 requires that a curve change
     * (like the v1.3 smoothing itself) can never lower an existing level.
     */
    fun levelForXp(xp: Int, floor: Int = 1): Int {
        var level = 1
        while (level < MAX_LEVEL && xp >= cumulativeXp(level + 1)) level++
        return maxOf(level, floor.coerceIn(1, MAX_LEVEL))
    }

    /** Progress 0..100 inside the current (floor-aware) level. */
    fun progressInLevel(xp: Int, floor: Int = 1): Int {
        val level = levelForXp(xp, floor)
        if (level >= MAX_LEVEL) return 100
        val base = cumulativeXp(level)
        val span = xpToNext(level)
        return ((xp - base) * 100 / span).coerceIn(0, 100)
    }

    fun xpToNextLevel(xp: Int, floor: Int = 1): Int {
        val level = levelForXp(xp, floor)
        if (level >= MAX_LEVEL) return 0
        return cumulativeXp(level + 1) - xp
    }

    fun titleForLevel(level: Int): String = when {
        level >= 100 -> "オトナの達人"
        level >= 85 -> "教養あるオトナ"
        level >= 65 -> "頼れるオトナ"
        level >= 45 -> "一人前オトナ"
        level >= 25 -> "一人前手前"
        level >= 10 -> "駆け出しオトナ"
        else -> "見習いオトナ"
    }

    /** Total XP required to reach Lv100 (for design checks in tests). */
    fun totalXpToMax(): Int = cumulativeXp(MAX_LEVEL)
}

/** Leitner spaced repetition (design v1.1 §3). Days are epoch days. */
object ReviewLogic {
    /** Interval in days after a correct answer, indexed by the box reached. */
    private val intervalDays = mapOf(1 to 1, 2 to 3, 3 to 7, 4 to 21, 5 to 21)
    const val GRADUATE_BOX = 5

    data class State(val box: Int, val dueDay: Long, val streak: Int)

    fun initial(today: Long): State = State(box = 0, dueDay = today, streak = 0)

    fun onAnswer(state: State, correct: Boolean, today: Long): State {
        return if (correct) {
            val newBox = (state.box + 1).coerceAtMost(GRADUATE_BOX)
            State(box = newBox, dueDay = today + (intervalDays[newBox] ?: 21), streak = state.streak + 1)
        } else {
            // v0.18.0 (design review 案1): a miss costs ONE box, not the
            // whole ladder — the question still returns tomorrow so the
            // correction lands while it is fresh.
            State(box = maxOf(1, state.box - 1), dueDay = today + 1, streak = 0)
        }
    }

    /** Days until a missed spot check asks for confirmation (v0.18.0, 案1). */
    const val SPOT_CONFIRM_DAYS = 3L

    /**
     * A graduated question missed its spot check (v0.18.0, 案1): graduation
     * is KEPT — the bookshelf never loses a book — and the question simply
     * comes back in a few days to confirm. Answering it correctly then
     * resumes the normal +21-day spot-check cycle via [onAnswer]; missing
     * again just reschedules the confirmation.
     */
    fun onSpotCheckMiss(state: State, today: Long): State =
        state.copy(dueDay = today + SPOT_CONFIRM_DAYS)

    /**
     * Graduated after 3 consecutive correct answers AND reaching box 3
     * (UX review IR-6: streak alone let same-day repeats graduate instantly,
     * which broke the spacing of the intervals).
     */
    fun isGraduated(state: State): Boolean = state.streak >= 3 && state.box >= 3
    fun isWeak(state: State): Boolean = state.box in 1..2

    /**
     * Due for regular review. Graduated questions are excluded (UX review
     * IR2-2: graduation must remove a question from the review pool, or the
     * review load never shrinks and the "卒業" count contradicts behaviour).
     */
    fun isDue(state: State, today: Long): Boolean =
        state.box > 0 && state.dueDay <= today && !isGraduated(state)

    /**
     * Spot check (design: 卒業後は「たまに抜き打ちで再登場」). A graduated
     * question becomes spot-check due when its dueDay arrives (21 days after
     * the last correct answer). Answered correctly it stays graduated and the
     * next spot check is scheduled +21 days via [onAnswer]; answered wrong
     * it stays graduated too (v0.18.0, 案1) and only asks for a confirmation
     * a few days later — see [onSpotCheckMiss].
     */
    fun isSpotCheckDue(state: State, today: Long): Boolean =
        isGraduated(state) && state.dueDay <= today
}

/** Local calendar day as an epoch-day number (UX review IR2-1). */
object LocalDay {
    /**
     * Days since epoch in the device's local timezone. The previous UTC-based
     * count rolled the date over at 09:00 JST, which let a question missed in
     * the morning reappear as "next day" review the same day.
     */
    fun today(nowMillis: Long = System.currentTimeMillis()): Long {
        val offset = java.util.TimeZone.getDefault().getOffset(nowMillis)
        return Math.floorDiv(nowMillis + offset, 86_400_000L)
    }
}

/**
 * Praise lines (v0.13.0, user request: celebrate the user much more).
 * Pure selection logic so it is unit-testable; the wording always stays on
 * the "never shame" side — even a low score gets a warm line.
 */
object Praise {
    private val correctPool = listOf(
        "すごい！", "その調子！", "さすが！", "バッチリ！", "完璧！", "いいね！", "やったね！"
    )
    private val cardPool = listOf(
        "覚えてた！すごい！", "バッチリ思い出せた！", "しっかり記憶に残ってる！"
    )

    /** Praise for a correct quiz answer; consecutive streaks get their own line. */
    fun forCorrect(streak: Int, pick: Int): String =
        if (streak >= 2) "${streak}連続正解！絶好調！"
        else correctPool[Math.floorMod(pick, correctPool.size)]

    /** Praise for remembering a card. */
    fun forCard(pick: Int): String = cardPool[Math.floorMod(pick, cardPool.size)]

    /** Warm headline for the result screen, by score. Never shaming. */
    fun resultLine(correct: Int, total: Int): String = when {
        total > 0 && correct >= total -> "全問正解！完璧な1セット！"
        correct == total - 1 && total >= 4 -> "あと1問で全問正解！すごい！"
        correct >= 3 -> "よくできました！その調子！"
        correct >= 1 -> "おつかれさま！間違えた問題は、また会える"
        else -> "おつかれさま！間違いは伸びしろ。今日の分は完了"
    }

    /** Confetti amount scales with the score (UX3-09 follow-up in v0.13.0). */
    fun confettiPieces(correct: Int): Int = if (correct <= 0) 10 else 14 + correct * 7
}

/**
 * The mascot's growth (v0.14.0, concept B added by the user): stages follow
 * the level (any category counts — just keep playing), and from the branch
 * stage on, the user picks a cute or cool evolution path. Pure mapping,
 * unit-testable; the drawing lives in Art.kt's MascotView.
 */
object OwlGrowth {
    const val PATH_NONE = 0
    const val PATH_CUTE = 1
    const val PATH_COOL = 2

    /** Level at which the evolution-path choice opens. */
    const val BRANCH_LEVEL = 30

    /** 0 egg / 1 chick / 2 young / 3 branch stage / 4 final form. */
    fun stageForLevel(level: Int): Int = when {
        level >= 60 -> 4
        level >= BRANCH_LEVEL -> 3
        level >= 15 -> 2
        level >= 5 -> 1
        else -> 0
    }

    fun canBranch(level: Int): Boolean = level >= BRANCH_LEVEL

    /** Level at which the next stage arrives, or null at the final form. */
    fun nextStageLevel(level: Int): Int? = when (stageForLevel(level)) {
        0 -> 5
        1 -> 15
        2 -> BRANCH_LEVEL
        3 -> 60
        else -> null
    }

    /**
     * Mid-game milestone accessories (v0.18.0, design review 案5): the long
     * Lv.30 -> 60 stretch gets small visible rewards — 0 none, 1 a gold
     * badge (Lv.40), 2 badge + a small crown (Lv.50).
     */
    fun accessoryFor(level: Int): Int = when {
        level >= 50 -> 2
        level >= 40 -> 1
        else -> 0
    }

    fun stageName(stage: Int, path: Int): String = when (stage) {
        0 -> "たまご"
        1 -> "ヒナ"
        2 -> "わかば"
        3 -> when (path) {
            PATH_CUTE -> "かわいいオトナ"
            PATH_COOL -> "かっこいいオトナ"
            else -> "オトナ"
        }
        else -> when (path) {
            PATH_CUTE -> "まんまるオトナ"
            PATH_COOL -> "りりしいオトナ"
            else -> "立派なオトナ"
        }
    }
}

/**
 * The home-screen bookshelf (v0.14.0, concept D chosen by the user): one
 * shelf per category, books fill in with the graduation ratio, and a
 * completed category earns a gold book. Pure math, unit-testable.
 */
object Bookshelf {
    const val BOOKS_PER_SHELF = 10

    fun booksFor(graduated: Int, total: Int): Int =
        if (total <= 0) 0 else (graduated * BOOKS_PER_SHELF / total).coerceIn(0, BOOKS_PER_SHELF)

    fun isComplete(graduated: Int, total: Int): Boolean = total > 0 && graduated >= total
}

/**
 * Daily-set design (design v1.3 §3.5): the difficulty mix per level band and
 * the wave ordering of a set. Pure functions so they are unit-testable.
 */
object SessionDesign {

    /**
     * Percent of new questions per difficulty (1 easy, 2 normal, 3 hard).
     * By level band normally; the first week is deliberately gentle
     * (v0.18.0, 案7 序盤ブースト): the very first session is all-easy, and
     * sessions 2-7 stay easy-centred whatever the level band says, so a new
     * player meets success before difficulty.
     */
    fun difficultyMix(level: Int, sessionsDone: Int = -1): Triple<Int, Int, Int> = when {
        sessionsDone == 0 -> Triple(100, 0, 0)
        sessionsDone in 1..6 -> Triple(85, 15, 0)
        level < 10 -> Triple(70, 30, 0)
        level < 25 -> Triple(40, 50, 10)
        level < 45 -> Triple(20, 55, 25)
        level < 65 -> Triple(10, 50, 40)
        level < 85 -> Triple(0, 50, 50)
        else -> Triple(0, 40, 60)
    }

    /**
     * Pick [count] new questions from [candidates] following the band mix.
     * Each slot is assigned a difficulty proportionally; when that difficulty
     * is unavailable the slot falls back to the closest available difficulty
     * (design: 手ごわい不足→ふつう、やさしい不足→ふつう). Never blocks: if the
     * pool runs dry the result is simply shorter.
     */
    fun pickNew(
        candidates: List<Question>,
        count: Int,
        level: Int,
        random: kotlin.random.Random = kotlin.random.Random,
        sessionsDone: Int = -1
    ): List<Question> {
        if (count <= 0 || candidates.isEmpty()) return emptyList()
        val (easyPct, _, hardPct) = difficultyMix(level, sessionsDone)
        val wants = mutableListOf<Int>()
        repeat(Math.round(count * easyPct / 100.0).toInt()) { wants += 1 }
        repeat(Math.round(count * hardPct / 100.0).toInt()) { wants += 3 }
        while (wants.size < count) wants += 2
        val pool = candidates.shuffled(random).toMutableList()
        val picked = mutableListOf<Question>()
        for (want in wants) {
            val order = listOf(want, 2, 1, 3).distinct()
                .sortedBy { kotlin.math.abs(it - want) }
            val idx = order.firstNotNullOfOrNull { d ->
                pool.indexOfFirst { it.difficulty == d }.takeIf { it >= 0 }
            }
            if (idx != null) picked += pool.removeAt(idx)
        }
        return picked
    }

    /**
     * Order a daily set into the wave (design v1.3 §3.5): an opener (a review
     * question, or the easiest new one), new questions in the middle, the
     * hardest question at position 4 (index 3), and a closer (the second
     * review, or the easiest remaining) so the set ends on a success.
     */
    fun orderWave(
        review: List<Question>,
        fresh: List<Question>,
        random: kotlin.random.Random = kotlin.random.Random
    ): List<Question> {
        val all = review + fresh
        if (all.size < 3) return all.shuffled(random)
        val remaining = all.toMutableList()
        // v0.18.0 (design review 案6): the peak slot is for a NEW question.
        // A difficulty-3 review (one the player keeps missing) must not
        // become the set's climax — it stays in the safe middle instead.
        val peak = (if (fresh.isNotEmpty()) fresh.maxByOrNull { it.difficulty } else null)
            ?: remaining.maxByOrNull { it.difficulty }!!
        remaining.remove(peak)
        val opener = review.firstOrNull() ?: remaining.minByOrNull { it.difficulty }!!
        remaining.remove(opener)
        val closer = review.getOrNull(1) ?: remaining.minByOrNull { it.difficulty }!!
        remaining.remove(closer)
        val result = mutableListOf<Question>()
        result += opener
        result += remaining.shuffled(random)
        result.add(minOf(3, result.size), peak)
        result += closer
        return result
    }
}

/**
 * Per-genre growth across one session (v0.17.0, for the result screen):
 * how many questions graduated and how many bookshelf books appeared,
 * per category, between the session-start snapshot and now. Books are a
 * pure function of graduated counts (Bookshelf.booksFor), so the deltas
 * always agree with the home bookshelf.
 */
object SessionGrowth {
    data class GenreGrowth(val gradDelta: Int, val bookDelta: Int)

    fun growth(beforeGrads: List<Int>, afterGrads: List<Int>, totals: List<Int>): List<GenreGrowth> =
        totals.indices.map { i ->
            val before = beforeGrads.getOrElse(i) { 0 }
            val after = afterGrads.getOrElse(i) { 0 }
            val total = totals.getOrElse(i) { 0 }
            GenreGrowth(
                gradDelta = after - before,
                bookDelta = Bookshelf.booksFor(after, total) - Bookshelf.booksFor(before, total)
            )
        }
}
