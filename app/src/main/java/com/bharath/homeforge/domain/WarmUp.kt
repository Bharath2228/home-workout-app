package com.bharath.homeforge.domain

data class WarmUpSet(val weightKg: Double, val reps: Int)

data class WarmUpMove(val name: String, val detail: String)

/** A short mobility warm-up done before a training day, picked from the muscles that day actually trains. */
object GeneralWarmUp {

    private val cardio = WarmUpMove("March or jog in place", "60 seconds")

    private val lower = setOf(Movement.SQUAT, Movement.HINGE, Movement.LUNGE, Movement.CALF)
    private val upperPush = setOf(Movement.HORIZONTAL_PUSH, Movement.VERTICAL_PUSH, Movement.TRICEP, Movement.SHOULDER_ISOLATION)
    private val upperPull = setOf(Movement.HORIZONTAL_PULL, Movement.BICEP)
    private val core = setOf(Movement.CORE)

    private val lowerMoves = listOf(
        WarmUpMove("Leg swings", "10 each leg, front to back"),
        WarmUpMove("Hip circles", "10 each direction"),
        WarmUpMove("Bodyweight squat", "10 reps"),
    )
    private val upperPushMoves = listOf(
        WarmUpMove("Arm circles", "10 forward, 10 backward"),
        WarmUpMove("Shoulder rolls", "10 each direction"),
    )
    private val upperPullMoves = listOf(
        WarmUpMove("Arm circles", "10 forward, 10 backward"),
        WarmUpMove("Reverse snow angel", "10 reps"),
    )
    private val coreMoves = listOf(
        WarmUpMove("Cat-cow", "8 reps"),
        WarmUpMove("Dead bug", "8 reps"),
    )

    /** Picks warm-up moves for the muscle groups [movements] trains that day. Always starts with a cardio opener. */
    fun movesFor(movements: Set<Movement>): List<WarmUpMove> {
        val picked = LinkedHashSet<WarmUpMove>()
        picked += cardio
        if (movements.any { it in lower }) picked += lowerMoves
        if (movements.any { it in upperPush }) picked += upperPushMoves
        if (movements.any { it in upperPull }) picked += upperPullMoves
        if (movements.any { it in core }) picked += coreMoves
        return picked.toList()
    }
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
