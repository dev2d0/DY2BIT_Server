package com.example.dy2bit.reservationOrder.model.dto

import com.example.dy2bit.model.ReservationOrder

data class UserReservationOrderListDTO(
    val id: Long,
    val coinName: String,
    val quantity: Float,
    val targetKimpRate: Float,
    val createdAt: Long,
    val endAt: Long?
) {
    constructor(reservationOrder: ReservationOrder) : this(
        id = reservationOrder.id,
        coinName = reservationOrder.coinName,
        quantity = reservationOrder.quantity,
        targetKimpRate = reservationOrder.targetKimpRate,
        createdAt = reservationOrder.createdAt.toEpochMilli(),
        endAt = reservationOrder.endAt?.toEpochMilli(),
    )
}
