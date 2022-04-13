package com.example.dy2bit.reservationOrder.controller

import com.example.dy2bit.model.ReservationOrder
import com.example.dy2bit.reservationOrder.model.dto.UserReservationOrderListDTO
import com.example.dy2bit.reservationOrder.model.form.CreateReservationOrderForm
import com.example.dy2bit.reservationOrder.service.ReservationOrderService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestBody

@RestController
class ReservationOrderController(
    private val reservationOrderService: ReservationOrderService,
) {
    @GetMapping("/api/reservationOrders/getReservationOrderList")
    fun getReservationOrderList(): List<UserReservationOrderListDTO> {
        return reservationOrderService.getReservationOrderList().map {
            UserReservationOrderListDTO(it)
        }
    }

    @PostMapping("/api/reservationOrders/createReservationOrder")
    suspend fun createReservationOrder(@RequestBody createReservationOrderForm: CreateReservationOrderForm): UserReservationOrderListDTO {
        return UserReservationOrderListDTO(
            reservationOrderService.createReservationOrder(
                createReservationOrderForm.coinName,
                createReservationOrderForm.quantity,
                createReservationOrderForm.targetKimpRate,
                createReservationOrderForm.position,
            )
        )
    }

    @PostMapping("/api/reservationOrders/{id}/cancelReservationOrder")
    fun cancelReservationOrder(@PathVariable id: Long): ReservationOrder {
        return reservationOrderService.cancelReservationOrder(id)
    }
}
