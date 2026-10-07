package com.bharath.homeforge.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LoadCalculatorTest {

    private val equipment = Equipment.Default

    @Test
    fun singleDumbbell_maxIsAllPlates() {
        val weights = LoadCalculator.achievableWeights(equipment, Rig.SINGLE_DUMBBELL)
        // 4*5 + 4*2.5 + 4*1.5 + 4*1 = 40
        assertEquals(40.0, weights.last(), 1e-9)
        assertEquals(0.0, weights.first(), 1e-9)
    }

    @Test
    fun dumbbellPair_maxIsHalfOfPlatesPerDumbbell() {
        val weights = LoadCalculator.achievableWeights(equipment, Rig.DUMBBELL_PAIR)
        assertEquals(20.0, weights.last(), 1e-9)
    }

    @Test
    fun barbell_maxIsAllPlatesMirrored() {
        val weights = LoadCalculator.achievableWeights(equipment, Rig.BARBELL)
        assertEquals(40.0, weights.last(), 1e-9)
    }

    @Test
    fun barbell_weightsAreAlwaysEvenMultiplesOfMirroredSide() {
        val weights = LoadCalculator.achievableWeights(equipment, Rig.BARBELL)
        // smallest non-zero step is 2 * 1.0kg plate
        assertEquals(2.0, weights[1], 1e-9)
        assertTrue(weights.none { it == 1.0 })
    }

    @Test
    fun dumbbellPair_allowsFineSteps() {
        val weights = LoadCalculator.achievableWeights(equipment, Rig.DUMBBELL_PAIR)
        assertTrue(weights.contains(1.0))
        assertTrue(weights.contains(2.5))
        assertTrue(weights.contains(3.5))
    }

    @Test
    fun snap_picksNearestAchievable() {
        assertEquals(7.5, LoadCalculator.snap(equipment, Rig.DUMBBELL_PAIR, 7.4), 1e-9)
    }

    @Test
    fun snap_aboveMaxClampsToMax() {
        assertEquals(20.0, LoadCalculator.snap(equipment, Rig.DUMBBELL_PAIR, 100.0), 1e-9)
    }

    @Test
    fun nextUp_returnsNextStep() {
        assertEquals(1.0, LoadCalculator.nextUp(equipment, Rig.DUMBBELL_PAIR, 0.0)!!, 1e-9)
    }

    @Test
    fun nextUp_atMaxIsNull() {
        assertNull(LoadCalculator.nextUp(equipment, Rig.DUMBBELL_PAIR, 20.0))
    }

    @Test
    fun noPlates_onlyZero() {
        val bare = Equipment(plates = emptyList())
        assertEquals(listOf(0.0), LoadCalculator.achievableWeights(bare, Rig.SINGLE_DUMBBELL))
    }
}
