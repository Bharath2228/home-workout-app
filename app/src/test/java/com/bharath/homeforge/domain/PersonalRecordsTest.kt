package com.bharath.homeforge.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PersonalRecordsTest {

    private val old = listOf(Effort("Curl", 10.0, 10), Effort("Curl", 10.0, 8))

    @Test
    fun heavierWeightIsARecord() {
        val records = PersonalRecords.newRecords(old, listOf(Effort("Curl", 12.0, 8)))
        assertEquals(listOf(Effort("Curl", 12.0, 8)), records)
    }

    @Test
    fun moreRepsAtTheSameWeightIsARecord() {
        val records = PersonalRecords.newRecords(old, listOf(Effort("Curl", 10.0, 12)))
        assertEquals(1, records.size)
    }

    @Test
    fun equalOrWorseIsNotARecord() {
        assertTrue(PersonalRecords.newRecords(old, listOf(Effort("Curl", 10.0, 10))).isEmpty())
        assertTrue(PersonalRecords.newRecords(old, listOf(Effort("Curl", 8.0, 8))).isEmpty())
    }

    @Test
    fun firstEverSessionIsNotARecord() {
        assertTrue(PersonalRecords.newRecords(emptyList(), listOf(Effort("Curl", 10.0, 10))).isEmpty())
    }

    @Test
    fun bodyweightUsesRepCount() {
        val previous = listOf(Effort("Push-up", null, 15))
        assertEquals(1, PersonalRecords.newRecords(previous, listOf(Effort("Push-up", null, 18))).size)
        assertTrue(PersonalRecords.newRecords(previous, listOf(Effort("Push-up", null, 15))).isEmpty())
    }

    @Test
    fun onlyTheBestSetPerExerciseIsReported() {
        val current = listOf(Effort("Curl", 12.0, 8), Effort("Curl", 14.0, 8))
        assertEquals(listOf(Effort("Curl", 14.0, 8)), PersonalRecords.newRecords(old, current))
    }
}
