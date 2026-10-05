package com.bharath.homeforge.domain

data class Effort(val exercise: String, val weightKg: Double?, val reps: Int)

object PersonalRecords {

    /** Estimated one-rep max (Epley) for weighted sets, plain rep count for bodyweight ones. */
    fun score(effort: Effort): Double =
        if (effort.weightKg == null) effort.reps.toDouble() else effort.weightKg * (1 + effort.reps / 30.0)

    fun best(efforts: List<Effort>): Effort? = efforts.maxByOrNull { score(it) }

    /** Best new effort per exercise that beats everything logged before. First-ever sessions don't count. */
    fun newRecords(previous: List<Effort>, current: List<Effort>): List<Effort> {
        val previousBest = previous.groupBy { it.exercise }.mapValues { (_, list) -> list.maxOf { score(it) } }
        return current.groupBy { it.exercise }.mapNotNull { (name, list) ->
            val old = previousBest[name] ?: return@mapNotNull null
            val top = best(list) ?: return@mapNotNull null
            if (score(top) > old + 1e-9) top else null
        }
    }
}
