package com.bharath.homeforge.domain

enum class MeasurementType(
    val label: String,
    val unit: String,
    val min: Double,
    val max: Double,
) {
    BODY_WEIGHT("Body weight", "kg", 20.0, 300.0),
    WAIST("Waist", "cm", 30.0, 250.0),
    CHEST("Chest", "cm", 30.0, 250.0),
    ARM("Upper arm", "cm", 10.0, 80.0),
    THIGH("Thigh", "cm", 20.0, 120.0),
}
