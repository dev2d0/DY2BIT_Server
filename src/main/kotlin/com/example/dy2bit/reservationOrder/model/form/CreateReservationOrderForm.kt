package com.example.dy2bit.reservationOrder.model.form

data class CreateReservationOrderForm(
    val targetKimpRate: Float,
    val quantity: Float,
    val secretKey: String,
    val isBuy: Boolean,
)
