package com.bharath.homeforge.domain

data class SetResult(val weightKg: Double?, val reps: Int)

object Progression {

    /**
     * Weight to use next time. With no history, the exercise's starting weight snapped to your plates.
     * If every planned set last time reached the top of the rep range, move up to the next loadable weight.
     */
    fun suggestedWeight(
        equipment: Equipment,
        exercise: Exercise,
        last: List<SetResult>,
        plannedSets: Int,
        reps: IntRange,
    ): Double? {
        val rig = exercise.rig ?: return null
        val lastWeight = last.mapNotNull { it.weightKg }.maxOrNull()
            ?: return LoadCalculator.snap(equipment, rig, exercise.startKg)

        val hitTarget = last.size >= plannedSets && last.all { it.reps >= reps.last }
        return if (hitTarget) LoadCalculator.nextUp(equipment, rig, lastWeight) ?: lastWeight else lastWeight
    }
}
