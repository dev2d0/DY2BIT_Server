package com.example.dy2bit.reservationOrder.controller

import com.example.dy2bit.reservationOrder.service.ReservationOrderService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class ReservationOrderController(
        private val reservationOrderService: ReservationOrderService,
) {
    @GetMapping("/api/hello")
    fun hello(): String  {
        return "true"
    }
}
