package com.bharath.homeforge.data

import android.content.Context

class HealthSync(context: Context, private val dao: WorkoutDao) {

    val manager = HealthConnectManager(context)
    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    var enabled: Boolean
        get() = prefs.getBoolean(KEY_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_ENABLED, value).apply()

    /** Writes one saved workout if sync is on and permission is granted. Returns true if written. */
    suspend fun syncSessionIfEnabled(sessionId: Long): Boolean {
        if (!enabled || !manager.hasAllPermissions()) return false
        val session = dao.sessionById(sessionId) ?: return false
        manager.writeSessions(listOf(SessionWithSets(session, dao.setsForSession(sessionId))))
        return true
    }

    /** Re-sends every saved workout. Safe to repeat: records are keyed by a client id, so no duplicates. */
    suspend fun syncAll(): Int {
        val sessions = dao.allSessions().map { SessionWithSets(it, dao.setsForSession(it.id)) }
        manager.writeSessions(sessions)
        return sessions.size
    }

    private companion object {
        const val KEY_ENABLED = "hc_sync"
    }
}
