package com.example.dy2bit.reservationOrder.controller

import com.example.dy2bit.coinExchange.model.dto.KimpDTO
import com.example.dy2bit.coinExchange.service.ExchangeRateService
import com.example.dy2bit.model.ReservationOrder
import com.example.dy2bit.reservationOrder.model.dto.UserReservationOrderListDTO
import com.example.dy2bit.reservationOrder.model.form.CreateReservationOrderForm
import com.example.dy2bit.reservationOrder.model.form.UpdateReservationOrderForm
import com.example.dy2bit.reservationOrder.service.ReservationOrderService
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestBody

@RestController
@CrossOrigin(origins = arrayOf("*"))
class ReservationOrderController(
    private val reservationOrderService: ReservationOrderService,
    private val exchangeRateService: ExchangeRateService,
) {
    @PostMapping("/api/reservationOrders/currentCoinPrices")
    suspend fun getCurrentCoinPrices(): KimpDTO {
        return exchangeRateService.getKimpPerAndRelatedCoinPrices()
    }

    @PostMapping("/api/reservationOrders/getReservationOrderList")
    fun getReservationOrderList(): List<UserReservationOrderListDTO> {
        return reservationOrderService.getReservationOrderList().map {
            UserReservationOrderListDTO(it)
        }
    }

    @PostMapping("/api/reservationOrders/createReservationOrder")
    @CrossOrigin(origins = arrayOf("*"))
    suspend fun createReservationOrder(@RequestBody createReservationOrderForm: CreateReservationOrderForm): UserReservationOrderListDTO {
        return UserReservationOrderListDTO(
            reservationOrderService.createReservationOrder(
                createReservationOrderForm.targetKimpRate,
                createReservationOrderForm.quantity,
                createReservationOrderForm.isBuy,
            )
        )
    }

    @PostMapping("/api/reservationOrders/updateReservationOrder")
    @CrossOrigin(origins = arrayOf("*"))
    fun updateReservationOrder(@RequestBody updateReservationOrderForm: UpdateReservationOrderForm): UserReservationOrderListDTO {
        return UserReservationOrderListDTO(
            reservationOrderService.updateReservationOrder(
                updateReservationOrderForm.id,
                updateReservationOrderForm.targetKimpRate,
                updateReservationOrderForm.unCompletedQuantity,
            )
        )
    }

    @PostMapping("/api/reservationOrders/{id}/cancelReservationOrder")
    fun cancelReservationOrder(@PathVariable id: Long): ReservationOrder {
        return reservationOrderService.cancelReservationOrder(id)
    }
}
