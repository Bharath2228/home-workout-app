package com.bharath.homeforge.domain

object Rotation {

    const val WEEKS_PER_BLOCK = 4
    private const val DAYS_PER_BLOCK = WEEKS_PER_BLOCK * 7

    /** Zero-based block number; each block shifts every exercise slot to its next variation. */
    fun blockIndex(startEpochDay: Long, todayEpochDay: Long): Int =
        (maxOf(0L, todayEpochDay - startEpochDay) / DAYS_PER_BLOCK).toInt()

    /** 1 to [WEEKS_PER_BLOCK]. */
    fun weekInBlock(startEpochDay: Long, todayEpochDay: Long): Int =
        ((maxOf(0L, todayEpochDay - startEpochDay) % DAYS_PER_BLOCK) / 7).toInt() + 1
}
