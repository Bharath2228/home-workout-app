package com.bharath.homeforge.domain

enum class Movement(val label: String) {
    SQUAT("Squat"),
    HINGE("Hinge"),
    LUNGE("Lunge"),
    HORIZONTAL_PUSH("Horizontal push"),
    VERTICAL_PUSH("Vertical push"),
    HORIZONTAL_PULL("Horizontal pull"),
    BICEP("Biceps"),
    TRICEP("Triceps"),
    SHOULDER_ISOLATION("Shoulder isolation"),
    CORE("Core"),
    CALF("Calves"),
}

/**
 * [rig] is null for bodyweight exercises. For DUMBBELL_PAIR, [startKg] is per dumbbell.
 * [timed] exercises are measured in seconds instead of reps.
 */
data class Exercise(
    val name: String,
    val movement: Movement,
    val rig: Rig?,
    val startKg: Double = 0.0,
    val timed: Boolean = false,
)

object ExerciseLibrary {

    val all: List<Exercise> = listOf(
        Exercise("Goblet squat", Movement.SQUAT, Rig.SINGLE_DUMBBELL, 12.0),
        Exercise("Barbell back squat", Movement.SQUAT, Rig.BARBELL, 30.0),
        Exercise("Barbell front squat", Movement.SQUAT, Rig.BARBELL, 20.0),
        Exercise("Dumbbell squat", Movement.SQUAT, Rig.DUMBBELL_PAIR, 8.0),

        Exercise("Romanian deadlift", Movement.HINGE, Rig.BARBELL, 30.0),
        Exercise("Dumbbell Romanian deadlift", Movement.HINGE, Rig.DUMBBELL_PAIR, 10.0),
        Exercise("Barbell deadlift", Movement.HINGE, Rig.BARBELL, 40.0),
        Exercise("Barbell glute bridge", Movement.HINGE, Rig.BARBELL, 30.0),

        Exercise("Dumbbell reverse lunge", Movement.LUNGE, Rig.DUMBBELL_PAIR, 6.0),
        Exercise("Split squat", Movement.LUNGE, Rig.DUMBBELL_PAIR, 6.0),
        Exercise("Goblet reverse lunge", Movement.LUNGE, Rig.SINGLE_DUMBBELL, 10.0),

        Exercise("Dumbbell floor press", Movement.HORIZONTAL_PUSH, Rig.DUMBBELL_PAIR, 8.0),
        Exercise("Barbell floor press", Movement.HORIZONTAL_PUSH, Rig.BARBELL, 25.0),
        Exercise("Push-up", Movement.HORIZONTAL_PUSH, null),
        Exercise("Close-grip push-up", Movement.HORIZONTAL_PUSH, null),

        Exercise("Barbell overhead press", Movement.VERTICAL_PUSH, Rig.BARBELL, 20.0),
        Exercise("Dumbbell shoulder press", Movement.VERTICAL_PUSH, Rig.DUMBBELL_PAIR, 6.0),
        Exercise("Pike push-up", Movement.VERTICAL_PUSH, null),

        Exercise("Barbell bent-over row", Movement.HORIZONTAL_PULL, Rig.BARBELL, 30.0),
        Exercise("One-arm dumbbell row", Movement.HORIZONTAL_PULL, Rig.SINGLE_DUMBBELL, 12.0),
        Exercise("Dumbbell bent-over row", Movement.HORIZONTAL_PULL, Rig.DUMBBELL_PAIR, 8.0),
        Exercise("Barbell upright row", Movement.HORIZONTAL_PULL, Rig.BARBELL, 15.0),

        Exercise("Dumbbell curl", Movement.BICEP, Rig.DUMBBELL_PAIR, 5.0),
        Exercise("Barbell curl", Movement.BICEP, Rig.BARBELL, 15.0),

        Exercise("Overhead triceps extension", Movement.TRICEP, Rig.SINGLE_DUMBBELL, 8.0),
        Exercise("Close-grip floor press", Movement.TRICEP, Rig.BARBELL, 20.0),
        Exercise("Triceps kickback", Movement.TRICEP, Rig.DUMBBELL_PAIR, 3.0),

        Exercise("Lateral raise", Movement.SHOULDER_ISOLATION, Rig.DUMBBELL_PAIR, 3.0),
        Exercise("Rear delt fly", Movement.SHOULDER_ISOLATION, Rig.DUMBBELL_PAIR, 3.0),

        Exercise("Plank", Movement.CORE, null, timed = true),
        Exercise("Russian twist", Movement.CORE, Rig.SINGLE_DUMBBELL, 5.0),
        Exercise("Dead bug", Movement.CORE, null),

        Exercise("Dumbbell calf raise", Movement.CALF, Rig.DUMBBELL_PAIR, 8.0),
        Exercise("Bodyweight calf raise", Movement.CALF, null),
    )

    fun forMovement(movement: Movement): List<Exercise> = all.filter { it.movement == movement }
}
