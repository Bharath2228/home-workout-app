package com.bharath.homeforge.data

import android.content.Context
import com.bharath.homeforge.domain.MeasurementType

class HealthSync(context: Context, private val dao: WorkoutDao) {

    val manager = HealthConnectManager(context)
    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    var enabled: Boolean
        get() = prefs.getBoolean(KEY_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_ENABLED, value).apply()

    var bodyWeightKg: Double?
        get() = prefs.getFloat(KEY_BODY_WEIGHT, 0f).takeIf { it > 0f }?.toDouble()
        set(value) = prefs.edit().putFloat(KEY_BODY_WEIGHT, (value ?: 0.0).toFloat()).apply()

    /** Saves the weight locally, and also writes it to Health Connect when access is granted. Returns true if written there. */
    suspend fun saveBodyWeight(kg: Double): Boolean {
        dao.insertMeasurement(Measurement(time = System.currentTimeMillis(), type = MeasurementType.BODY_WEIGHT.name, value = kg))
        bodyWeightKg = kg
        if (!manager.hasAllPermissions()) return false
        manager.writeWeight(kg)
        return true
    }

    /** Writes one saved workout if sync is on and permission is granted. Returns true if written. */
    suspend fun syncSessionIfEnabled(sessionId: Long): Boolean {
        if (!enabled || !manager.hasAllPermissions()) return false
        val session = dao.sessionById(sessionId) ?: return false
        manager.writeSessions(listOf(SessionWithSets(session, dao.setsForSession(sessionId))), bodyWeightKg)
        return true
    }

    /** Removes a deleted workout from Health Connect, if access is granted. */
    suspend fun deleteSession(sessionId: Long) {
        if (manager.hasAllPermissions()) manager.deleteSession(sessionId)
    }

    /** Re-sends every saved workout. Safe to repeat: records are keyed by a client id, so no duplicates. */
    suspend fun syncAll(): Int {
        val sessions = dao.allSessions().map { SessionWithSets(it, dao.setsForSession(it.id)) }
        manager.writeSessions(sessions, bodyWeightKg)
        return sessions.size
    }

    private companion object {
        const val KEY_ENABLED = "hc_sync"
        const val KEY_BODY_WEIGHT = "body_weight_kg"
    }
}
