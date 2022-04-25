package com.example.dy2bit.reservationOrder.model.form

data class UpdateReservationOrderForm(
    val id: Long,
    val targetKimpRate: Float,
    val unCompletedQuantity: Float,
)
