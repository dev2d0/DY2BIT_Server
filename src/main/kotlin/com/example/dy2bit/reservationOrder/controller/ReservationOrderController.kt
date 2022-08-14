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
import com.example.dy2bit.utils.UtilService
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.RequestBody

@RestController
class ReservationOrderController(
    private val reservationOrderService: ReservationOrderService,
    private val trackerService: TrackerService,
    private val errorService: ErrorService,
    private val exchangeRateService: ExchangeRateService,
    private val utilService: UtilService,
) {
    @GetMapping("/api/reservation-orders/current-coin-prices")
    suspend fun getCurrentCoinPrices(): KimpDTO {
        return exchangeRateService.getKimpPerAndRelatedCoinPrices()
    }

    @GetMapping("/api/reservation-orders/user-account")
    suspend fun getUserAccount(): UserAccountDTO {
        return reservationOrderService.getUserAccount()
    }

    @GetMapping("/api/reservation-orders/list")
    fun getReservationOrderList(): List<UserReservationOrderListDTO> {
        return reservationOrderService.getReservationOrderList().map {
            UserReservationOrderListDTO(it)
        }
    }

    @PostMapping("/api/reservation-orders")
    suspend fun createReservationOrder(@RequestBody createReservationOrderForm: CreateReservationOrderForm): UserReservationOrderListDTO {
        utilService.checkDy2bitSecretKey(createReservationOrderForm.secretKey)
        return UserReservationOrderListDTO(
            reservationOrderService.createReservationOrder(
                createReservationOrderForm.targetKimpRate,
                createReservationOrderForm.quantity,
                createReservationOrderForm.isBuy,
            )
        )
    }

    @PutMapping("/api/reservation-orders")
    fun updateReservationOrder(@RequestBody updateReservationOrderForm: UpdateReservationOrderForm): UserReservationOrderListDTO {
        utilService.checkDy2bitSecretKey(updateReservationOrderForm.secretKey)
        return UserReservationOrderListDTO(
            reservationOrderService.updateReservationOrder(
                updateReservationOrderForm.id,
                updateReservationOrderForm.targetKimpRate,
                updateReservationOrderForm.unCompletedQuantity,
            )
        )
    }

    @DeleteMapping("/api/reservation-orders")
    fun deleteReservationOrder(@RequestBody deleteReservationOrderForm: DeleteReservationOrderForm): ReservationOrder {
        utilService.checkDy2bitSecretKey(deleteReservationOrderForm.secretKey)
        return reservationOrderService.deleteReservationOrder(deleteReservationOrderForm.id)
    }

    @GetMapping("/api/reservation-orders/histories")
    fun historyReservationOrderList(): List<UserHistoryReservationOrderListDTO> {
        return reservationOrderService.getHistoryReservationOrderList().map {
            UserHistoryReservationOrderListDTO(it)
        }
    }

    @DeleteMapping("/api/reservation-orders/histories")
    fun deleteHistoryReservationOrder(@RequestBody deleteHistoryReservationOrderForm: DeleteHistoryReservationOrderForm) {
        return reservationOrderService.deleteHistoryReservationOrder(deleteHistoryReservationOrderForm.id)
    }

    @GetMapping("/api/reservation-orders/daily-kimp-list")
    fun dailyKimpList(): List<UserDailyKimpListDTO> {
        return trackerService.getDailyKimpList().map {
            UserDailyKimpListDTO(it)
        }
    }

    @GetMapping("/api/reservation-orders/error-report")
    fun getErrorReport(): ErrorDTO {
        return ErrorDTO(errorService.getError())
    }

    @PostMapping("/api/reservation-orders/confirm-error-report")
    fun confirmErrorReport(): ErrorDTO {
        return errorService.confirmErrorReport()
    }
}
