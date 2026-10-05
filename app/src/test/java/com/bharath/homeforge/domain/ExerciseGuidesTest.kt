package com.bharath.homeforge.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExerciseGuidesTest {

    @Test
    fun everyExerciseInTheLibraryHasAGuide() {
        val missing = ExerciseLibrary.all.filter { ExerciseGuides.forExercise(it.name) == null }.map { it.name }
        assertTrue("Missing guides: $missing", missing.isEmpty())
    }

    @Test
    fun everyGuideBelongsToARealExerciseAndIsComplete() {
        val names = ExerciseLibrary.all.map { it.name }.toSet()
        names.forEach { name ->
            val guide = ExerciseGuides.forExercise(name)!!
            assertTrue("$name steps", guide.steps.size >= 3)
            assertTrue("$name cues", guide.cues.isNotEmpty())
            assertTrue("$name mistakes", guide.mistakes.isNotEmpty())
        }
    }

    @Test
    fun exerciseNamesAreUnique() {
        assertEquals(ExerciseLibrary.all.size, ExerciseLibrary.all.map { it.name }.toSet().size)
    }

    @Test
    fun everyPictureNameBelongsToARealExercise() {
        val names = ExerciseLibrary.all.map { it.name }.toSet()
        ExerciseMediaLibrary.all.keys.forEach { assertTrue("$it has pictures but is not an exercise", it in names) }
    }

    @Test
    fun everyPictureHasACredit() {
        ExerciseMediaLibrary.all.forEach { (name, media) ->
            assertTrue("$name has no pictures", media.drawableNames.isNotEmpty())
            assertTrue("$name credit", media.credit.author.isNotBlank() && media.credit.sourceUrl.startsWith("https://"))
        }
    }
}
