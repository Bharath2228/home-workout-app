package com.bharath.homeforge.domain

object CalorieEstimator {

    /** Moderate-effort resistance training; published ranges run roughly 3.5 to 6 MET. */
    private const val STRENGTH_MET = 4.0
    private const val MIN_MINUTES = 1.0
    private const val MAX_MINUTES = 180.0

    /** Active calories only: the resting share (1 MET) is excluded. Rough estimate, not a measurement. */
    fun activeKcal(bodyWeightKg: Double, durationMinutes: Double): Double {
        val minutes = durationMinutes.coerceIn(MIN_MINUTES, MAX_MINUTES)
        return (STRENGTH_MET - 1.0) * bodyWeightKg * minutes / 60.0
    }
}
