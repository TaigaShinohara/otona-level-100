package com.taigashinohara.otonalevel100

import java.time.DayOfWeek
import java.time.LocalDate

/**
 * Calendar events for the seasonal common-knowledge feature (v0.15.0;
 * design: docs/季節の常識_設計案.md). The calendar supplies the reason to
 * open the app: during an event's window, the home shows a quiet
 * "今日は何の日" card and one of the day's new questions is a seasonal one.
 * Facts only, no hype (the shared 外の理由 principles). Windows are computed
 * per year, floating days (nth weekday) included; no network, all on-device.
 */
data class SeasonalEvent(
    val id: String,
    val name: String,
    val note: String,
    val questionIds: List<String>,
)

object Seasonal {
    private data class WindowSpec(val start: (Int) -> LocalDate, val end: (Int) -> LocalDate)

    private fun fixed(month: Int, day: Int): (Int) -> LocalDate = { y -> LocalDate.of(y, month, day) }

    private fun nthWeekday(month: Int, dow: DayOfWeek, n: Int): (Int) -> LocalDate = { y ->
        val first = LocalDate.of(y, month, 1)
        val shift = (dow.value - first.dayOfWeek.value + 7) % 7
        first.plusDays((shift + (n - 1) * 7).toLong())
    }

    private val specs: Map<String, WindowSpec> = mapOf(
        "oseibo" to WindowSpec(fixed(11, 25), fixed(12, 20)),
        // Wraps the year end: Dec 21 of [year] .. Jan 7 of [year]+1.
        "nenmatsu" to WindowSpec(fixed(12, 21), { y -> LocalDate.of(y + 1, 1, 7) }),
        "seijin" to WindowSpec(
            { y -> nthWeekday(1, DayOfWeek.MONDAY, 2)(y).minusDays(7) },
            nthWeekday(1, DayOfWeek.MONDAY, 2)),
        "setsubun" to WindowSpec(fixed(1, 27), fixed(2, 3)),
        "shinkoku" to WindowSpec(fixed(2, 10), fixed(3, 15)),
        "hinamatsuri" to WindowSpec(fixed(2, 25), fixed(3, 3)),
    )

    val events: List<SeasonalEvent> = listOf(
        SeasonalEvent("oseibo", "お歳暮シーズン",
            "お歳暮は、贈るなら12月上旬までが目安。のしの表書きは「お歳暮」、水引は紅白の蝶結びが基本です。",
            listOf("oseibo1", "oseibo2", "oseibo3", "oseibo4")),
        SeasonalEvent("nenmatsu", "年末年始",
            "年賀状は12月25日までの投函が目安です（日本郵便の案内）。松の内は1月7日頃まで。",
            listOf("nen1", "nen2", "nen3", "nen4")),
        SeasonalEvent("seijin", "成人の日",
            "成人の日は1月の第2月曜日。成年年齢は18歳ですが、飲酒・喫煙は20歳からです。",
            listOf("seijin1", "seijin2", "seijin3", "seijin4")),
        SeasonalEvent("setsubun", "節分",
            "節分は立春の前日。「季節の分かれ目」という意味の言葉です。",
            listOf("setsubun1", "setsubun2", "setsubun3", "setsubun4")),
        SeasonalEvent("shinkoku", "確定申告シーズン",
            "所得税の確定申告は2月16日から3月15日まで（国税庁）。還付申告は1月1日からできます。",
            listOf("shinkoku1", "shinkoku2", "shinkoku3", "shinkoku4")),
        SeasonalEvent("hinamatsuri", "ひな祭り",
            "3月3日は上巳の節句、桃の節句。ひし餅の色は桃・白・緑です。",
            listOf("hina1", "hina2", "hina3", "hina4")),
    )

    /** The window [start, end] (inclusive) of an event for the season starting in [year]. */
    fun windowOf(eventId: String, year: Int): Pair<LocalDate, LocalDate>? {
        val spec = specs[eventId] ?: return null
        return spec.start(year) to spec.end(year)
    }

    /** The active event on [date] with its window; when several overlap, the one ending soonest wins. */
    fun activeOn(date: LocalDate): Pair<SeasonalEvent, Pair<LocalDate, LocalDate>>? {
        var best: Pair<SeasonalEvent, Pair<LocalDate, LocalDate>>? = null
        for (ev in events) {
            for (y in listOf(date.year - 1, date.year)) {
                val w = windowOf(ev.id, y) ?: continue
                if (!date.isBefore(w.first) && !date.isAfter(w.second)) {
                    if (best == null || w.second.isBefore(best.second.second)) best = ev to w
                }
            }
        }
        return best
    }

    fun eventOf(questionId: String): SeasonalEvent? = events.firstOrNull { questionId in it.questionIds }
}
