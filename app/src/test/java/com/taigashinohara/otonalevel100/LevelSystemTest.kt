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
