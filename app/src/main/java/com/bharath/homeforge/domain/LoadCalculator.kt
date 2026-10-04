package com.bharath.homeforge.domain

import kotlin.math.abs

object LoadCalculator {

    /**
     * All total weights (kg) you can physically build for [rig], sorted ascending.
     * SINGLE_DUMBBELL: weight of the one dumbbell.
     * DUMBBELL_PAIR: weight of each dumbbell (both are loaded identically).
     * BARBELL: total bar weight (both rods joined, plates mirrored on each side).
     */
    fun achievableWeights(equipment: Equipment, rig: Rig): List<Double> {
        val rodGrams = toGrams(equipment.rodWeightKg)
        val plates = equipment.plates.filter { it.count > 0 && it.weightKg > 0 }

        return when (rig) {
            Rig.SINGLE_DUMBBELL -> {
                val sums = subsetSums(plates.map { toGrams(it.weightKg) to it.count })
                sums.map { fromGrams(rodGrams + it) }
            }
            Rig.DUMBBELL_PAIR -> {
                val sums = subsetSums(plates.map { toGrams(it.weightKg) to it.count / 2 })
                sums.map { fromGrams(rodGrams + it) }
            }
            Rig.BARBELL -> {
                val sums = subsetSums(plates.map { toGrams(it.weightKg) to it.count / 2 })
                sums.map { fromGrams(2 * rodGrams + 2 * it) }
            }
        }.sorted()
    }

    /** Nearest achievable weight to [targetKg]; ties go to the lighter weight. */
    fun snap(equipment: Equipment, rig: Rig, targetKg: Double): Double {
        val options = achievableWeights(equipment, rig)
        return options.minWith(
            compareBy<Double> { abs(it - targetKg) }.thenBy { it },
        )
    }

    /** Smallest achievable weight strictly above [currentKg], or null if already at max. */
    fun nextUp(equipment: Equipment, rig: Rig, currentKg: Double): Double? =
        achievableWeights(equipment, rig).firstOrNull { it > currentKg + 1e-9 }

    private fun subsetSums(groups: List<Pair<Int, Int>>): Set<Int> {
        var sums = setOf(0)
        for ((grams, count) in groups) {
            val next = HashSet<Int>()
            for (s in sums) {
                for (n in 0..count) next.add(s + n * grams)
            }
            sums = next
        }
        return sums
    }

    private fun toGrams(kg: Double): Int = Math.round(kg * 1000).toInt()
    private fun fromGrams(grams: Int): Double = grams / 1000.0
}
