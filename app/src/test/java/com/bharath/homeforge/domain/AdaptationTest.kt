package com.bharath.homeforge.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AdaptationTest {

    private val equipment = Equipment.Default.copy(rodWeightKg = 0.0)
    private val curl = Exercise("Dumbbell curl", Movement.BICEP, Rig.DUMBBELL_PAIR, 5.0)
    private val reps = 8..10

    private fun session(weight: Double, vararg repCounts: Int) = repCounts.map { SetResult(weight, it) }

    private fun suggest(history: List<List<SetResult>>, sets: Int = 3) =
        Adaptation.suggest(equipment, curl, history, sets, reps)

    @Test
    fun noHistory_usesStartingWeightWithNoReason() {
        val s = suggest(emptyList())
        assertEquals(5.0, s.weightKg!!, 1e-9)
        assertNull(s.reason)
    }

    @Test
    fun allSetsHitTop_movesUp() {
        val s = suggest(listOf(session(5.0, 10, 10, 10)))
        assertEquals(LoadCalculator.nextUp(equipment, Rig.DUMBBELL_PAIR, 5.0)!!, s.weightKg!!, 1e-9)
        assertNotNull(s.reason)
    }

    @Test
    fun repsWellAboveTarget_makesABiggerJump() {
        val up = LoadCalculator.nextUp(equipment, Rig.DUMBBELL_PAIR, 5.0)!!
        val twoUp = LoadCalculator.nextUp(equipment, Rig.DUMBBELL_PAIR, up)!!
        assertEquals(twoUp, suggest(listOf(session(5.0, 12, 12, 12))).weightKg!!, 1e-9)
    }

    @Test
    fun missedReps_holdsTheWeight() {
        val s = suggest(listOf(session(5.0, 10, 9, 8)))
        assertEquals(5.0, s.weightKg!!, 1e-9)
    }

    @Test
    fun fewerSetsThanPlanned_holdsTheWeight() {
        assertEquals(5.0, suggest(listOf(session(5.0, 10, 10))).weightKg!!, 1e-9)
    }

    @Test
    fun missedBelowRangeTwice_stepsDown() {
        val history = listOf(session(10.0, 8, 8, 6), session(10.0, 8, 7, 6))
        val s = suggest(history)
        assertEquals(LoadCalculator.nextDown(equipment, Rig.DUMBBELL_PAIR, 10.0)!!, s.weightKg!!, 1e-9)
    }

    @Test
    fun threeStalledSessions_deload() {
        val stalled = session(10.0, 9, 8, 8)
        val s = suggest(listOf(stalled, stalled, stalled))
        assertTrue(s.weightKg!! < 10.0)
        assertTrue(s.weightKg!! >= 8.0)
        assertTrue(s.reason!!.contains("deload", ignoreCase = true))
    }

    @Test
    fun differentWeightsDoNotCountAsAStall() {
        val s = suggest(listOf(session(10.0, 9, 8, 8), session(9.0, 9, 8, 8), session(8.0, 9, 8, 8)))
        assertEquals(10.0, s.weightKg!!, 1e-9)
    }

    @Test
    fun atHeaviestLoad_addsASetInstead() {
        val max = LoadCalculator.achievableWeights(equipment, Rig.DUMBBELL_PAIR).last()
        val s = suggest(listOf(session(max, 10, 10, 10)))
        assertEquals(max, s.weightKg!!, 1e-9)
        assertEquals(4, s.sets)
    }

    @Test
    fun bodyweight_raisesRepTargetWhenAllHit() {
        val pushUp = Exercise("Push-up", Movement.HORIZONTAL_PUSH, null)
        val s = Adaptation.suggest(equipment, pushUp, listOf(List(3) { SetResult(null, 10) }), 3, reps)
        assertNull(s.weightKg)
        assertEquals(10..12, s.reps)
    }

    @Test
    fun bodyweight_keepsTargetWhenMissed() {
        val pushUp = Exercise("Push-up", Movement.HORIZONTAL_PUSH, null)
        val s = Adaptation.suggest(equipment, pushUp, listOf(listOf(SetResult(null, 9), SetResult(null, 8), SetResult(null, 7))), 3, reps)
        assertEquals(reps, s.reps)
    }

    private fun hardSession(weight: Double, vararg repCounts: Int) =
        repCounts.map { SetResult(weight, it, Difficulty.HARD) }

    @Test
    fun allSetsHitButFeltHard_holdsTheWeightInstead() {
        val s = suggest(listOf(hardSession(5.0, 10, 10, 10)))
        assertEquals(5.0, s.weightKg!!, 1e-9)
        assertTrue(s.reason!!.contains("hard", ignoreCase = true))
    }

    @Test
    fun allSetsHitAndFeltEasy_makesABiggerJumpEvenWithoutExtraReps() {
        val up = LoadCalculator.nextUp(equipment, Rig.DUMBBELL_PAIR, 5.0)!!
        val twoUp = LoadCalculator.nextUp(equipment, Rig.DUMBBELL_PAIR, up)!!
        val easy = listOf(SetResult(5.0, 10, Difficulty.EASY), SetResult(5.0, 10, Difficulty.EASY), SetResult(5.0, 10, Difficulty.EASY))
        assertEquals(twoUp, suggest(listOf(easy)).weightKg!!, 1e-9)
    }

    @Test
    fun bodyweight_hitButFeltHard_holdsTheTarget() {
        val pushUp = Exercise("Push-up", Movement.HORIZONTAL_PUSH, null)
        val hard = List(3) { SetResult(null, 10, Difficulty.HARD) }
        val s = Adaptation.suggest(equipment, pushUp, listOf(hard), 3, reps)
        assertEquals(reps, s.reps)
    }
}
