package com.taigashinohara.otonalevel100

import android.content.Context
import android.content.SharedPreferences

/** On-device progress: XP, per-question Leitner state, totals. No account, no network. */
class ProgressStore(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("otona_progress", Context.MODE_PRIVATE)

    var xp: Int
        get() = prefs.getInt("xp", 0)
        set(v) = prefs.edit().putInt("xp", v).apply()

    var totalAnswered: Int
        get() = prefs.getInt("total_answered", 0)
        set(v) = prefs.edit().putInt("total_answered", v).apply()

    var totalCorrect: Int
        get() = prefs.getInt("total_correct", 0)
        set(v) = prefs.edit().putInt("total_correct", v).apply()

    var sessionsDone: Int
        get() = prefs.getInt("sessions_done", 0)
        set(v) = prefs.edit().putInt("sessions_done", v).apply()

    /** Epoch day of the last completed daily set, -1 if none. */
    var lastDailyDay: Long
        get() = prefs.getLong("last_daily_day", -1L)
        set(v) = prefs.edit().putLong("last_daily_day", v).apply()

    /** Whether the review-mechanics intro has been shown in full (UX3-05). */
    var reviewIntroSeen: Boolean
        get() = prefs.getBoolean("review_intro_seen", false)
        set(v) = prefs.edit().putBoolean("review_intro_seen", v).apply()

    /**
     * Highest level ever reached (design v1.3 §3.5 migration rule): when the
     * XP curve changes, levels are recomputed but never lowered, so the
     * reached level is kept as a floor.
     */
    var levelFloor: Int
        get() = prefs.getInt("level_floor", 1)
        set(v) = prefs.edit().putInt("level_floor", v).apply()

    fun stateFor(id: String): ReviewLogic.State? {
        val raw = prefs.getString("q_$id", null) ?: return null
        val parts = raw.split(",")
        if (parts.size != 3) return null
        // Guarded parse (v0.9.0 audit): corrupted data must not crash the app.
        val box = parts[0].toIntOrNull() ?: return null
        val due = parts[1].toLongOrNull() ?: return null
        val streak = parts[2].toIntOrNull() ?: return null
        return ReviewLogic.State(box, due, streak)
    }

    fun saveState(id: String, state: ReviewLogic.State) {
        prefs.edit().putString("q_$id", "${state.box},${state.dueDay},${state.streak}").apply()
    }

    fun allStates(): Map<String, ReviewLogic.State> =
        Questions.all.mapNotNull { q -> stateFor(q.id)?.let { q.id to it } }.toMap()

    // --- Session persistence (UX review IR-4: a session must survive rotation
    // and tab/back interruptions, so the result screen is always reachable). ---

    /** One session question with its (possibly shuffled) choice order. */
    data class SavedItem(val qid: String, val order: List<Int>)

    data class SavedSession(
        val items: List<SavedItem>,
        val index: Int,
        val correct: Int,
        val xp: Int,
        val streak: Int,
        val startXp: Int,
        val mode: Int, // 0 daily, 1 weak-only, 2 due-review
        val answered: Boolean,
        val chosen: Int, // shuffled choice index, or card: 1 remembered / 0 not; -1 none
        val counted: Boolean
    )

    fun saveSession(s: SavedSession) {
        prefs.edit()
            .putString("s_items", s.items.joinToString(";") { it.qid + ":" + it.order.joinToString(",") })
            .putInt("s_index", s.index).putInt("s_correct", s.correct).putInt("s_xp", s.xp)
            .putInt("s_streak", s.streak).putInt("s_startXp", s.startXp).putInt("s_mode", s.mode)
            .putBoolean("s_answered", s.answered).putInt("s_chosen", s.chosen)
            .putBoolean("s_counted", s.counted)
            .apply()
    }

    fun loadSession(): SavedSession? {
        val raw = prefs.getString("s_items", null) ?: return null
        if (raw.isBlank()) return null
        val items = raw.split(";").mapNotNull { part ->
            val kv = part.split(":")
            if (kv.size != 2) return@mapNotNull null
            val order = if (kv[1].isBlank()) emptyList()
                else kv[1].split(",").mapNotNull { it.toIntOrNull() }
            SavedItem(kv[0], order)
        }
        if (items.isEmpty()) return null
        return SavedSession(
            items,
            prefs.getInt("s_index", 0), prefs.getInt("s_correct", 0), prefs.getInt("s_xp", 0),
            prefs.getInt("s_streak", 0), prefs.getInt("s_startXp", 0), prefs.getInt("s_mode", 0),
            prefs.getBoolean("s_answered", false), prefs.getInt("s_chosen", -1),
            prefs.getBoolean("s_counted", false)
        )
    }

    fun clearSession() {
        prefs.edit()
            .remove("s_items").remove("s_index").remove("s_correct").remove("s_xp")
            .remove("s_streak").remove("s_startXp").remove("s_mode")
            .remove("s_answered").remove("s_chosen").remove("s_counted")
            .apply()
    }

    fun hasSession(): Boolean = prefs.getString("s_items", null)?.isNotBlank() == true

    companion object {
        /** Local calendar day (IR2-1). Stored dueDay values from before this
         *  change were UTC days, so they may shift by at most one day, once. */
        fun today(): Long = LocalDay.today()
    }
}
