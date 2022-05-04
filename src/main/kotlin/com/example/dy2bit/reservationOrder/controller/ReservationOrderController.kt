package com.example.dy2bit.reservationOrder.controller

import com.example.dy2bit.coinExchange.model.dto.KimpDTO
import com.example.dy2bit.coinExchange.service.ExchangeRateService
import com.example.dy2bit.error.service.ErrorService
import com.example.dy2bit.error.service.model.ErrorDTO
import com.example.dy2bit.model.ReservationOrder
import com.example.dy2bit.reservationOrder.model.dto.UserAccountDTO
import com.example.dy2bit.reservationOrder.model.dto.UserDailyKimpListDTO
import com.example.dy2bit.reservationOrder.model.dto.UserHistoryReservationOrderListDTO
import com.example.dy2bit.reservationOrder.model.dto.UserReservationOrderListDTO
import com.example.dy2bit.reservationOrder.model.form.CreateReservationOrderForm
import com.example.dy2bit.reservationOrder.model.form.DeleteHistoryReservationOrderForm
import com.example.dy2bit.reservationOrder.model.form.DeleteReservationOrderForm
import com.example.dy2bit.reservationOrder.model.form.UpdateReservationOrderForm
import com.example.dy2bit.reservationOrder.service.ReservationOrderService
import com.example.dy2bit.tracker.service.TrackerService
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestBody

@RestController
class ReservationOrderController(
    private val reservationOrderService: ReservationOrderService,
    private val trackerService: TrackerService,
    private val errorService: ErrorService,
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
        reservationOrderService.checkDy2bitSecretKey(createReservationOrderForm.secretKey)
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
        reservationOrderService.checkDy2bitSecretKey(updateReservationOrderForm.secretKey)
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
        reservationOrderService.checkDy2bitSecretKey(deleteReservationOrderForm.secretKey)
        return reservationOrderService.deleteReservationOrder(deleteReservationOrderForm.id)
    }

    @PostMapping("/api/reservationOrders/getHistoryReservationOrderList")
    @CrossOrigin(origins = ["*"])
    fun historyReservationOrderList(): List<UserHistoryReservationOrderListDTO> {
        return reservationOrderService.getHistoryReservationOrderList().map {
            UserHistoryReservationOrderListDTO(it)
        }
    }

    @PostMapping("/api/reservationOrders/deleteHistoryReservationOrder")
    @CrossOrigin(origins = ["*"])
    fun deleteHistoryReservationOrder(@RequestBody deleteHistoryReservationOrderForm: DeleteHistoryReservationOrderForm) {
        return reservationOrderService.deleteHistoryReservationOrder(deleteHistoryReservationOrderForm.id)
    }

    @PostMapping("/api/reservationOrders/getDailyKimpList")
    @CrossOrigin(origins = ["*"])
    fun dailyKimpList(): List<UserDailyKimpListDTO> {
        return trackerService.getDailyKimpList().map {
            UserDailyKimpListDTO(it)
        }
    }

    @PostMapping("/api/reservationOrders/getErrorReport")
    @CrossOrigin(origins = ["*"])
    fun getErrorReport(): ErrorDTO {
        return ErrorDTO(errorService.getError())
    }

    @PostMapping("/api/reservationOrders/confirmErrorReport")
    @CrossOrigin(origins = ["*"])
    fun confirmErrorReport(): ErrorDTO {
        return errorService.confirmErrorReport()
    }
}
