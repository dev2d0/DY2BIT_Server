package com.example.dy2bit.reservationOrder.model.dto

import com.example.dy2bit.model.ReservationOrder

data class UserHistoryReservationOrderListDTO(
    val id: Long?,
    val coinName: String,
    val completedQuantity: Float?,
    val position: Boolean,
    val targetKimpRate: Float,
    val createdAt: Long,
    val endAt: Long?
) {
    constructor(reservationOrder: ReservationOrder) : this(
        id = reservationOrder.id,
        coinName = reservationOrder.coinName,
        completedQuantity = reservationOrder.completedQuantity,
        position = reservationOrder.position,
        targetKimpRate = reservationOrder.targetKimpRate,
        createdAt = reservationOrder.createdAt.toEpochMilli(),
        endAt = reservationOrder.endAt?.toEpochMilli(),
    )
}
