package com.taigashinohara.otonalevel100

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LevelSystemTest {
    @Test fun level1AtZeroXp() {
        assertEquals(1, LevelSystem.levelForXp(0))
        assertEquals("見習いオトナ", LevelSystem.titleForLevel(1))
    }

    @Test fun earlyLevelsAreFast() {
        // Lv1 -> Lv2 needs 50 XP (about 5 new correct answers).
        assertEquals(50, LevelSystem.xpToNext(1))
        assertEquals(2, LevelSystem.levelForXp(50))
        assertEquals(1, LevelSystem.levelForXp(49))
    }

    @Test fun level100ReachableAndCapped() {
        val total = LevelSystem.totalXpToMax()
        assertEquals(100, LevelSystem.levelForXp(total))
        assertEquals(100, LevelSystem.levelForXp(total + 100000))
        assertEquals("オトナの達人", LevelSystem.titleForLevel(100))
        // Design target: roughly 10k XP to max (about 8-10 months at 5/day).
        assertTrue("total=$total", total in 9000..13000)
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

    @Test fun smoothedCurveMatchesDesignV13() {
        // Design v1.3 §3.5: bands match the title bands, +20 XP per band.
        assertEquals(50, LevelSystem.xpToNext(1))
        assertEquals(70, LevelSystem.xpToNext(10))
        assertEquals(90, LevelSystem.xpToNext(25))
        assertEquals(110, LevelSystem.xpToNext(45))
        assertEquals(130, LevelSystem.xpToNext(65))
        assertEquals(150, LevelSystem.xpToNext(85))
        assertEquals(450, LevelSystem.cumulativeXp(10))
        assertEquals(1500, LevelSystem.cumulativeXp(25))
        assertEquals(3300, LevelSystem.cumulativeXp(45))
        assertEquals(5500, LevelSystem.cumulativeXp(65))
        assertEquals(8100, LevelSystem.cumulativeXp(85))
        assertEquals(10350, LevelSystem.totalXpToMax())
    }

    @Test fun levelFloorNeverLowersLevel() {
        // Migration rule (v1.3 §3.5): a reached level is kept as a floor.
        assertEquals(50, LevelSystem.levelForXp(0, floor = 50))
        assertEquals(50, LevelSystem.levelForXp(3400, floor = 50))
        assertTrue(LevelSystem.progressInLevel(3400, floor = 50) in 0..100)
        assertTrue(LevelSystem.xpToNextLevel(3400, floor = 50) > 0)
        assertEquals(100, LevelSystem.levelForXp(LevelSystem.totalXpToMax(), floor = 50))
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
    @Test fun wrongAnswerResetsToBox1DueTomorrow() {
        val s = ReviewLogic.onAnswer(ReviewLogic.State(3, 100L, 2), correct = false, today = 100L)
        assertEquals(1, s.box)
        assertEquals(101L, s.dueDay)
        assertEquals(0, s.streak)
        assertTrue(ReviewLogic.isWeak(s))
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

    @Test fun spotCheckWrongSendsBackToLearningCycle() {
        val g = ReviewLogic.State(box = 5, dueDay = 10L, streak = 5)
        val s = ReviewLogic.onAnswer(g, correct = false, today = 10L)
        assertFalse(ReviewLogic.isGraduated(s))
        assertEquals(1, s.box)
        assertTrue(ReviewLogic.isDue(s, 11L))
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
        // v0.8.0: pool grew to 104 (keigo 24 incl. the pilot batch, others 20).
        assertEquals(104, Questions.all.size)
        val expected = mapOf("敬語" to 24, "マナー" to 20, "漢字" to 20, "標識・交通" to 20, "社会・お金" to 20)
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
        assertEquals(48, Questions.all.count { it.difficulty == 1 })
        assertEquals(34, Questions.all.count { it.difficulty == 2 })
        assertEquals(22, Questions.all.count { it.difficulty == 3 })
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
