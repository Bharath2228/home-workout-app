package com.bharath.homeforge.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RoutineGeneratorTest {

    private val equipment = Equipment.Default

    @Test
    fun everyMovementHasAtLeastTwoExercises() {
        Movement.entries.forEach { movement ->
            assertTrue(movement.name, ExerciseLibrary.forMovement(movement).size >= 2)
        }
    }

    @Test
    fun everyDayHasExercisesAndNoDuplicates() {
        Split.entries.forEach { split ->
            split.dayNames.indices.forEach { day ->
                val routine = RoutineGenerator.generate(split, day, equipment)
                assertTrue(routine.items.isNotEmpty())
                val names = routine.items.map { it.exercise.name }
                assertEquals("$split day $day", names.size, names.toSet().size)
            }
        }
    }

    @Test
    fun everyWeightIsLoadableWithOwnedPlates() {
        Split.entries.forEach { split ->
            split.dayNames.indices.forEach { day ->
                RoutineGenerator.generate(split, day, equipment).items.forEach { item ->
                    val rig = item.exercise.rig
                    if (rig == null) {
                        assertEquals(null, item.weightKg)
                    } else {
                        val options = LoadCalculator.achievableWeights(equipment, rig)
                        assertTrue(item.exercise.name, options.any { Math.abs(it - item.weightKg!!) < 1e-9 })
                    }
                }
            }
        }
    }

    @Test
    fun everyTrainingDayEndsWithCoreWork() {
        Split.entries.forEach { split ->
            split.dayNames.indices.forEach { day ->
                val items = RoutineGenerator.generate(split, day, equipment).items
                assertTrue("$split day $day", items.any { it.exercise.movement == Movement.CORE })
            }
        }
    }

    @Test
    fun coreDayHasSeveralDifferentCoreExercises() {
        val items = RoutineGenerator.generate(Split.CORE_DAY, 0, equipment).items
        assertTrue(items.size >= 5)
        assertTrue(items.all { it.exercise.movement == Movement.CORE })
    }

    @Test
    fun swapOffsetChangesExerciseAndWraps() {
        val base = RoutineGenerator.generate(Split.FULL_BODY, 0, equipment)
        val swapped = RoutineGenerator.generate(Split.FULL_BODY, 0, equipment, mapOf(0 to 1))
        assertTrue(base.items[0].exercise != swapped.items[0].exercise)

        val count = ExerciseLibrary.forMovement(base.items[0].exercise.movement).size
        val wrapped = RoutineGenerator.generate(Split.FULL_BODY, 0, equipment, mapOf(0 to count))
        assertEquals(base.items[0].exercise, wrapped.items[0].exercise)
    }
}
