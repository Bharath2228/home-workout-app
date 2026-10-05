package com.bharath.homeforge.domain

object EquipmentCodec {

    fun encodePlates(plates: List<Plate>): String =
        plates.joinToString(";") { "${it.weightKg}:${it.count}" }

    fun decodePlates(raw: String): List<Plate> =
        raw.split(';').mapNotNull { part ->
            val pieces = part.split(':')
            if (pieces.size != 2) return@mapNotNull null
            val weight = pieces[0].toDoubleOrNull()
            val count = pieces[1].toIntOrNull()
            if (weight == null || count == null || weight <= 0.0 || count <= 0) null else Plate(weight, count)
        }
}
