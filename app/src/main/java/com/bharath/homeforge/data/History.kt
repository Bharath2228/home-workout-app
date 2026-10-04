package com.bharath.homeforge.data

data class SessionPoint(
    val time: Long,
    val topWeightKg: Double?,
    val totalReps: Int,
    val volumeKg: Double,
)

fun buildHistory(sets: List<LoggedSet>): Map<String, List<SessionPoint>> =
    sets.groupBy { it.exerciseName }.mapValues { (_, exerciseSets) ->
        exerciseSets.groupBy { it.sessionId }.values.map { session ->
            SessionPoint(
                time = session.minOf { it.loggedAt },
                topWeightKg = session.mapNotNull { it.weightKg }.maxOrNull(),
                totalReps = session.sumOf { it.reps },
                volumeKg = session.sumOf { (it.weightKg ?: 0.0) * it.reps },
            )
        }.sortedBy { it.time }
    }
