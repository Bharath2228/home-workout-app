package com.bharath.homeforge.domain

data class WarmUpSet(val weightKg: Double, val reps: Int)

data class WarmUpMove(val name: String, val detail: String)

/** A short, fixed mobility warm-up done before any training day, regardless of the day's exercises. */
object GeneralWarmUp {
    val moves: List<WarmUpMove> = listOf(
        WarmUpMove("March or jog in place", "60 seconds"),
        WarmUpMove("Arm circles", "10 forward, 10 backward"),
        WarmUpMove("Leg swings", "10 each leg, front to back"),
        WarmUpMove("Bodyweight squat", "10 reps"),
        WarmUpMove("Hip circles", "10 each direction"),
        WarmUpMove("Cat-cow", "8 reps"),
    )
}

object WarmUp {

    private val compound = setOf(
        Movement.SQUAT,
        Movement.HINGE,
        Movement.HORIZONTAL_PUSH,
        Movement.VERTICAL_PUSH,
        Movement.HORIZONTAL_PULL,
    )

    private const val MIN_WORKING_KG = 10.0

    fun eligible(exercise: Exercise): Boolean = exercise.rig != null && exercise.movement in compound

    /** Ramp of about 50%, 70% and 85% of the working weight, each snapped to a load you can build. */
    fun sets(equipment: Equipment, exercise: Exercise, workingKg: Double): List<WarmUpSet> {
        val rig = exercise.rig ?: return emptyList()
        if (!eligible(exercise) || workingKg < MIN_WORKING_KG) return emptyList()
        val seen = mutableSetOf<Double>()
        return listOf(0.5 to 8, 0.7 to 5, 0.85 to 3).mapNotNull { (fraction, reps) ->
            val weight = LoadCalculator.snap(equipment, rig, workingKg * fraction)
            if (weight <= 0.0 || weight >= workingKg || !seen.add(weight)) null else WarmUpSet(weight, reps)
        }
    }
}
