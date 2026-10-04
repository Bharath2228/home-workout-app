package com.bharath.homeforge.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
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

    @Query(
        "SELECT * FROM logged_sets WHERE exerciseName = :name AND sessionId = " +
            "(SELECT sessionId FROM logged_sets WHERE exerciseName = :name ORDER BY loggedAt DESC LIMIT 1) " +
            "ORDER BY setNumber",
    )
    abstract suspend fun lastSetsFor(name: String): List<LoggedSet>

    @Query("SELECT * FROM logged_sets ORDER BY loggedAt")
    abstract fun observeAllSets(): Flow<List<LoggedSet>>

    @Query("SELECT COUNT(*) FROM sessions")
    abstract fun observeSessionCount(): Flow<Int>
}
