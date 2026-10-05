package com.bharath.homeforge.domain

data class SetResult(val weightKg: Double?, val reps: Int)

data class Suggestion(
    val weightKg: Double?,
    val sets: Int,
    val reps: IntRange,
    val reason: String?,
)

/** Rule-based adjustment of the next session from past results. No history means the plain starting plan. */
object Adaptation {

    private const val DELOAD_FACTOR = 0.9

    /** [history] holds past sessions of this exercise, most recent first. */
    fun suggest(
        equipment: Equipment,
        exercise: Exercise,
        history: List<List<SetResult>>,
        plannedSets: Int,
        reps: IntRange,
    ): Suggestion {
        val rig = exercise.rig
        if (history.isEmpty()) {
            return Suggestion(rig?.let { LoadCalculator.snap(equipment, it, exercise.startKg) }, plannedSets, reps, null)
        }
        val last = history.first()
        if (rig == null) return bodyweight(exercise, last, plannedSets, reps)

        val lastWeight = last.mapNotNull { it.weightKg }.maxOrNull()
            ?: return Suggestion(LoadCalculator.snap(equipment, rig, exercise.startKg), plannedSets, reps, null)
        // Equipment may have changed since last time, so start from the nearest weight you can still build.
        val base = LoadCalculator.snap(equipment, rig, lastWeight)

        if (hitAll(last, plannedSets, reps)) {
            val up = LoadCalculator.nextUp(equipment, rig, base)
                ?: return Suggestion(
                    base,
                    minOf(maxOf(last.size, plannedSets) + 1, plannedSets + 2),
                    reps,
                    "Heaviest load reached: added a set.",
                )
            val wellAbove = last.all { it.reps >= reps.last + 2 }
            val bigJump = if (wellAbove) LoadCalculator.nextUp(equipment, rig, up) else null
            return if (bigJump != null) {
                Suggestion(bigJump, plannedSets, reps, "Reps were well above target: bigger jump.")
            } else {
                Suggestion(up, plannedSets, reps, "All reps hit last time: weight up.")
            }
        }

        val failedStreak = history
            .takeWhile { topWeight(equipment, rig, it) == base }
            .takeWhile { !hitAll(it, plannedSets, reps) }
            .size

        return when {
            failedStreak >= 3 -> {
                val target = LoadCalculator.snap(equipment, rig, base * DELOAD_FACTOR)
                val lighter = if (target < base) target else LoadCalculator.nextDown(equipment, rig, base) ?: base
                Suggestion(lighter, plannedSets, reps, "No progress for 3 sessions: deloading about 10% to rebuild.")
            }
            failedStreak >= 2 && last.any { it.reps < reps.first } ->
                Suggestion(
                    LoadCalculator.nextDown(equipment, rig, base) ?: base,
                    plannedSets,
                    reps,
                    "Missed the rep range twice: one step lighter.",
                )
            else -> Suggestion(base, plannedSets, reps, "Same weight: hit ${reps.last} reps on every set to go up.")
        }
    }

    private fun bodyweight(exercise: Exercise, last: List<SetResult>, plannedSets: Int, reps: IntRange): Suggestion {
        if (!hitAll(last, plannedSets, reps)) return Suggestion(null, plannedSets, reps, null)
        val step = if (exercise.timed) 5 else 2
        val newTop = last.minOf { it.reps } + step
        val shift = newTop - reps.last
        val unit = if (exercise.timed) "seconds" else "reps"
        return Suggestion(null, plannedSets, (reps.first + shift)..newTop, "All reps hit: target raised by $step $unit.")
    }

    private fun hitAll(session: List<SetResult>, plannedSets: Int, reps: IntRange): Boolean =
        session.size >= plannedSets && session.all { it.reps >= reps.last }

    private fun topWeight(equipment: Equipment, rig: Rig, session: List<SetResult>): Double? =
        session.mapNotNull { it.weightKg }.maxOrNull()?.let { LoadCalculator.snap(equipment, rig, it) }
}
