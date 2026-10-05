package com.bharath.homeforge.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class EquipmentCodecTest {

    @Test
    fun roundTripsDefaultPlates() {
        val plates = Equipment.Default.plates
        assertEquals(plates, EquipmentCodec.decodePlates(EquipmentCodec.encodePlates(plates)))
    }

    @Test
    fun emptyStringMeansNoPlates() {
        assertEquals(emptyList<Plate>(), EquipmentCodec.decodePlates(""))
    }

    @Test
    fun badEntriesAreSkipped() {
        val decoded = EquipmentCodec.decodePlates("5.0:4;oops;2.5:x;-1:3;1.0:0;1.5:2")
        assertEquals(listOf(Plate(5.0, 4), Plate(1.5, 2)), decoded)
    }
}
