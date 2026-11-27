package com.taigashinohara.otonalevel100

/** Adult level 1..100 and its XP curve (design v1.3 §1/§3.5). Levels never go down. */
object LevelSystem {
    const val MAX_LEVEL = 100
    const val XP_NEW_CORRECT = 10
    const val XP_REVIEW_CORRECT = 5
    const val XP_STREAK_BONUS = 5

    /**
     * XP needed to advance from [level] to level+1. Design v1.3 §3.5: the
     * bands match the title bands and grow by a flat +20 per band (the old
     * curve jumped 80 -> 120 at Lv50, which felt abruptly heavy). Total to
     * Lv100 is 10,350 XP, about the same journey length as before (10,250).
     */
    fun xpToNext(level: Int): Int = when {
        level >= MAX_LEVEL -> 0
        level < 10 -> 50
        level < 25 -> 70
        level < 45 -> 90
        level < 65 -> 110
        level < 85 -> 130
        else -> 150
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
            State(box = 1, dueDay = today + 1, streak = 0)
        }
    }

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
     * next spot check is scheduled +21 days via [onAnswer]; answered wrong it
     * drops back to box 1 and re-enters the normal learning cycle.
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
 * Daily-set design (design v1.3 §3.5): the difficulty mix per level band and
 * the wave ordering of a set. Pure functions so they are unit-testable.
 */
object SessionDesign {

    /** Percent of new questions per difficulty (1 easy, 2 normal, 3 hard), by level band. */
    fun difficultyMix(level: Int): Triple<Int, Int, Int> = when {
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
        random: kotlin.random.Random = kotlin.random.Random
    ): List<Question> {
        if (count <= 0 || candidates.isEmpty()) return emptyList()
        val (easyPct, _, hardPct) = difficultyMix(level)
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
        val peak = remaining.maxByOrNull { it.difficulty }!!
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
