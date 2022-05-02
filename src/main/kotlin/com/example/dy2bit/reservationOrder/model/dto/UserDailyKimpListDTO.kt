package com.example.dy2bit.reservationOrder.model.dto

import com.example.dy2bit.model.Tracker

data class UserDailyKimpListDTO(
    val id: Long?,
    val minRate: Float,
    val minRateAt: Long,
    val maxRate: Float,
    val maxRateAt: Long,
    val createdAt: Long,
) {
    constructor(tracker: Tracker) : this(
        id = tracker.id,
        minRate = tracker.minRate,
        minRateAt = tracker.minRateAt.toEpochMilli(),
        maxRate = tracker.maxRate,
        maxRateAt = tracker.maxRateAt.toEpochMilli(),
        createdAt = tracker.createdAt.toEpochMilli(),
    )
}
