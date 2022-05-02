package com.example.dy2bit.repository

import com.example.dy2bit.model.ReservationOrder
import org.springframework.data.jpa.repository.JpaRepository

interface ReservationOrderRepository : JpaRepository<ReservationOrder, Long> {
    fun findByEndAtIsNull(): List<ReservationOrder>
    fun findByEndAtIsNotNullOrderByCreatedAtDesc(): List<ReservationOrder>
}
