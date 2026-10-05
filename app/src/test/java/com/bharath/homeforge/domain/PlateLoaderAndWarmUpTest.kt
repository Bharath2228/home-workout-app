package com.bharath.homeforge.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PlateLoaderAndWarmUpTest {

    private val equipment = Equipment.Default.copy(rodWeightKg = 1.0)

    @Test
    fun everyAchievableWeightHasAPlateLoadThatAddsUp() {
        Rig.entries.forEach { rig ->
            LoadCalculator.achievableWeights(equipment, rig).forEach { target ->
                val plates = PlateLoader.platesFor(equipment, rig, target)
                assertNotNull("$rig $target", plates)
                val total = when (rig) {
                    Rig.BARBELL -> 2 * equipment.rodWeightKg + 2 * plates!!.sum()
                    else -> equipment.rodWeightKg + plates!!.sum()
                }
                assertEquals("$rig $target", target, total, 1e-9)
            }
        }
    }

    @Test
    fun usesTheFewestPlates() {
        val plates = PlateLoader.platesFor(equipment.copy(rodWeightKg = 0.0), Rig.DUMBBELL_PAIR, 8.0)!!
        assertEquals(3, plates.size)
    }

    @Test
    fun unbuildableWeightIsNull() {
        assertNull(PlateLoader.platesFor(equipment, Rig.BARBELL, 3.0))
        assertNull(PlateLoader.platesFor(equipment, Rig.DUMBBELL_PAIR, 0.2))
    }

    @Test
    fun describeNamesTheSide() {
        assertTrue(PlateLoader.describe(equipment, Rig.BARBELL, 14.0)!!.startsWith("Per side"))
        assertTrue(PlateLoader.describe(equipment, Rig.DUMBBELL_PAIR, 6.0)!!.startsWith("Per dumbbell"))
        assertEquals("Per dumbbell: no plates", PlateLoader.describe(equipment, Rig.DUMBBELL_PAIR, 1.0))
    }

    private val squat = Exercise("Barbell back squat", Movement.SQUAT, Rig.BARBELL, 30.0)

    @Test
    fun warmUpRampsUpBelowTheWorkingWeightAndIsLoadable() {
        val sets = WarmUp.sets(equipment, squat, 30.0)
        assertTrue(sets.isNotEmpty())
        assertEquals(sets.sortedBy { it.weightKg }, sets)
        val options = LoadCalculator.achievableWeights(equipment, Rig.BARBELL)
        sets.forEach { set ->
            assertTrue(set.weightKg < 30.0)
            assertTrue(options.any { Math.abs(it - set.weightKg) < 1e-9 })
        }
        assertEquals(sets.map { it.weightKg }.toSet().size, sets.size)
    }

    @Test
    fun noWarmUpForLightWeightsIsolationOrBodyweight() {
        assertTrue(WarmUp.sets(equipment, squat, 8.0).isEmpty())
        val curl = Exercise("Curl", Movement.BICEP, Rig.DUMBBELL_PAIR, 8.0)
        assertTrue(WarmUp.sets(equipment, curl, 20.0).isEmpty())
        val pushUp = Exercise("Push-up", Movement.HORIZONTAL_PUSH, null)
        assertTrue(WarmUp.sets(equipment, pushUp, 20.0).isEmpty())
    }
}
