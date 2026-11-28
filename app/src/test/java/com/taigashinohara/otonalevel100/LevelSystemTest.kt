package com.taigashinohara.otonalevel100

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LevelSystemTest {
    @Test fun reviewXpEqualsNewXp() {
        // v0.18.0 (design review 案2): mastery must not pay less.
        assertEquals(LevelSystem.XP_NEW_CORRECT, LevelSystem.XP_REVIEW_CORRECT)
    }

    @Test fun level1AtZeroXp() {
        assertEquals(1, LevelSystem.levelForXp(0))
        assertEquals("見習いオトナ", LevelSystem.titleForLevel(1))
    }

    @Test fun earlyLevelsAreFast() {
        // Lv1 -> Lv2 needs 20 XP (v0.19.0 curve: about 2 correct answers).
        assertEquals(20, LevelSystem.xpToNext(1))
        assertEquals(2, LevelSystem.levelForXp(20))
        assertEquals(1, LevelSystem.levelForXp(19))
    }

    @Test fun level100ReachableAndCapped() {
        val total = LevelSystem.totalXpToMax()
        assertEquals(100, LevelSystem.levelForXp(total))
        assertEquals(100, LevelSystem.levelForXp(total + 100000))
        assertEquals("オトナの達人", LevelSystem.titleForLevel(100))
        // Design target (v0.19.0): about 3 months of daily play to max.
        assertTrue("total=$total", total in 3800..4600)
    }

    @Test fun progressStaysInRange() {
        for (xp in listOf(0, 49, 50, 500, 3650, 9000)) {
            val p = LevelSystem.progressInLevel(xp)
            assertTrue("xp=$xp progress=$p", p in 0..100)
        }
    }

    @Test fun titlesAdvance() {
        assertEquals("駆け出しオトナ", LevelSystem.titleForLevel(10))
        assertEquals("一人前オトナ", LevelSystem.titleForLevel(45))
        assertEquals("頼れるオトナ", LevelSystem.titleForLevel(65))
        assertEquals("教養あるオトナ", LevelSystem.titleForLevel(85))
    }

    @Test fun curveReachesMaxInAboutThreeMonths() {
        // v0.19.0 (user request): the top rank in about 3 months of daily
        // play. Bands still match the title bands; total 4,140 XP is about
        // 92 days at ~45 XP/day (one daily set, most answers correct).
        assertEquals(20, LevelSystem.xpToNext(1))
        assertEquals(28, LevelSystem.xpToNext(10))
        assertEquals(36, LevelSystem.xpToNext(25))
        assertEquals(44, LevelSystem.xpToNext(45))
        assertEquals(52, LevelSystem.xpToNext(65))
        assertEquals(60, LevelSystem.xpToNext(85))
        assertEquals(180, LevelSystem.cumulativeXp(10))
        assertEquals(600, LevelSystem.cumulativeXp(25))
        assertEquals(1320, LevelSystem.cumulativeXp(45))
        assertEquals(2200, LevelSystem.cumulativeXp(65))
        assertEquals(3240, LevelSystem.cumulativeXp(85))
        assertEquals(4140, LevelSystem.totalXpToMax())
    }

    @Test fun levelFloorNeverLowersLevel() {
        // Migration rule (v1.3 §3.5): a reached level is kept as a floor.
        assertEquals(50, LevelSystem.levelForXp(0, floor = 50))
        assertEquals(50, LevelSystem.levelForXp(1000, floor = 50))
        assertTrue(LevelSystem.progressInLevel(1000, floor = 50) in 0..100)
        assertTrue(LevelSystem.xpToNextLevel(1000, floor = 50) > 0)
        assertEquals(100, LevelSystem.levelForXp(LevelSystem.totalXpToMax(), floor = 50))
    }
}

class PraiseTest {
    @Test fun correctPraiseRotatesAndCelebratesStreaks() {
        assertEquals("すごい！", Praise.forCorrect(1, 0))
        assertEquals("その調子！", Praise.forCorrect(1, 1))
        assertTrue(Praise.forCorrect(2, 5).contains("2連続正解"))
        assertTrue(Praise.forCorrect(3, 0).contains("3連続正解"))
    }

    @Test fun cardPraiseRotates() {
        assertEquals("覚えてた！すごい！", Praise.forCard(0))
        assertTrue(Praise.forCard(1).contains("思い出せた"))
    }

    @Test fun resultLineIsWarmAtEveryScore() {
        assertEquals("全問正解！完璧な1セット！", Praise.resultLine(5, 5))
        assertTrue(Praise.resultLine(4, 5).contains("あと1問"))
        assertTrue(Praise.resultLine(3, 5).isNotBlank())
        assertTrue(Praise.resultLine(1, 5).isNotBlank())
        assertTrue(Praise.resultLine(0, 5).contains("伸びしろ"))
    }

    @Test fun confettiScalesWithScore() {
        assertEquals(10, Praise.confettiPieces(0))
        assertEquals(49, Praise.confettiPieces(5))
        assertEquals(35, Praise.confettiPieces(3))
    }
}

class OwlGrowthTest {
    @Test fun accessoriesArriveAt40And50() {
        // v0.18.0 (案5): small rewards inside the Lv.30 -> 60 stretch.
        assertEquals(0, OwlGrowth.accessoryFor(39))
        assertEquals(1, OwlGrowth.accessoryFor(40))
        assertEquals(1, OwlGrowth.accessoryFor(49))
        assertEquals(2, OwlGrowth.accessoryFor(50))
        assertEquals(2, OwlGrowth.accessoryFor(100))
    }

    @Test fun stagesFollowLevel() {
        assertEquals(0, OwlGrowth.stageForLevel(1))
        assertEquals(0, OwlGrowth.stageForLevel(4))
        assertEquals(1, OwlGrowth.stageForLevel(5))
        assertEquals(1, OwlGrowth.stageForLevel(14))
        assertEquals(2, OwlGrowth.stageForLevel(15))
        assertEquals(2, OwlGrowth.stageForLevel(29))
        assertEquals(3, OwlGrowth.stageForLevel(30))
        assertEquals(3, OwlGrowth.stageForLevel(59))
        assertEquals(4, OwlGrowth.stageForLevel(60))
        assertEquals(4, OwlGrowth.stageForLevel(100))
    }

    @Test fun branchAndNextStage() {
        assertTrue(!OwlGrowth.canBranch(29))
        assertTrue(OwlGrowth.canBranch(30))
        assertEquals(5, OwlGrowth.nextStageLevel(1))
        assertEquals(15, OwlGrowth.nextStageLevel(5))
        assertEquals(30, OwlGrowth.nextStageLevel(15))
        assertEquals(60, OwlGrowth.nextStageLevel(30))
        assertEquals(null, OwlGrowth.nextStageLevel(60))
        assertEquals("まんまるオトナ", OwlGrowth.stageName(4, OwlGrowth.PATH_CUTE))
        assertEquals("りりしいオトナ", OwlGrowth.stageName(4, OwlGrowth.PATH_COOL))
        assertEquals("オトナ", OwlGrowth.stageName(3, OwlGrowth.PATH_NONE))
    }
}

class SeasonalTest {
    private fun active(y: Int, m: Int, d: Int) = Seasonal.activeOn(java.time.LocalDate.of(y, m, d))?.first?.id

    @Test fun windowsFollowTheCalendar() {
        assertEquals("oseibo", active(2026, 11, 25))
        assertEquals("oseibo", active(2026, 12, 10))
        assertEquals("oseibo", active(2026, 12, 20))
        assertEquals("nenmatsu", active(2026, 12, 21))
        assertEquals("nenmatsu", active(2026, 12, 31))
        assertEquals("nenmatsu", active(2027, 1, 5))
        assertEquals("nenmatsu", active(2027, 1, 7))
        // 2027 Coming-of-Age Day is Jan 11 (second Monday); window starts a week before.
        assertEquals("seijin", active(2027, 1, 8))
        assertEquals("seijin", active(2027, 1, 11))
        assertEquals("setsubun", active(2027, 2, 2))
        assertEquals("shinkoku", active(2027, 2, 20))
        // Overlap: hinamatsuri (ends Mar 3) wins over shinkoku (ends Mar 15).
        assertEquals("hinamatsuri", active(2027, 3, 1))
        assertEquals("shinkoku", active(2027, 3, 10))
        assertEquals(null, active(2027, 6, 1))
        assertEquals(null, active(2027, 1, 20))
    }

    @Test fun seasonalPoolIsValidAndSeparate() {
        assertEquals(24, Questions.seasonal.size)
        assertEquals(204, Questions.all.size)
        assertEquals(228, Questions.pool.size)
        Questions.seasonal.forEach { q ->
            assertEquals("q=${q.id}", 4, q.choices.size)
            assertTrue("q=${q.id}", q.answerIndex in q.choices.indices)
            assertTrue("q=${q.id}", q.category in Questions.categories)
            assertTrue("q=${q.id}", Seasonal.eventOf(q.id) != null)
        }
        Seasonal.events.forEach { ev ->
            assertEquals("ev=${ev.id}", 4, ev.questionIds.size)
            ev.questionIds.forEach { id -> assertTrue("id=$id", Questions.byId(id) != null) }
        }
        assertEquals(Questions.pool.size, Questions.pool.map { it.id }.distinct().size)
    }
}

class BookshelfTest {
    @Test fun booksFillWithGraduationRatio() {
        assertEquals(0, Bookshelf.booksFor(0, 20))
        assertEquals(5, Bookshelf.booksFor(10, 20))
        assertEquals(9, Bookshelf.booksFor(19, 20))
        assertEquals(10, Bookshelf.booksFor(20, 20))
        assertEquals(5, Bookshelf.booksFor(12, 24))
        assertEquals(0, Bookshelf.booksFor(0, 0))
    }

    @Test fun completeOnlyAtFullGraduation() {
        assertTrue(Bookshelf.isComplete(20, 20))
        assertTrue(Bookshelf.isComplete(24, 24))
        assertFalse(Bookshelf.isComplete(19, 20))
        assertFalse(Bookshelf.isComplete(0, 0))
    }
}

class SessionDesignTest {
    private fun q(id: String, difficulty: Int) = Question(
        id, "敬語", QType.QUIZ, "p", listOf("a", "b", "c", "d"), 0,
        explanation = "e", scene = "s", source = "src", difficulty = difficulty
    )

    @Test fun mixFollowsLevelBands() {
        assertEquals(Triple(70, 30, 0), SessionDesign.difficultyMix(1))
        assertEquals(Triple(40, 50, 10), SessionDesign.difficultyMix(10))
        assertEquals(Triple(20, 55, 25), SessionDesign.difficultyMix(25))
        assertEquals(Triple(10, 50, 40), SessionDesign.difficultyMix(45))
        assertEquals(Triple(0, 50, 50), SessionDesign.difficultyMix(65))
        assertEquals(Triple(0, 40, 60), SessionDesign.difficultyMix(85))
    }

    @Test fun pickNewRespectsMixWhenPoolIsPlentiful() {
        val pool = (1..10).map { q("e$it", 1) } + (1..10).map { q("n$it", 2) } + (1..10).map { q("h$it", 3) }
        val low = SessionDesign.pickNew(pool, 3, level = 1, random = kotlin.random.Random(7))
        assertEquals(3, low.size)
        assertEquals(2, low.count { it.difficulty == 1 })
        assertEquals(1, low.count { it.difficulty == 2 })
        assertEquals(0, low.count { it.difficulty == 3 })
        val high = SessionDesign.pickNew(pool, 3, level = 90, random = kotlin.random.Random(7))
        assertEquals(2, high.count { it.difficulty == 3 })
        assertEquals(1, high.count { it.difficulty == 2 })
    }

    @Test fun pickNewFallsBackToClosestDifficulty() {
        // No hard questions in the pool: a high-level pick must still fill up.
        val pool = (1..5).map { q("e$it", 1) } + (1..5).map { q("n$it", 2) }
        val picked = SessionDesign.pickNew(pool, 3, level = 90, random = kotlin.random.Random(7))
        assertEquals(3, picked.size)
        assertTrue(picked.all { it.difficulty <= 2 })
    }

    @Test fun wavePutsPeakFourthAndReviewAtBothEnds() {
        val r1 = q("r1", 2); val r2 = q("r2", 1)
        val f1 = q("f1", 1); val f2 = q("f2", 2); val f3 = q("f3", 3)
        val ordered = SessionDesign.orderWave(listOf(r1, r2), listOf(f1, f2, f3), kotlin.random.Random(7))
        assertEquals(5, ordered.size)
        assertEquals("r1", ordered[0].id) // opener = a review
        assertEquals("f3", ordered[3].id) // peak = hardest, at position 4
        assertEquals("r2", ordered[4].id) // closer = the other review
    }

    @Test fun gentleMixForTheFirstWeek() {
        // v0.18.0 (案7): the first session is all-easy, the rest of the
        // first week stays easy-centred, then the level bands take over.
        assertEquals(Triple(100, 0, 0), SessionDesign.difficultyMix(1, sessionsDone = 0))
        assertEquals(Triple(85, 15, 0), SessionDesign.difficultyMix(50, sessionsDone = 3))
        assertEquals(Triple(10, 50, 40), SessionDesign.difficultyMix(50, sessionsDone = 7))
        assertEquals(Triple(70, 30, 0), SessionDesign.difficultyMix(1))
    }

    @Test fun wavePeakPrefersFreshOverHardReview() {
        // v0.18.0 (案6): a difficulty-3 review the player keeps missing
        // must not become the peak; the hardest NEW question takes slot 4.
        val rHard = q("rh", 3); val rEasy = q("re", 1)
        val f1 = q("f1", 1); val f2 = q("f2", 2); val f3 = q("f3", 1)
        val ordered = SessionDesign.orderWave(listOf(rHard, rEasy), listOf(f1, f2, f3), kotlin.random.Random(7))
        assertEquals(5, ordered.size)
        assertEquals("f2", ordered[3].id)
    }

    @Test fun waveWithoutReviewOpensAndClosesOnEasy() {
        val f1 = q("f1", 1); val f2 = q("f2", 1); val f3 = q("f3", 2); val f4 = q("f4", 2); val f5 = q("f5", 3)
        val ordered = SessionDesign.orderWave(emptyList(), listOf(f1, f2, f3, f4, f5), kotlin.random.Random(7))
        assertEquals(5, ordered.size)
        assertEquals(1, ordered[0].difficulty)
        assertEquals("f5", ordered[3].id)
        assertEquals(1, ordered[4].difficulty)
    }
}

class ReviewLogicTest {
    @Test fun wrongAnswerDropsOneBoxDueTomorrow() {
        // v0.18.0 (design review 案1): a miss costs one box, not the ladder.
        val s = ReviewLogic.onAnswer(ReviewLogic.State(3, 100L, 2), correct = false, today = 100L)
        assertEquals(2, s.box)
        assertEquals(101L, s.dueDay)
        assertEquals(0, s.streak)
        assertTrue(ReviewLogic.isWeak(s))
        val s4 = ReviewLogic.onAnswer(ReviewLogic.State(4, 100L, 3), correct = false, today = 100L)
        assertEquals(3, s4.box)
        // Box 1 is the floor.
        val s1 = ReviewLogic.onAnswer(ReviewLogic.State(1, 100L, 0), correct = false, today = 100L)
        assertEquals(1, s1.box)
    }

    @Test fun correctAnswersWidenIntervals() {
        var s = ReviewLogic.initial(0L)
        s = ReviewLogic.onAnswer(s, correct = true, today = 0L)
        assertEquals(1L, s.dueDay) // box1 -> +1 day
        s = ReviewLogic.onAnswer(s, correct = true, today = 1L)
        assertEquals(4L, s.dueDay) // box2 -> +3 days
        s = ReviewLogic.onAnswer(s, correct = true, today = 4L)
        assertEquals(11L, s.dueDay) // box3 -> +7 days
    }

    @Test fun threeConsecutiveCorrectGraduates() {
        var s = ReviewLogic.initial(0L)
        repeat(2) { s = ReviewLogic.onAnswer(s, correct = true, today = it.toLong()) }
        assertFalse(ReviewLogic.isGraduated(s))
        s = ReviewLogic.onAnswer(s, correct = true, today = 2L)
        assertTrue(ReviewLogic.isGraduated(s))
    }

    @Test fun streakAloneDoesNotGraduateBeforeBox3() {
        // IR-6: 3 in a row while still in box 1-2 must not graduate.
        assertFalse(ReviewLogic.isGraduated(ReviewLogic.State(2, 10L, 3)))
        assertTrue(ReviewLogic.isGraduated(ReviewLogic.State(3, 10L, 3)))
    }

    @Test fun dueOnlyAfterAnswered() {
        assertFalse(ReviewLogic.isDue(ReviewLogic.initial(5L), 5L))
        val s = ReviewLogic.onAnswer(ReviewLogic.initial(5L), correct = false, today = 5L)
        assertFalse(ReviewLogic.isDue(s, 5L))
        assertTrue(ReviewLogic.isDue(s, 6L))
    }

    @Test fun graduatedIsNotDueButBecomesSpotCheckDue() {
        // IR2-2: graduation removes the question from the regular review pool;
        // when its dueDay arrives it is a spot check instead.
        val g = ReviewLogic.State(box = 3, dueDay = 10L, streak = 3)
        assertTrue(ReviewLogic.isGraduated(g))
        assertFalse(ReviewLogic.isDue(g, 10L))
        assertFalse(ReviewLogic.isSpotCheckDue(g, 9L))
        assertTrue(ReviewLogic.isSpotCheckDue(g, 10L))
    }

    @Test fun spotCheckCorrectKeepsGraduationAndReschedules() {
        val g = ReviewLogic.State(box = 5, dueDay = 10L, streak = 5)
        val s = ReviewLogic.onAnswer(g, correct = true, today = 10L)
        assertTrue(ReviewLogic.isGraduated(s))
        assertEquals(31L, s.dueDay) // box5 interval: +21 days
    }

    @Test fun spotCheckMissKeepsGraduationAndAsksConfirmation() {
        // v0.18.0 (design review 案1): a missed spot check never revokes
        // graduation — it only reschedules a confirmation a few days out.
        val g = ReviewLogic.State(box = 5, dueDay = 10L, streak = 5)
        val s = ReviewLogic.onSpotCheckMiss(g, today = 10L)
        assertTrue(ReviewLogic.isGraduated(s))
        assertEquals(5, s.box)
        assertEquals(13L, s.dueDay)
        assertFalse(ReviewLogic.isDue(s, 13L))
        assertTrue(ReviewLogic.isSpotCheckDue(s, 13L))
    }
}

class LocalDayTest {
    @Test fun todayUsesLocalCalendarDay() {
        // 2026-11-18T16:30:00Z is 2026-11-19 01:30 in Asia/Tokyo:
        // the UTC epoch day is Nov 18, the local day must be Nov 19.
        val original = java.util.TimeZone.getDefault()
        try {
            java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("Asia/Tokyo"))
            val instant = java.time.Instant.parse("2026-11-18T16:30:00Z").toEpochMilli()
            val expected = java.time.LocalDate.of(2026, 11, 19).toEpochDay()
            assertEquals(expected, LocalDay.today(instant))
        } finally {
            java.util.TimeZone.setDefault(original)
        }
    }
}

class QuestionsTest {
    @Test fun poolHasFourPerCategoryAndOneCardMinimum() {
        // v0.19.0: pool grew to 204 (keigo 44, others 40).
        assertEquals(204, Questions.all.size)
        val expected = mapOf("敬語" to 44, "マナー" to 40, "漢字" to 40, "標識・交通" to 40, "社会・お金" to 40)
        Questions.categories.forEach { cat ->
            assertEquals("cat=$cat", expected[cat], Questions.all.count { it.category == cat })
        }
        // Figure questions exist and their keys are known to SignView.
        val figures = Questions.all.filter { it.figure.isNotEmpty() }
        assertEquals(6, figures.size)
        assertTrue(figures.all { it.figure in setOf("sign_stop", "sign_arrow", "sign_warning", "sign_noparking", "sign_speed", "sign_oneway") })
        assertTrue(Questions.all.any { it.type == QType.CARD })
        Questions.all.filter { it.type == QType.QUIZ }.forEach { q ->
            assertEquals("q=${q.id}", 4, q.choices.size)
            assertTrue(q.answerIndex in q.choices.indices)
        }
        assertEquals(Questions.all.size, Questions.all.map { it.id }.distinct().size)
    }

    @Test fun difficultyTagsCoverThePool() {
        // Design v1.3 §3.5: every question carries a tag (1 easy, 2 normal,
        // 3 hard); the counts are the approved tagging pass of 2026-11-26.
        assertTrue(Questions.all.all { it.difficulty in 1..3 })
        assertEquals(83, Questions.all.count { it.difficulty == 1 })
        assertEquals(70, Questions.all.count { it.difficulty == 2 })
        assertEquals(51, Questions.all.count { it.difficulty == 3 })
    }

    @Test fun shuffledChoicesKeepTheSameCorrectAnswer() {
        // IR-5: shuffling must preserve the choice set and the correct answer text.
        val random = kotlin.random.Random(42)
        Questions.all.filter { it.type == QType.QUIZ }.forEach { q ->
            repeat(5) {
                val (choices, answerIndex) = shuffledChoices(q, random)
                assertEquals("q=${q.id}", q.choices.sorted(), choices.sorted())
                assertEquals("q=${q.id}", q.choices[q.answerIndex], choices[answerIndex])
            }
        }
    }
}

class SessionGrowthTest {
    @Test fun growthReportsGraduationAndBookDeltas() {
        val g = SessionGrowth.growth(
            beforeGrads = listOf(0, 10, 20),
            afterGrads = listOf(2, 10, 20),
            totals = listOf(20, 20, 20)
        )
        assertEquals(2, g[0].gradDelta)
        assertEquals(1, g[0].bookDelta) // 0 books -> 1 book (2/20)
        assertEquals(0, g[1].gradDelta)
        assertEquals(0, g[1].bookDelta)
        assertEquals(0, g[2].gradDelta)
        assertEquals(0, g[2].bookDelta)
    }

    @Test fun growthHandlesMissingAndZeroTotals() {
        val g = SessionGrowth.growth(listOf(1), listOf(3), listOf(0, 20))
        assertEquals(2, g[0].gradDelta)
        assertEquals(0, g[0].bookDelta) // total 0 -> no books either side
        assertEquals(0, g[1].gradDelta) // missing entries read as 0
    }
}
