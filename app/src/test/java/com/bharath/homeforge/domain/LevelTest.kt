package com.bharath.homeforge.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LevelTest {

    private val equipment = Equipment.Default

    @Test
    fun thresholdsAre24And72Workouts() {
        assertEquals(Level.BEGINNER, LevelProgress.levelForWorkouts(0))
        assertEquals(Level.BEGINNER, LevelProgress.levelForWorkouts(23))
        assertEquals(Level.INTERMEDIATE, LevelProgress.levelForWorkouts(24))
        assertEquals(Level.INTERMEDIATE, LevelProgress.levelForWorkouts(71))
        assertEquals(Level.ADVANCED, LevelProgress.levelForWorkouts(72))
    }

    @Test
    fun autoLevelNeverGoesBelowTheChosenLevel() {
        assertEquals(Level.INTERMEDIATE, LevelProgress.status(Level.INTERMEDIATE, true, 0).level)
        assertEquals(Level.ADVANCED, LevelProgress.status(Level.BEGINNER, true, 80).level)
        assertEquals(Level.ADVANCED, LevelProgress.status(Level.ADVANCED, true, 5).level)
    }

    @Test
    fun withAutoOffTheChosenLevelStays() {
        assertEquals(Level.BEGINNER, LevelProgress.status(Level.BEGINNER, false, 100).level)
    }

    @Test
    fun workoutsToNextCountsDown() {
        val status = LevelProgress.status(Level.BEGINNER, true, 10)
        assertEquals(Level.INTERMEDIATE, status.nextLevel)
        assertEquals(14, status.workoutsToNext)
        assertEquals(60, LevelProgress.status(Level.INTERMEDIATE, true, 12).workoutsToNext)
        assertNull(LevelProgress.status(Level.ADVANCED, true, 100).workoutsToNext)
    }

    @Test
    fun setsScaleWithLevel() {
        assertEquals(3, Level.BEGINNER.setsFor(4))
        assertEquals(2, Level.BEGINNER.setsFor(3))
        assertEquals(2, Level.BEGINNER.setsFor(2))
        assertEquals(3, Level.INTERMEDIATE.setsFor(3))
        assertEquals(4, Level.ADVANCED.setsFor(3))
        assertEquals(2, Level.ADVANCED.setsFor(2))
    }

    @Test
    fun everyLevelAndEquipmentModeHasEnoughExercisesForEveryMovement() {
        Level.entries.forEach { level ->
            listOf(false, true).forEach { equipmentFree ->
                Movement.entries.forEach { movement ->
                    val pool = ExerciseLibrary.poolFor(movement, level, equipmentFree)
                    assertTrue("$level $movement free=$equipmentFree", pool.size >= 2)
                    assertEquals(pool.size, pool.map { it.name }.toSet().size)
                    if (equipmentFree) assertTrue(pool.all { it.equipmentFree })
                }
            }
        }
    }

    @Test
    fun beginnersOnlyGetBeginnerExercisesWhereThereAreEnough() {
        val squats = ExerciseLibrary.poolFor(Movement.SQUAT, Level.BEGINNER, false)
        assertTrue(squats.all { it.level == Level.BEGINNER })
        val advancedSquats = ExerciseLibrary.poolFor(Movement.SQUAT, Level.ADVANCED, false)
        assertEquals(Level.ADVANCED, advancedSquats.first().level)
    }

    @Test
    fun noEquipmentRoutinesNeedNoPlatesOrRods() {
        Level.entries.forEach { level ->
            Split.entries.forEach { split ->
                split.dayNames.indices.forEach { day ->
                    RoutineGenerator.generate(split, day, equipment, level = level, noEquipment = true).items.forEach {
                        assertTrue("${it.exercise.name} at $level", it.exercise.equipmentFree)
                        assertNull(it.weightKg)
                    }
                }
            }
        }
    }

    @Test
    fun everyLevelModeAndRotationGivesValidDaysWithoutDuplicates() {
        Level.entries.forEach { level ->
            listOf(false, true).forEach { noEquipment ->
                Split.entries.forEach { split ->
                    split.dayNames.indices.forEach { day ->
                        for (block in 0..11) {
                            val items = RoutineGenerator.generate(
                                split, day, equipment, rotation = block, level = level, noEquipment = noEquipment,
                            ).items
                            val names = items.map { it.exercise.name }
                            assertEquals("$level free=$noEquipment $split $day block $block", names.size, names.toSet().size)
                            items.forEach { item ->
                                val rig = item.exercise.rig ?: return@forEach
                                val options = LoadCalculator.achievableWeights(equipment, rig)
                                assertTrue(options.any { Math.abs(it - item.weightKg!!) < 1e-9 })
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    fun higherLevelsDoMoreSetsAndStartHeavier() {
        val beginner = RoutineGenerator.generate(Split.BODY_PART_SPLIT, 0, equipment, level = Level.BEGINNER)
        val advanced = RoutineGenerator.generate(Split.BODY_PART_SPLIT, 0, equipment, level = Level.ADVANCED)
        assertTrue(beginner.items.sumOf { it.sets } < advanced.items.sumOf { it.sets })
        assertNotEquals(beginner.items.map { it.exercise }, advanced.items.map { it.exercise })

        val squat = Exercise("Barbell back squat", Movement.SQUAT, Rig.BARBELL, 30.0)
        val light = Adaptation.suggest(equipment, squat, emptyList(), 3, 8..10, Level.BEGINNER.startFactor)
        val heavy = Adaptation.suggest(equipment, squat, emptyList(), 3, 8..10, Level.ADVANCED.startFactor)
        assertTrue(light.weightKg!! < heavy.weightKg!!)
    }
}
