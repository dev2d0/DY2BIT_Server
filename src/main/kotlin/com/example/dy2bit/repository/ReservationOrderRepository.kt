package com.example.dy2bit.repository

import com.example.dy2bit.model.ReservationOrder
import org.springframework.data.jpa.repository.JpaRepository

interface ReservationOrderRepository : JpaRepository<ReservationOrder, Long> {
    fun findByEndAtNotNull(): List<ReservationOrder>
    fun findByTargetKimpRateAndPositionAndEndAtNotNull(targetKimpRate: Float, position: Boolean): List<ReservationOrder>
}
