package com.example.dy2bit.reservationOrder.service

import com.example.dy2bit.coinExchange.service.ExchangeRateService
import com.example.dy2bit.model.ReservationOrder
import com.example.dy2bit.repository.ReservationOrderRepository
import com.example.dy2bit.tracker.service.TrackerService
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class ReservationOrderService(
    private val reservationOrderRepository: ReservationOrderRepository,
    private val trackerService: TrackerService,
    private val exchangeRateService: ExchangeRateService,
) {

    @Transactional
    fun getReservationOrderList(): List<ReservationOrder> {
        return reservationOrderRepository.findByEndAtNotNull()
    }

    @Transactional
    suspend fun createReservationOrder(coinName: String, quantity: Float, targetKimpRate: Float, position: Boolean): ReservationOrder {
        val kimp = trackerService.getKimpPer()
        val exchangeRatePrice = exchangeRateService.getExchangeRatePrice().basePrice
        return reservationOrderRepository.saveAndFlush(
            ReservationOrder(
                coinName = coinName,
                quantity = quantity,
                targetKimpRate = targetKimpRate,
                curKimp = kimp,
                curExchangeRatePrice = exchangeRatePrice,
                position = position,
                createdAt = Instant.now()
            )
        )
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    fun cancelReservationOrder(id: Long): ReservationOrder {
        val cancelReservation = reservationOrderRepository.findById(id).get()
        cancelReservation.endAt = Instant.now()
        return reservationOrderRepository.save(cancelReservation)
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    suspend fun tradeReservationOrder(kimpPer: Float) = coroutineScope {
        val targetBuyReservation = reservationOrderRepository.findByTargetKimpRateAndPositionAndEndAtNotNull(kimpPer, true)
        val targetSellReservation = reservationOrderRepository.findByTargetKimpRateAndPositionAndEndAtNotNull(kimpPer, false)
        if (targetBuyReservation.isNotEmpty()) {
            targetBuyReservation.map { reservationOrder ->
                //  isPossibleTrade()
                tradeReservationOrder(reservationOrder, true)
            }
        }
        if (targetSellReservation.isNotEmpty()) {
            // TODO: 둘 중 하나만 체결되는 문제를 해결하기 위해 업비트, 바이낸스 잔고 조회 해서 두 거래소 모두 주문 가능한 수량인가 먼저 체크하는 로직 필요
            targetBuyReservation.map { tradeReservationOrder(it, false) }
        }
    }

    // TODO: 둘 중 하나만 체결되는 문제를 해결하기 위해 업비트, 바이낸스 잔고 조회 해서 두 거래소 모두 주문 가능한 수량인가 먼저 체크하는 로직 필요
    private suspend fun isPossibleTrade(): Boolean = coroutineScope {
        // 잔고의 10프로는 여분으로 둠
        // 업비트와 바이낸스 각각 계좌에서 10프로를 뺀 가격에서 주문 수량 * 가격이 잔여액을 넘는가 체크
        val isUpbitPossible = async { getUpbitAccountAndCheckTradePossible() }
        val isBinancePossible = async { getBinanceAccountAndCheckTradePossible() }
        return@coroutineScope isUpbitPossible.await() && isBinancePossible.await()
    }

    // TODO: 업비트 계좌 조회 & 주문 가능 여부
    private fun getUpbitAccountAndCheckTradePossible(): Boolean {
        return true
    }

    // TODO: 바이낸스 계좌 조회 & 주문 가능 여부
    private fun getBinanceAccountAndCheckTradePossible(): Boolean {
        return true
    }

    private suspend fun tradeReservationOrder(reservationOrder: ReservationOrder, isBuy: Boolean) = coroutineScope {
        async {
            tradeUpbit(isBuy, reservationOrder.quantity)
            tradeBinance(!isBuy, reservationOrder.quantity)
        }.await()
        completedTrade(reservationOrder)
    }

    // TODO: 업비트로 코인 매수, 매도 주문 요청 로직
    private fun tradeUpbit(position: Boolean, quantity: Float) {
    }

    // TODO: 바이낸스로 코인 매수, 매도 주문 요청 로직
    private fun tradeBinance(position: Boolean, quantity: Float) {
    }

    private fun completedTrade(reservationOrder: ReservationOrder): ReservationOrder {
        reservationOrder.endAt = Instant.now()
        return reservationOrderRepository.saveAndFlush(reservationOrder)
    }
}
