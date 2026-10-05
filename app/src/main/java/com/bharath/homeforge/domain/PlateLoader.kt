package com.bharath.homeforge.domain

object PlateLoader {

    /**
     * Fewest plates (heaviest first) for one rod, or one side of the joined barbell, that reach [targetKg].
     * Null if that exact weight can't be built.
     */
    fun platesFor(equipment: Equipment, rig: Rig, targetKg: Double): List<Double>? {
        val rodGrams = grams(equipment.rodWeightKg)
        val targetGrams = grams(targetKg)
        val needed = if (rig == Rig.BARBELL) {
            val plateTotal = targetGrams - 2 * rodGrams
            if (plateTotal < 0 || plateTotal % 2 != 0) return null
            plateTotal / 2
        } else {
            targetGrams - rodGrams
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
        val label = when (rig) {
            Rig.BARBELL -> "Per side"
            Rig.DUMBBELL_PAIR -> "Per dumbbell"
            Rig.SINGLE_DUMBBELL -> "On the rod"
        }
        return if (plates.isEmpty()) "$label: no plates" else "$label: ${plates.joinToString(" + ") { format(it) }}"
    }

    private fun format(kg: Double): String = if (kg == kg.toLong().toDouble()) kg.toLong().toString() else kg.toString()

    private fun grams(kg: Double): Int = Math.round(kg * 1000).toInt()
}
