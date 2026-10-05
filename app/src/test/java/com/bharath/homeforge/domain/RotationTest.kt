package com.bharath.homeforge.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RotationTest {

    @Test
    fun firstFourWeeksAreBlockZero() {
        assertEquals(0, Rotation.blockIndex(100, 100))
        assertEquals(0, Rotation.blockIndex(100, 127))
        assertEquals(1, Rotation.blockIndex(100, 128))
    }

    @Test
    fun weekCyclesOneToFour() {
        assertEquals(1, Rotation.weekInBlock(100, 100))
        assertEquals(2, Rotation.weekInBlock(100, 107))
        assertEquals(4, Rotation.weekInBlock(100, 127))
        assertEquals(1, Rotation.weekInBlock(100, 128))
    }

    @Test
    fun dateBeforeStartIsTreatedAsStart() {
        assertEquals(0, Rotation.blockIndex(100, 50))
        assertEquals(1, Rotation.weekInBlock(100, 50))
    }

    @Test
    fun rotationChangesExercisesButKeepsDaysValid() {
        val equipment = Equipment.Default
        Split.entries.forEach { split ->
            split.dayNames.indices.forEach { day ->
                val base = RoutineGenerator.generate(split, day, equipment)
                val next = RoutineGenerator.generate(split, day, equipment, rotation = 1)
                assertNotEquals(base.items.map { it.exercise }, next.items.map { it.exercise })

                for (block in 0..11) {
                    val routine = RoutineGenerator.generate(split, day, equipment, rotation = block)
                    val names = routine.items.map { it.exercise.name }
                    assertEquals("$split day $day block $block", names.size, names.toSet().size)
                    routine.items.forEach { item ->
                        val rig = item.exercise.rig ?: return@forEach
                        val options = LoadCalculator.achievableWeights(equipment, rig)
                        assertTrue(options.any { Math.abs(it - item.weightKg!!) < 1e-9 })
                    }
                }
            }
        }
    }
}
