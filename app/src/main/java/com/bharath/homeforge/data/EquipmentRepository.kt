package com.bharath.homeforge.data

import android.content.Context
import com.bharath.homeforge.domain.Equipment
import com.bharath.homeforge.domain.EquipmentCodec
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class EquipmentRepository private constructor(context: Context) {

    private val prefs = context.getSharedPreferences("equipment", Context.MODE_PRIVATE)
    private val state = MutableStateFlow(load())

    val equipment: StateFlow<Equipment> = state.asStateFlow()

    fun save(equipment: Equipment) {
        state.value = equipment
        prefs.edit()
            .putString(KEY_PLATES, EquipmentCodec.encodePlates(equipment.plates))
            .apply()
    }

    private fun load(): Equipment {
        val raw = prefs.getString(KEY_PLATES, null) ?: return Equipment.Default
        return Equipment(EquipmentCodec.decodePlates(raw))
    }

    companion object {
        private const val KEY_PLATES = "plates"

        @Volatile
        private var instance: EquipmentRepository? = null

        fun get(context: Context): EquipmentRepository =
            instance ?: synchronized(this) {
                instance ?: EquipmentRepository(context.applicationContext).also { instance = it }
            }
    }
}
