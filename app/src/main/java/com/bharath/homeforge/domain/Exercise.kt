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
 * [level] is the lowest level that uses the exercise. [prop] names everyday furniture it needs, if any.
 * [equipmentFree] is true when it needs none of your plates or rods.
 */
data class Exercise(
    val name: String,
    val movement: Movement,
    val rig: Rig?,
    val startKg: Double = 0.0,
    val timed: Boolean = false,
    val level: Level = Level.BEGINNER,
    val prop: String? = null,
    val equipmentFree: Boolean = rig == null,
)

object ExerciseLibrary {

    private const val MIN_POOL = 2

    private val B = Level.BEGINNER
    private val I = Level.INTERMEDIATE
    private val A = Level.ADVANCED

    val all: List<Exercise> = listOf(
        // Squat
        Exercise("Goblet squat", Movement.SQUAT, Rig.SINGLE_DUMBBELL, 12.0, level = B),
        Exercise("Barbell back squat", Movement.SQUAT, Rig.BARBELL, 30.0, level = I),
        Exercise("Barbell front squat", Movement.SQUAT, Rig.BARBELL, 20.0, level = A),
        Exercise("Dumbbell squat", Movement.SQUAT, Rig.DUMBBELL_PAIR, 8.0, level = B),
        Exercise("Bodyweight squat", Movement.SQUAT, null, level = B),
        Exercise("Wall sit", Movement.SQUAT, null, timed = true, level = B, prop = "a wall"),
        Exercise("Jump squat", Movement.SQUAT, null, level = I),
        Exercise("Assisted pistol squat", Movement.SQUAT, null, level = A, prop = "a sturdy chair or doorframe to hold"),

        // Hinge
        Exercise("Romanian deadlift", Movement.HINGE, Rig.BARBELL, 30.0, level = I),
        Exercise("Dumbbell Romanian deadlift", Movement.HINGE, Rig.DUMBBELL_PAIR, 10.0, level = B),
        Exercise("Barbell deadlift", Movement.HINGE, Rig.BARBELL, 40.0, level = I),
        Exercise("Barbell glute bridge", Movement.HINGE, Rig.BARBELL, 30.0, level = B),
        Exercise("Glute bridge", Movement.HINGE, null, level = B),
        Exercise("Good morning", Movement.HINGE, null, level = B),
        Exercise("Single-leg glute bridge", Movement.HINGE, null, level = I),
        Exercise("Single-leg Romanian deadlift", Movement.HINGE, null, level = A),

        // Lunge
        Exercise("Dumbbell reverse lunge", Movement.LUNGE, Rig.DUMBBELL_PAIR, 6.0, level = B),
        Exercise("Split squat", Movement.LUNGE, Rig.DUMBBELL_PAIR, 6.0, level = I, prop = "a sturdy chair"),
        Exercise("Goblet reverse lunge", Movement.LUNGE, Rig.SINGLE_DUMBBELL, 10.0, level = B),
        Exercise("Bodyweight reverse lunge", Movement.LUNGE, null, level = B),
        Exercise("Step-up", Movement.LUNGE, null, level = B, prop = "a sturdy chair or step"),
        Exercise("Rear-foot elevated split squat", Movement.LUNGE, null, level = I, prop = "a sturdy chair"),
        Exercise("Jumping lunge", Movement.LUNGE, null, level = A),

        // Horizontal push
        Exercise("Dumbbell floor press", Movement.HORIZONTAL_PUSH, Rig.DUMBBELL_PAIR, 8.0, level = B),
        Exercise("Barbell floor press", Movement.HORIZONTAL_PUSH, Rig.BARBELL, 25.0, level = I),
        Exercise("Push-up", Movement.HORIZONTAL_PUSH, null, level = B),
        Exercise("Close-grip push-up", Movement.HORIZONTAL_PUSH, null, level = I),
        Exercise("Incline push-up", Movement.HORIZONTAL_PUSH, null, level = B, prop = "a sturdy table or counter"),
        Exercise("Knee push-up", Movement.HORIZONTAL_PUSH, null, level = B),
        Exercise("Diamond push-up", Movement.HORIZONTAL_PUSH, null, level = I),
        Exercise("Decline push-up", Movement.HORIZONTAL_PUSH, null, level = A, prop = "a sturdy chair"),
        Exercise("Archer push-up", Movement.HORIZONTAL_PUSH, null, level = A),

        // Vertical push
        Exercise("Barbell overhead press", Movement.VERTICAL_PUSH, Rig.BARBELL, 20.0, level = I),
        Exercise("Dumbbell shoulder press", Movement.VERTICAL_PUSH, Rig.DUMBBELL_PAIR, 6.0, level = B),
        Exercise("Pike push-up", Movement.VERTICAL_PUSH, null, level = I),
        Exercise("Hands-elevated pike push-up", Movement.VERTICAL_PUSH, null, level = B, prop = "a sturdy chair"),
        Exercise("Pike hold", Movement.VERTICAL_PUSH, null, timed = true, level = B),
        Exercise("Feet-elevated pike push-up", Movement.VERTICAL_PUSH, null, level = A, prop = "a sturdy chair"),

        // Horizontal pull
        Exercise("Barbell bent-over row", Movement.HORIZONTAL_PULL, Rig.BARBELL, 30.0, level = I),
        Exercise("One-arm dumbbell row", Movement.HORIZONTAL_PULL, Rig.SINGLE_DUMBBELL, 12.0, level = B, prop = "a sturdy chair"),
        Exercise("Dumbbell bent-over row", Movement.HORIZONTAL_PULL, Rig.DUMBBELL_PAIR, 8.0, level = B),
        Exercise("Barbell upright row", Movement.HORIZONTAL_PULL, Rig.BARBELL, 15.0, level = I),
        Exercise("Superman", Movement.HORIZONTAL_PULL, null, level = B),
        Exercise("Reverse snow angel", Movement.HORIZONTAL_PULL, null, level = B),
        Exercise("Table inverted row", Movement.HORIZONTAL_PULL, null, level = I, prop = "a very sturdy table"),
        Exercise("Feet-elevated inverted row", Movement.HORIZONTAL_PULL, null, level = A, prop = "a very sturdy table and a chair"),

        // Biceps
        Exercise("Dumbbell curl", Movement.BICEP, Rig.DUMBBELL_PAIR, 5.0, level = B),
        Exercise("Barbell curl", Movement.BICEP, Rig.BARBELL, 15.0, level = B),
        Exercise("Towel curl", Movement.BICEP, null, level = B, prop = "a towel"),
        Exercise("Underhand inverted row", Movement.BICEP, null, level = I, prop = "a very sturdy table"),

        // Triceps
        Exercise("Overhead triceps extension", Movement.TRICEP, Rig.SINGLE_DUMBBELL, 8.0, level = B),
        Exercise("Close-grip floor press", Movement.TRICEP, Rig.BARBELL, 20.0, level = I),
        Exercise("Triceps kickback", Movement.TRICEP, Rig.DUMBBELL_PAIR, 3.0, level = B),
        Exercise("Chair dip", Movement.TRICEP, null, level = B, prop = "a sturdy chair"),
        Exercise("Close-grip incline push-up", Movement.TRICEP, null, level = B, prop = "a sturdy table or counter"),
        Exercise("Table triceps extension", Movement.TRICEP, null, level = I, prop = "a very sturdy table"),
        Exercise("Feet-elevated chair dip", Movement.TRICEP, null, level = A, prop = "two sturdy chairs"),

        // Shoulder isolation
        Exercise("Lateral raise", Movement.SHOULDER_ISOLATION, Rig.DUMBBELL_PAIR, 3.0, level = B),
        Exercise("Rear delt fly", Movement.SHOULDER_ISOLATION, Rig.DUMBBELL_PAIR, 3.0, level = B),
        Exercise("Plank shoulder tap", Movement.SHOULDER_ISOLATION, null, level = B),
        Exercise("Arm circles", Movement.SHOULDER_ISOLATION, null, level = B),
        Exercise("Pike shoulder tap", Movement.SHOULDER_ISOLATION, null, level = I),

        // Core
        Exercise("Plank", Movement.CORE, null, timed = true, level = B),
        Exercise("Russian twist", Movement.CORE, Rig.SINGLE_DUMBBELL, 5.0, level = I),
        Exercise("Dead bug", Movement.CORE, null, level = B),
        Exercise("Side plank", Movement.CORE, null, timed = true, level = B),
        Exercise("Leg raise", Movement.CORE, null, level = I),
        Exercise("Bicycle crunch", Movement.CORE, null, level = B),
        Exercise("Weighted crunch", Movement.CORE, Rig.SINGLE_DUMBBELL, 5.0, level = I),
        Exercise("Barbell rollout", Movement.CORE, null, level = A, equipmentFree = false),
        Exercise("Mountain climber", Movement.CORE, null, timed = true, level = B),
        Exercise("Crunch", Movement.CORE, null, level = B),
        Exercise("Bird dog", Movement.CORE, null, level = B),
        Exercise("Flutter kicks", Movement.CORE, null, timed = true, level = I),
        Exercise("V-up", Movement.CORE, null, level = A),
        Exercise("Hollow hold", Movement.CORE, null, timed = true, level = A),

        // Calves
        Exercise("Dumbbell calf raise", Movement.CALF, Rig.DUMBBELL_PAIR, 8.0, level = B),
        Exercise("Bodyweight calf raise", Movement.CALF, null, level = B),
        Exercise("Single-leg calf raise", Movement.CALF, null, level = I),
    )

    fun forMovement(movement: Movement): List<Exercise> = all.filter { it.movement == movement }

    /**
     * Exercises for [movement] that suit [level], best match first: your own level, then easier ones.
     * Harder exercises are only added if there would otherwise be fewer than two to rotate through.
     * With [equipmentFree], only exercises that need none of your plates or rods are used.
     */
    fun poolFor(movement: Movement, level: Level, equipmentFree: Boolean): List<Exercise> {
        val candidates = all.filter { it.movement == movement && (!equipmentFree || it.equipmentFree) }
        val suitable = candidates.filter { it.level.ordinal <= level.ordinal }.sortedByDescending { it.level.ordinal }
        if (suitable.size >= MIN_POOL) return suitable
        val harder = candidates.filter { it.level.ordinal > level.ordinal }.sortedBy { it.level.ordinal }
        return suitable + harder.take(MIN_POOL - suitable.size)
    }
}
