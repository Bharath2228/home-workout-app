package com.bharath.homeforge.domain

enum class Level(val label: String, val description: String, val startFactor: Double) {
    BEGINNER(
        "Beginner",
        "Simpler exercises, fewer sets and lighter starting weights.",
        0.7,
    ),
    INTERMEDIATE(
        "Intermediate",
        "The standard plan: balanced exercises and sets.",
        1.0,
    ),
    ADVANCED(
        "Advanced",
        "Harder exercises, an extra set on the main lifts and heavier starting weights.",
        1.2,
    ),
    ;

    /** Sets for a slot that the standard plan gives [base] sets. */
    fun setsFor(base: Int): Int = when (this) {
        BEGINNER -> (base - 1).coerceAtLeast(2)
        INTERMEDIATE -> base
        ADVANCED -> if (base >= 3) base + 1 else base
    }
}

data class LevelStatus(
    val level: Level,
    val workouts: Int,
    val nextLevel: Level?,
    val workoutsToNext: Int?,
    val auto: Boolean,
) {
    companion object {
        val Initial = LevelStatus(Level.BEGINNER, 0, Level.INTERMEDIATE, LevelProgress.INTERMEDIATE_AT, true)
    }
}

/** Levels rise with the number of workouts you have completed, not with calendar days. */
object LevelProgress {

    const val INTERMEDIATE_AT = 24
    const val ADVANCED_AT = 72

    fun levelForWorkouts(count: Int): Level = when {
        count >= ADVANCED_AT -> Level.ADVANCED
        count >= INTERMEDIATE_AT -> Level.INTERMEDIATE
        else -> Level.BEGINNER
    }

    /** With [auto] on, your level is whichever is higher: the one you chose, or the one your workouts earned. */
    fun status(chosen: Level, auto: Boolean, workouts: Int): LevelStatus {
        val earned = levelForWorkouts(workouts)
        val level = if (auto && earned.ordinal > chosen.ordinal) earned else chosen
        val next = Level.entries.getOrNull(level.ordinal + 1)
        val toNext = when (next) {
            Level.INTERMEDIATE -> INTERMEDIATE_AT - workouts
            Level.ADVANCED -> ADVANCED_AT - workouts
            else -> null
        }?.coerceAtLeast(0)
        return LevelStatus(level, workouts, next, toNext, auto)
    }
}
