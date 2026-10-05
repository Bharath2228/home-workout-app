package com.bharath.homeforge.data

import com.bharath.homeforge.domain.Plate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.fail
import org.junit.Test

class BackupCodecTest {

    private val backup = Backup(
        exportedAt = 1_000L,
        sessions = listOf(WorkoutSession(id = 1, startedAt = 10, endedAt = 20, splitName = "FULL_BODY", dayIndex = 0)),
        sets = listOf(
            LoggedSet(id = 1, sessionId = 1, exerciseName = "Goblet squat", setNumber = 1, weightKg = 12.5, reps = 10, loggedAt = 20),
            LoggedSet(id = 2, sessionId = 1, exerciseName = "Plank", setNumber = 1, weightKg = null, reps = 40, loggedAt = 20),
        ),
        measurements = listOf(Measurement(id = 1, time = 30, type = "BODY_WEIGHT", value = 78.4)),
        plates = listOf(Plate(5.0, 4), Plate(1.5, 4)),
        rodWeightKg = 1.0,
    )

    @Test
    fun roundTripKeepsEverything() {
        val decoded = BackupCodec.decode(BackupCodec.encode(backup))
        assertEquals(backup, decoded)
        assertNull(decoded.sets[1].weightKg)
    }

    @Test
    fun garbageIsRejected() {
        try {
            BackupCodec.decode("not json")
            fail("expected an exception")
        } catch (expected: IllegalArgumentException) {
        }
    }

    @Test
    fun otherJsonIsRejected() {
        try {
            BackupCodec.decode("""{"app":"Other","version":1}""")
            fail("expected an exception")
        } catch (expected: IllegalArgumentException) {
        }
    }

    @Test
    fun setsWithoutAWorkoutAreRejected() {
        val broken = backup.copy(sets = listOf(backup.sets[0].copy(sessionId = 99)))
        try {
            BackupCodec.decode(BackupCodec.encode(broken))
            fail("expected an exception")
        } catch (expected: IllegalArgumentException) {
        }
    }
}
