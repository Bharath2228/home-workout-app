package com.bharath.homeforge.domain

/** Credit for pictures taken from the wger exercise database (https://wger.de), used under Creative Commons licenses. */
data class ImageCredit(
    val author: String,
    val license: String,
    val licenseUrl: String,
    val sourceUrl: String,
)

data class ExerciseMedia(val drawableNames: List<String>, val credit: ImageCredit)

object ExerciseMediaLibrary {

    private val media: Map<String, ExerciseMedia> = mapOf(
        "Goblet squat" to ExerciseMedia(
            listOf("ex_goblet_squat_1"),
            ImageCredit("philip", "CC BY-SA 4.0", "https://creativecommons.org/licenses/by-sa/4.0/", "https://wger.de/en/exercise/203/view/"),
        ),
        "Barbell back squat" to ExerciseMedia(
            listOf("ex_barbell_back_squat_1", "ex_barbell_back_squat_2"),
            ImageCredit("Workout Guru", "CC BY-SA 4.0", "https://creativecommons.org/licenses/by-sa/4.0/", "https://wger.de/en/exercise/1801/view/"),
        ),
        "Barbell front squat" to ExerciseMedia(
            listOf("ex_barbell_front_squat_1", "ex_barbell_front_squat_2"),
            ImageCredit("Everkinetic", "CC BY-SA 3.0", "https://creativecommons.org/licenses/by-sa/3.0/", "https://wger.de/en/exercise/257/view/"),
        ),
        "Dumbbell Romanian deadlift" to ExerciseMedia(
            listOf("ex_dumbbell_romanian_deadlift_1"),
            ImageCredit("AlucardEvil40", "CC BY-SA 4.0", "https://creativecommons.org/licenses/by-sa/4.0/", "https://wger.de/en/exercise/1652/view/"),
        ),
        "Dumbbell reverse lunge" to ExerciseMedia(
            listOf("ex_dumbbell_reverse_lunge_1"),
            ImageCredit("AlucardEvil40", "CC BY-SA 4.0", "https://creativecommons.org/licenses/by-sa/4.0/", "https://wger.de/en/exercise/1651/view/"),
        ),
        "Split squat" to ExerciseMedia(
            listOf("ex_split_squat_1"),
            ImageCredit("AlucardEvil40", "CC BY-SA 4.0", "https://creativecommons.org/licenses/by-sa/4.0/", "https://wger.de/en/exercise/1706/view/"),
        ),
        "Goblet reverse lunge" to ExerciseMedia(
            listOf("ex_goblet_reverse_lunge_1"),
            ImageCredit("philip", "CC BY-SA 4.0", "https://creativecommons.org/licenses/by-sa/4.0/", "https://wger.de/en/exercise/999/view/"),
        ),
        "Dumbbell floor press" to ExerciseMedia(
            listOf("ex_dumbbell_floor_press_1"),
            ImageCredit("author not recorded on wger.de", "CC BY-SA 4.0", "https://creativecommons.org/licenses/by-sa/4.0/", "https://wger.de/en/exercise/1084/view/"),
        ),
        "Barbell overhead press" to ExerciseMedia(
            listOf("ex_barbell_overhead_press_1", "ex_barbell_overhead_press_2"),
            ImageCredit("Everkinetic", "CC BY-SA 3.0", "https://creativecommons.org/licenses/by-sa/3.0/", "https://wger.de/en/exercise/566/view/"),
        ),
        "Dumbbell shoulder press" to ExerciseMedia(
            listOf("ex_dumbbell_shoulder_press_1", "ex_dumbbell_shoulder_press_2"),
            ImageCredit("Everkinetic", "CC BY-SA 3.0", "https://creativecommons.org/licenses/by-sa/3.0/", "https://wger.de/en/exercise/567/view/"),
        ),
        "Barbell bent-over row" to ExerciseMedia(
            listOf("ex_barbell_bent_over_row_1", "ex_barbell_bent_over_row_2"),
            ImageCredit("Everkinetic", "CC BY-SA 3.0", "https://creativecommons.org/licenses/by-sa/3.0/", "https://wger.de/en/exercise/83/view/"),
        ),
        "One-arm dumbbell row" to ExerciseMedia(
            listOf("ex_one_arm_dumbbell_row_1"),
            ImageCredit("Franpol", "CC BY-SA 4.0", "https://creativecommons.org/licenses/by-sa/4.0/", "https://wger.de/en/exercise/81/view/"),
        ),
        "Dumbbell curl" to ExerciseMedia(
            listOf("ex_dumbbell_curl_1", "ex_dumbbell_curl_2"),
            ImageCredit("Everkinetic", "CC BY-SA 3.0", "https://creativecommons.org/licenses/by-sa/3.0/", "https://wger.de/en/exercise/92/view/"),
        ),
        "Barbell curl" to ExerciseMedia(
            listOf("ex_barbell_curl_1", "ex_barbell_curl_2"),
            ImageCredit("Everkinetic", "CC BY-SA 3.0", "https://creativecommons.org/licenses/by-sa/3.0/", "https://wger.de/en/exercise/91/view/"),
        ),
        "Overhead triceps extension" to ExerciseMedia(
            listOf("ex_overhead_triceps_extension_1"),
            ImageCredit("author not recorded on wger.de", "CC BY-SA 4.0", "https://creativecommons.org/licenses/by-sa/4.0/", "https://wger.de/en/exercise/1336/view/"),
        ),
        "Lateral raise" to ExerciseMedia(
            listOf("ex_lateral_raise_1", "ex_lateral_raise_2"),
            ImageCredit("Everkinetic", "CC BY-SA 3.0", "https://creativecommons.org/licenses/by-sa/3.0/", "https://wger.de/en/exercise/348/view/"),
        ),
        "Plank" to ExerciseMedia(
            listOf("ex_plank_1"),
            ImageCredit("utkb", "CC BY-SA 4.0", "https://creativecommons.org/licenses/by-sa/4.0/", "https://wger.de/en/exercise/458/view/"),
        ),
        "Russian twist" to ExerciseMedia(
            listOf("ex_russian_twist_1"),
            ImageCredit("lion", "CC BY-SA 4.0", "https://creativecommons.org/licenses/by-sa/4.0/", "https://wger.de/en/exercise/1193/view/"),
        ),
        "Leg raise" to ExerciseMedia(
            listOf("ex_leg_raise_1", "ex_leg_raise_2"),
            ImageCredit("Everkinetic", "CC BY-SA 3.0", "https://creativecommons.org/licenses/by-sa/3.0/", "https://wger.de/en/exercise/377/view/"),
        ),
        "Weighted crunch" to ExerciseMedia(
            listOf("ex_weighted_crunch_1"),
            ImageCredit("AlucardEvil40", "CC BY-SA 4.0", "https://creativecommons.org/licenses/by-sa/4.0/", "https://wger.de/en/exercise/1648/view/"),
        ),
        "Dumbbell calf raise" to ExerciseMedia(
            listOf("ex_dumbbell_calf_raise_1"),
            ImageCredit("clafal", "CC BY-SA 4.0", "https://creativecommons.org/licenses/by-sa/4.0/", "https://wger.de/en/exercise/622/view/"),
        ),
        "Bodyweight calf raise" to ExerciseMedia(
            listOf("ex_bodyweight_calf_raise_1"),
            ImageCredit("erikocobra", "CC BY-SA 4.0", "https://creativecommons.org/licenses/by-sa/4.0/", "https://wger.de/en/exercise/1243/view/"),
        ),
    )

    fun forExercise(name: String): ExerciseMedia? = media[name]

    val all: Map<String, ExerciseMedia> get() = media
}
