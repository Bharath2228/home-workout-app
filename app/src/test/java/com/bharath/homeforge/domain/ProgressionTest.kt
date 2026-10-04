package com.bharath.homeforge.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ProgressionTest {

    private val equipment = Equipment.Default.copy(rodWeightKg = 0.0)
    private val curl = Exercise("Dumbbell curl", Movement.BICEP, Rig.DUMBBELL_PAIR, 5.0)

    @Test
    fun noHistory_usesStartingWeight() {
        val w = Progression.suggestedWeight(equipment, curl, emptyList(), 3, 8..10)
        assertEquals(5.0, w!!, 1e-9)
    }

    @Test
    fun allSetsHitTop_movesUp() {
        val last = List(3) { SetResult(5.0, 10) }
        val w = Progression.suggestedWeight(equipment, curl, last, 3, 8..10)
        assertEquals(LoadCalculator.nextUp(equipment, Rig.DUMBBELL_PAIR, 5.0)!!, w!!, 1e-9)
    }

    @Test
    fun missedReps_staysSame() {
        val last = listOf(SetResult(5.0, 10), SetResult(5.0, 9), SetResult(5.0, 8))
        val w = Progression.suggestedWeight(equipment, curl, last, 3, 8..10)
        assertEquals(5.0, w!!, 1e-9)
    }

    @Test
    fun fewerSetsThanPlanned_staysSame() {
        val last = listOf(SetResult(5.0, 10), SetResult(5.0, 10))
        val w = Progression.suggestedWeight(equipment, curl, last, 3, 8..10)
        assertEquals(5.0, w!!, 1e-9)
    }

    @Test
    fun bodyweight_returnsNull() {
        val pushUp = Exercise("Push-up", Movement.HORIZONTAL_PUSH, null)
        assertNull(Progression.suggestedWeight(equipment, pushUp, emptyList(), 3, 8..10))
    }
}
