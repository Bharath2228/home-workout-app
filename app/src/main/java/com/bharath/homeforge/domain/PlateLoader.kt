package com.bharath.homeforge.domain

object PlateLoader {

    /**
     * Fewest plates (heaviest first) for one dumbbell, or one side of the barbell, that reach [targetKg].
     * Null if that exact weight can't be built.
     */
    fun platesFor(equipment: Equipment, rig: Rig, targetKg: Double): List<Double>? {
        val targetGrams = grams(targetKg)
        val needed = if (rig == Rig.BARBELL) {
            if (targetGrams % 2 != 0) return null
            targetGrams / 2
        } else {
            targetGrams
        }
        if (needed < 0) return null

        val limits = equipment.plates
            .filter { it.count > 0 && it.weightKg > 0 }
            .sortedByDescending { it.weightKg }
            .map { grams(it.weightKg) to (if (rig == Rig.SINGLE_DUMBBELL) it.count else it.count / 2) }

        var best: Map<Int, List<Int>> = mapOf(0 to emptyList())
        for ((plateGrams, max) in limits) {
            val next = HashMap<Int, List<Int>>()
            for ((sum, used) in best) {
                for (n in 0..max) {
                    val total = sum + n * plateGrams
                    if (total > needed) break
                    val candidate = used + List(n) { plateGrams }
                    val existing = next[total]
                    if (existing == null || candidate.size < existing.size) next[total] = candidate
                }
            }
            best = next
        }
        return best[needed]?.map { it / 1000.0 }?.sortedDescending()
    }

    fun describe(equipment: Equipment, rig: Rig, targetKg: Double): String? {
        val plates = platesFor(equipment, rig, targetKg) ?: return null
        val list = if (plates.isEmpty()) "no plates" else plates.joinToString(" + ") { format(it) }
        return when (rig) {
            // One side of the bar; you load the identical mirror on the other side to balance it.
            Rig.BARBELL -> "Per side: $list"
            // Short for "split this evenly across both ends of the one dumbbell" — it's the total
            // for that dumbbell, not one side, but spelling that out every time doesn't fit on a
            // single line, so "Both ends" carries the distinction instead.
            Rig.DUMBBELL_PAIR -> "Both ends: $list"
            Rig.SINGLE_DUMBBELL -> "Both ends: $list"
        }
    }

    private fun format(kg: Double): String = if (kg == kg.toLong().toDouble()) kg.toLong().toString() else kg.toString()

    private fun grams(kg: Double): Int = Math.round(kg * 1000).toInt()
}
