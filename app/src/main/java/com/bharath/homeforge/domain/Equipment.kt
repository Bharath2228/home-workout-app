package com.bharath.homeforge.domain

data class Plate(val weightKg: Double, val count: Int)

enum class Rig(val label: String) {
    SINGLE_DUMBBELL("Single dumbbell"),
    DUMBBELL_PAIR("Dumbbell pair"),
    BARBELL("Barbell"),
}

data class Equipment(
    val plates: List<Plate>,
    val rodWeightKg: Double = 1.0,
) {
    companion object {
        val Default = Equipment(
            plates = listOf(
                Plate(5.0, 4),
                Plate(2.5, 4),
                Plate(1.5, 4),
                Plate(1.0, 4),
            ),
        )
    }
}
