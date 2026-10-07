package com.bharath.homeforge.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class ActiveSetSnapshot(val weight: String, val reps: String, val done: Boolean, val difficulty: String?)

data class ActiveExerciseSnapshot(
    val id: Int,
    val exerciseName: String,
    val sets: Int,
    val repsFirst: Int,
    val repsLast: Int,
    val weightKg: Double?,
    val restSeconds: Int,
    val lastText: String?,
    val note: String?,
    val isSuperset: Boolean,
    val setDrafts: List<ActiveSetSnapshot>,
)

data class ActiveWorkoutSnapshot(
    val splitName: String,
    val dayIndex: Int,
    val startedAt: Long,
    val pausedAt: Long?,
    val pausedMs: Long,
    val nextId: Int,
    val warmUpMoveNames: List<String>,
    val warmUpMoveDetails: List<String>,
    val warmUpDone: List<Boolean>,
    val exercises: List<ActiveExerciseSnapshot>,
)

/** Keeps the in-progress workout on disk so it survives the app process being killed, not just backgrounded. */
class ActiveWorkoutStore private constructor(context: Context) {

    private val store = context.getSharedPreferences("active_workout", Context.MODE_PRIVATE)

    fun save(snapshot: ActiveWorkoutSnapshot) {
        store.edit().putString(KEY, encode(snapshot)).apply()
    }

    fun clear() {
        store.edit().remove(KEY).apply()
    }

    /** Null if nothing is saved, or the saved JSON can no longer be parsed (e.g. after an app update). */
    fun load(): ActiveWorkoutSnapshot? {
        val text = store.getString(KEY, null) ?: return null
        return runCatching { decode(text) }.getOrNull()
    }

    private fun encode(s: ActiveWorkoutSnapshot): String = JSONObject().apply {
        put("splitName", s.splitName)
        put("dayIndex", s.dayIndex)
        put("startedAt", s.startedAt)
        if (s.pausedAt != null) put("pausedAt", s.pausedAt)
        put("pausedMs", s.pausedMs)
        put("nextId", s.nextId)
        put("warmUpMoveNames", JSONArray(s.warmUpMoveNames))
        put("warmUpMoveDetails", JSONArray(s.warmUpMoveDetails))
        put("warmUpDone", JSONArray(s.warmUpDone))
        put(
            "exercises",
            JSONArray().apply {
                s.exercises.forEach { e ->
                    put(
                        JSONObject()
                            .put("id", e.id)
                            .put("exerciseName", e.exerciseName)
                            .put("sets", e.sets)
                            .put("repsFirst", e.repsFirst)
                            .put("repsLast", e.repsLast)
                            .putOpt("weightKg", e.weightKg)
                            .put("restSeconds", e.restSeconds)
                            .putOpt("lastText", e.lastText)
                            .putOpt("note", e.note)
                            .put("isSuperset", e.isSuperset)
                            .put(
                                "setDrafts",
                                JSONArray().apply {
                                    e.setDrafts.forEach { set ->
                                        put(
                                            JSONObject()
                                                .put("weight", set.weight)
                                                .put("reps", set.reps)
                                                .put("done", set.done)
                                                .putOpt("difficulty", set.difficulty),
                                        )
                                    }
                                },
                            ),
                    )
                }
            },
        )
    }.toString()

    private fun decode(text: String): ActiveWorkoutSnapshot {
        val root = JSONObject(text)
        return ActiveWorkoutSnapshot(
            splitName = root.getString("splitName"),
            dayIndex = root.getInt("dayIndex"),
            startedAt = root.getLong("startedAt"),
            pausedAt = if (root.has("pausedAt")) root.getLong("pausedAt") else null,
            pausedMs = root.optLong("pausedMs", 0L),
            nextId = root.getInt("nextId"),
            warmUpMoveNames = root.getJSONArray("warmUpMoveNames").strings(),
            warmUpMoveDetails = root.getJSONArray("warmUpMoveDetails").strings(),
            warmUpDone = root.getJSONArray("warmUpDone").booleans(),
            exercises = root.getJSONArray("exercises").objects().map { e ->
                ActiveExerciseSnapshot(
                    id = e.getInt("id"),
                    exerciseName = e.getString("exerciseName"),
                    sets = e.getInt("sets"),
                    repsFirst = e.getInt("repsFirst"),
                    repsLast = e.getInt("repsLast"),
                    weightKg = if (e.has("weightKg")) e.getDouble("weightKg") else null,
                    restSeconds = e.getInt("restSeconds"),
                    lastText = e.optString("lastText", null),
                    note = e.optString("note", null),
                    isSuperset = e.optBoolean("isSuperset", false),
                    setDrafts = e.getJSONArray("setDrafts").objects().map { set ->
                        ActiveSetSnapshot(
                            weight = set.getString("weight"),
                            reps = set.getString("reps"),
                            done = set.getBoolean("done"),
                            difficulty = set.optString("difficulty", null),
                        )
                    },
                )
            },
        )
    }

    private fun JSONArray.objects(): List<JSONObject> = (0 until length()).map { getJSONObject(it) }
    private fun JSONArray.strings(): List<String> = (0 until length()).map { getString(it) }
    private fun JSONArray.booleans(): List<Boolean> = (0 until length()).map { getBoolean(it) }

    companion object {
        private const val KEY = "snapshot"

        @Volatile
        private var instance: ActiveWorkoutStore? = null

        fun get(context: Context): ActiveWorkoutStore =
            instance ?: synchronized(this) {
                instance ?: ActiveWorkoutStore(context.applicationContext).also { instance = it }
            }
    }
}
