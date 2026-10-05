package com.bharath.homeforge.data

import android.content.Context
import com.bharath.homeforge.domain.Split
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Remembers exercise swaps per split/day/slot so Routines and Log show the same plan. */
class SwapRepository private constructor(context: Context) {

    private val prefs = context.getSharedPreferences("swaps", Context.MODE_PRIVATE)
    private val state = MutableStateFlow(load())

    val swaps: StateFlow<Map<String, Int>> = state.asStateFlow()

    fun swap(split: Split, dayIndex: Int, slotIndex: Int) {
        val key = "${split.name}:$dayIndex:$slotIndex"
        val updated = state.value + (key to ((state.value[key] ?: 0) + 1))
        state.value = updated
        prefs.edit().putString(KEY_SWAPS, updated.entries.joinToString(";") { "${it.key}=${it.value}" }).apply()
    }

    private fun load(): Map<String, Int> =
        (prefs.getString(KEY_SWAPS, null) ?: "").split(';').mapNotNull { entry ->
            val pieces = entry.split('=')
            val count = pieces.getOrNull(1)?.toIntOrNull()
            if (pieces.size == 2 && count != null) pieces[0] to count else null
        }.toMap()

    companion object {
        private const val KEY_SWAPS = "swaps"

        @Volatile
        private var instance: SwapRepository? = null

        fun get(context: Context): SwapRepository =
            instance ?: synchronized(this) {
                instance ?: SwapRepository(context.applicationContext).also { instance = it }
            }

        fun offsetsFor(swaps: Map<String, Int>, split: Split, dayIndex: Int): Map<Int, Int> =
            swaps.mapNotNull { (key, count) ->
                val parts = key.split(':')
                if (parts.size == 3 && parts[0] == split.name && parts[1].toIntOrNull() == dayIndex) {
                    parts[2].toIntOrNull()?.let { it to count }
                } else {
                    null
                }
            }.toMap()
    }
}
