package com.example.dy2bit.reservationOrder.controller

import com.example.dy2bit.coinExchange.model.dto.KimpDTO
import com.example.dy2bit.coinExchange.service.ExchangeRateService
import com.example.dy2bit.model.ReservationOrder
import com.example.dy2bit.reservationOrder.model.dto.UserAccountDTO
import com.example.dy2bit.reservationOrder.model.dto.UserReservationOrderListDTO
import com.example.dy2bit.reservationOrder.model.form.CreateReservationOrderForm
import com.example.dy2bit.reservationOrder.model.form.DeleteReservationOrderForm
import com.example.dy2bit.reservationOrder.model.form.UpdateReservationOrderForm
import com.example.dy2bit.reservationOrder.service.ReservationOrderService
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestBody

@RestController
class ReservationOrderController(
    private val reservationOrderService: ReservationOrderService,
    private val exchangeRateService: ExchangeRateService,
) {
    @PostMapping("/api/reservationOrders/currentCoinPrices")
    @CrossOrigin(origins = ["*"])
    suspend fun getCurrentCoinPrices(): KimpDTO {
        return exchangeRateService.getKimpPerAndRelatedCoinPrices()
    }

    @PostMapping("/api/reservationOrders/getUserAccount")
    @CrossOrigin(origins = ["*"])
    suspend fun getUserAccount(): UserAccountDTO {
        return reservationOrderService.getUserAccount()
    }

    @PostMapping("/api/reservationOrders/getReservationOrderList")
    @CrossOrigin(origins = ["*"])
    fun getReservationOrderList(): List<UserReservationOrderListDTO> {
        return reservationOrderService.getReservationOrderList().map {
            UserReservationOrderListDTO(it)
        }
    }

    @PostMapping("/api/reservationOrders/createReservationOrder")
    @CrossOrigin(origins = ["*"])
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
    @CrossOrigin(origins = ["*"])
    fun updateReservationOrder(@RequestBody updateReservationOrderForm: UpdateReservationOrderForm): UserReservationOrderListDTO {
        return UserReservationOrderListDTO(
            reservationOrderService.updateReservationOrder(
                updateReservationOrderForm.id,
                updateReservationOrderForm.targetKimpRate,
                updateReservationOrderForm.unCompletedQuantity,
            )
        )
    }

    @PostMapping("/api/reservationOrders/deleteReservationOrder")
    @CrossOrigin(origins = ["*"])
    fun deleteReservationOrder(@RequestBody deleteReservationOrderForm: DeleteReservationOrderForm): ReservationOrder {
        return reservationOrderService.deleteReservationOrder(deleteReservationOrderForm.id)
    }
}
