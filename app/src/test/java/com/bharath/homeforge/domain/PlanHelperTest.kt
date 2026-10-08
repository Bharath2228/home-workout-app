package com.bharath.homeforge.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlanHelperTest {

    @Test
    fun noHistoryStartsAtDayOne() {
        assertEquals(0, PlanHelper.nextDay(Split.FULL_BODY, null))
    }

    @Test
    fun nextDayAdvancesAndWraps() {
        assertEquals(1, PlanHelper.nextDay(Split.FULL_BODY, 0))
        assertEquals(0, PlanHelper.nextDay(Split.FULL_BODY, 2))
        assertEquals(0, PlanHelper.nextDay(Split.BODY_PART_SPLIT, 5))
        assertEquals(4, PlanHelper.nextDay(Split.BODY_PART_SPLIT, 3))
    }

    @Test
    fun everyProgramDayHasAFocusLine() {
        Split.entries.forEach { split ->
            split.dayNames.indices.forEach { day ->
                val text = PlanHelper.focusText(split, day)
                assertTrue("$split $day", text.startsWith("Trains: ") && text.length > "Trains: ".length)
            }
        }
    }

    @Test
    fun focusNamesTheRightMuscles() {
        val chest = PlanHelper.focusText(Split.BODY_PART_SPLIT, 0)
        assertTrue(chest.contains("chest") && chest.contains("triceps"))
        val legs = PlanHelper.focusText(Split.BODY_PART_SPLIT, 2)
        assertTrue(legs.contains("quads") && legs.contains("calves"))
        assertFalse(legs.contains("chest"))
    }

    @Test
    fun theCoreDayIsNotAProgramAndEveryPlanIsDescribed() {
        assertFalse(Split.CORE_DAY in Split.programs)
        Split.entries.forEach {
            assertTrue(it.tagline.isNotBlank() && it.bestFor.isNotBlank() && it.schedule.isNotBlank())
        }
    }
}
