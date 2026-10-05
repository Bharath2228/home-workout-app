package com.bharath.homeforge.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
abstract class WorkoutDao {

    @Insert
    protected abstract suspend fun insertSession(session: WorkoutSession): Long

    @Insert
    protected abstract suspend fun insertSets(sets: List<LoggedSet>)

    @Transaction
    open suspend fun saveWorkout(session: WorkoutSession, sets: List<LoggedSet>): Long {
        val sessionId = insertSession(session)
        insertSets(sets.map { it.copy(sessionId = sessionId) })
        return sessionId
    }

    @Query("SELECT * FROM sessions ORDER BY startedAt")
    abstract suspend fun allSessions(): List<WorkoutSession>

    @Query("SELECT * FROM sessions WHERE id = :id")
    abstract suspend fun sessionById(id: Long): WorkoutSession?

    @Query("SELECT * FROM logged_sets WHERE sessionId = :sessionId ORDER BY exerciseName, setNumber")
    abstract suspend fun setsForSession(sessionId: Long): List<LoggedSet>

    /** Sets from this exercise's five most recent workouts, newest workout first. */
    @Query(
        "SELECT * FROM logged_sets WHERE exerciseName = :name AND sessionId IN " +
            "(SELECT sessionId FROM logged_sets WHERE exerciseName = :name " +
            "GROUP BY sessionId ORDER BY sessionId DESC LIMIT 5) " +
            "ORDER BY sessionId DESC, setNumber",
    )
    abstract suspend fun recentSetsFor(name: String): List<LoggedSet>

    @Query("SELECT * FROM measurements ORDER BY time")
    abstract suspend fun allMeasurementsOnce(): List<Measurement>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun restoreSessions(items: List<WorkoutSession>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun restoreSets(items: List<LoggedSet>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun restoreMeasurements(items: List<Measurement>)

    @Query("DELETE FROM sessions")
    protected abstract suspend fun deleteAllSessions()

    @Query("DELETE FROM measurements")
    protected abstract suspend fun deleteAllMeasurements()

    /** Replaces all workouts and measurements. Deleting sessions also removes their sets. */
    @Transaction
    open suspend fun replaceAll(
        sessions: List<WorkoutSession>,
        sets: List<LoggedSet>,
        measurements: List<Measurement>,
    ) {
        deleteAllSessions()
        deleteAllMeasurements()
        restoreSessions(sessions)
        restoreSets(sets)
        restoreMeasurements(measurements)
    }

    @Query("SELECT * FROM logged_sets ORDER BY loggedAt")
    abstract fun observeAllSets(): Flow<List<LoggedSet>>

    @Query("SELECT * FROM sessions ORDER BY startedAt DESC")
    abstract fun observeSessions(): Flow<List<WorkoutSession>>

    @Query("SELECT * FROM sessions WHERE splitName = :splitName AND startedAt >= :sinceMillis ORDER BY startedAt")
    abstract fun observeProgramSessions(splitName: String, sinceMillis: Long): Flow<List<WorkoutSession>>

    @Query("DELETE FROM sessions WHERE id = :id")
    abstract suspend fun deleteSession(id: Long)

    @Update
    abstract suspend fun updateSet(set: LoggedSet)

    @Delete
    abstract suspend fun deleteSet(set: LoggedSet)

    @Query("SELECT COUNT(*) FROM logged_sets WHERE sessionId = :sessionId")
    abstract suspend fun setCount(sessionId: Long): Int

    @Query("SELECT * FROM logged_sets")
    abstract suspend fun allSetsOnce(): List<LoggedSet>

    @Insert
    abstract suspend fun insertMeasurement(measurement: Measurement)

    @Delete
    abstract suspend fun deleteMeasurement(measurement: Measurement)

    @Query("SELECT * FROM measurements ORDER BY time")
    abstract fun observeMeasurements(): Flow<List<Measurement>>

    @Query("SELECT COUNT(*) FROM sessions")
    abstract fun observeSessionCount(): Flow<Int>

    /** Completed workouts that count toward your level. The optional core day is an extra and doesn't. */
    @Query("SELECT COUNT(*) FROM sessions WHERE splitName != 'CORE_DAY'")
    abstract fun observeWorkoutCount(): Flow<Int>
}
