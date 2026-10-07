package com.bharath.homeforge.data

import com.bharath.homeforge.domain.Plate
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

data class Backup(
    val exportedAt: Long,
    val sessions: List<WorkoutSession>,
    val sets: List<LoggedSet>,
    val measurements: List<Measurement>,
    val plates: List<Plate>,
)

object BackupCodec {

    const val VERSION = 1

    fun encode(backup: Backup): String = JSONObject().apply {
        put("app", "HomeForge")
        put("version", VERSION)
        put("exportedAt", backup.exportedAt)
        put("plates", JSONArray().apply {
            backup.plates.forEach { put(JSONObject().put("weightKg", it.weightKg).put("count", it.count)) }
        })
        put("sessions", JSONArray().apply {
            backup.sessions.forEach {
                put(
                    JSONObject()
                        .put("id", it.id)
                        .put("startedAt", it.startedAt)
                        .put("endedAt", it.endedAt)
                        .put("splitName", it.splitName)
                        .put("dayIndex", it.dayIndex)
                        .put("warmUpDone", it.warmUpDone),
                )
            }
        })
        put("sets", JSONArray().apply {
            backup.sets.forEach {
                val set = JSONObject()
                    .put("id", it.id)
                    .put("sessionId", it.sessionId)
                    .put("exerciseName", it.exerciseName)
                    .put("setNumber", it.setNumber)
                    .put("reps", it.reps)
                    .put("loggedAt", it.loggedAt)
                if (it.weightKg != null) set.put("weightKg", it.weightKg)
                if (it.difficulty != null) set.put("difficulty", it.difficulty)
                put(set)
            }
        })
        put("measurements", JSONArray().apply {
            backup.measurements.forEach {
                put(JSONObject().put("id", it.id).put("time", it.time).put("type", it.type).put("value", it.value))
            }
        })
    }.toString(2)

    /** Throws [IllegalArgumentException] if [text] isn't a valid HomeForge backup. */
    fun decode(text: String): Backup {
        try {
            val root = JSONObject(text)
            require(root.optString("app") == "HomeForge") { "Not a HomeForge backup." }
            require(root.getInt("version") <= VERSION) { "This backup was made by a newer version of the app." }

            val sessions = root.getJSONArray("sessions").objects().map {
                WorkoutSession(
                    id = it.getLong("id"),
                    startedAt = it.getLong("startedAt"),
                    endedAt = it.getLong("endedAt"),
                    splitName = it.getString("splitName"),
                    dayIndex = it.getInt("dayIndex"),
                    warmUpDone = it.optBoolean("warmUpDone", false),
                )
            }
            val sessionIds = sessions.map { it.id }.toSet()
            val sets = root.getJSONArray("sets").objects().map {
                LoggedSet(
                    id = it.getLong("id"),
                    sessionId = it.getLong("sessionId"),
                    exerciseName = it.getString("exerciseName"),
                    setNumber = it.getInt("setNumber"),
                    weightKg = if (it.has("weightKg") && !it.isNull("weightKg")) it.getDouble("weightKg") else null,
                    reps = it.getInt("reps"),
                    loggedAt = it.getLong("loggedAt"),
                    difficulty = it.optString("difficulty", null),
                )
            }
            require(sets.all { it.sessionId in sessionIds }) { "Backup has sets that belong to no workout." }

            return Backup(
                exportedAt = root.optLong("exportedAt"),
                sessions = sessions,
                sets = sets,
                measurements = root.getJSONArray("measurements").objects().map {
                    Measurement(it.getLong("id"), it.getLong("time"), it.getString("type"), it.getDouble("value"))
                },
                plates = root.getJSONArray("plates").objects().map { Plate(it.getDouble("weightKg"), it.getInt("count")) },
            )
        } catch (e: JSONException) {
            throw IllegalArgumentException("This file isn't a valid HomeForge backup.", e)
        }
    }

    private fun JSONArray.objects(): List<JSONObject> = (0 until length()).map { getJSONObject(it) }
}
