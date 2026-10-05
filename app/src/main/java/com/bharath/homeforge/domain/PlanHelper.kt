package com.bharath.homeforge.domain

object PlanHelper {

    /** The day after the one you did last, wrapping around; the first day if you haven't started this plan. */
    fun nextDay(program: Split, lastDayIndex: Int?): Int =
        if (lastDayIndex == null) 0 else (lastDayIndex + 1) % program.dayNames.size

    /** The usual training weekdays for a plan (1 = Monday ... 7 = Sunday). The user can change them. */
    fun defaultTrainingDays(program: Split): Set<Int> = when (program) {
        Split.SIX_DAY_PPL -> setOf(1, 2, 3, 4, 5, 6)
        Split.CORE_DAY -> emptySet()
        else -> setOf(1, 3, 5)
    }

    /** A plain-words summary of which muscles a day trains, for example "Trains: chest, shoulders, triceps, abs". */
    fun focusText(split: Split, dayIndex: Int): String {
        val muscles = RoutineGenerator.slots(split, dayIndex).flatMap { muscles(it.movement) }.distinct()
        return "Trains: " + muscles.joinToString(", ")
    }

    private fun muscles(movement: Movement): List<String> = when (movement) {
        Movement.SQUAT -> listOf("quads", "glutes")
        Movement.HINGE -> listOf("hamstrings", "glutes")
        Movement.LUNGE -> listOf("quads", "glutes")
        Movement.HORIZONTAL_PUSH -> listOf("chest")
        Movement.VERTICAL_PUSH -> listOf("shoulders")
        Movement.HORIZONTAL_PULL -> listOf("back")
        Movement.BICEP -> listOf("biceps")
        Movement.TRICEP -> listOf("triceps")
        Movement.SHOULDER_ISOLATION -> listOf("shoulders")
        Movement.CORE -> listOf("abs")
        Movement.CALF -> listOf("calves")
    }
}
