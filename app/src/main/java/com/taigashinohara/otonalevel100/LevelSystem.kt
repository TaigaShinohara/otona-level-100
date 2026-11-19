package com.taigashinohara.otonalevel100

/** Adult level 1..100 and its XP curve (design v1.1 §1). Levels never go down. */
object LevelSystem {
    const val MAX_LEVEL = 100
    const val XP_NEW_CORRECT = 10
    const val XP_REVIEW_CORRECT = 5
    const val XP_STREAK_BONUS = 5

    /** XP needed to advance from [level] to level+1. */
    fun xpToNext(level: Int): Int = when {
        level >= MAX_LEVEL -> 0
        level < 10 -> 50
        level < 50 -> 80
        level < 85 -> 120
        else -> 160
    }

    /** Cumulative XP at which [level] starts (level 1 starts at 0). */
    fun cumulativeXp(level: Int): Int {
        var sum = 0
        for (l in 1 until level.coerceIn(1, MAX_LEVEL)) sum += xpToNext(l)
        return sum
    }

    fun levelForXp(xp: Int): Int {
        var level = 1
        while (level < MAX_LEVEL && xp >= cumulativeXp(level + 1)) level++
        return level
    }

    /** Progress 0..100 inside the current level. */
    fun progressInLevel(xp: Int): Int {
        val level = levelForXp(xp)
        if (level >= MAX_LEVEL) return 100
        val base = cumulativeXp(level)
        val span = xpToNext(level)
        return ((xp - base) * 100 / span).coerceIn(0, 100)
    }

    fun xpToNextLevel(xp: Int): Int {
        val level = levelForXp(xp)
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
