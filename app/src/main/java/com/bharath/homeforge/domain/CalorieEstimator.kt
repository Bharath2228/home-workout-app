package com.bharath.homeforge.domain

object CalorieEstimator {

    private const val MIN_MINUTES = 1.0
    private const val MAX_MINUTES = 180.0

    /**
     * Resistance-training intensity by goal; published ranges run roughly 3.5 to 6 MET.
     * Strength work rests longer between heavier sets (lower average effort over time), weight-loss
     * work rests less and keeps the heart rate up, and muscle gain sits in between.
     */
    private fun met(goal: Goal): Double = when (goal) {
        Goal.STRENGTH -> 3.5
        Goal.MUSCLE_GAIN -> 4.0
        Goal.WEIGHT_LOSS -> 5.0
    }

    /** Active calories only: the resting share (1 MET) is excluded. Rough estimate, not a measurement. */
    fun activeKcal(bodyWeightKg: Double, durationMinutes: Double, goal: Goal = Goal.MUSCLE_GAIN): Double {
        val minutes = durationMinutes.coerceIn(MIN_MINUTES, MAX_MINUTES)
        return (met(goal) - 1.0) * bodyWeightKg * minutes / 60.0
    }
}
