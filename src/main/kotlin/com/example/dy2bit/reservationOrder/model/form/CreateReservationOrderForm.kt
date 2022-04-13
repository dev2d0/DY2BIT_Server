package com.example.dy2bit.reservationOrder.model.form

data class CreateReservationOrderForm(
    val coinName: String = "BTC",
    val quantity: Float,
    val targetKimpRate: Float,
    val position: Boolean,
)
