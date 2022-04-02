package com.example.dy2bit.reservationOrder.controller

import com.example.dy2bit.reservationOrder.service.reservationOrderService
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class reservationOrderController(
        private val reservationOrderService: reservationOrderService,
) {
    @GetMapping("/api/hello")
    fun hello(): String  {
        return "true"
    }
}
