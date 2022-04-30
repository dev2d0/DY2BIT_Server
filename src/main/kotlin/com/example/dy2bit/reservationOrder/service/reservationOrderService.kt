package com.example.dy2bit.reservationOrder.service

import com.example.dy2bit.coinExchange.model.dto.KimpDTO
import com.example.dy2bit.coinExchange.service.BinanceCoinExchangeService
import com.example.dy2bit.coinExchange.service.ExchangeRateService
import com.example.dy2bit.coinExchange.service.UpbitCoinExchangeService
import com.example.dy2bit.model.ReservationOrder
import com.example.dy2bit.repository.ReservationOrderRepository
import com.example.dy2bit.reservationOrder.model.dto.UserAccountDTO
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class ReservationOrderService(
    private val reservationOrderRepository: ReservationOrderRepository,
    private val exchangeRateService: ExchangeRateService,
    private val upbitCoinExchangeService: UpbitCoinExchangeService,
    private val binanceCoinExchangeService: BinanceCoinExchangeService,
) {
    companion object {
        const val DEFAULT_ORDER = 0.03
    }

    @Transactional
    fun getReservationOrderList(): List<ReservationOrder> {
        return reservationOrderRepository.findByEndAtIsNull()
    }

    @Transactional
    fun getAliveBuyOneReservationOrder(): ReservationOrder? {
        return getReservationOrderList()
            .filter { it.position }
            .minByOrNull { it.createdAt }
    }

    @Transactional
    fun getAliveSellOneReservationOrder(): ReservationOrder? {
        return getReservationOrderList()
            .filter { !it.position }
            .minByOrNull { it.createdAt }
    }

    suspend fun getUserAccount(): UserAccountDTO {
        val upbitAccount = upbitCoinExchangeService.getAccount()
        val binanceAccount = binanceCoinExchangeService.getAccount()
        return UserAccountDTO(upbitAccount, binanceAccount)
    }

    @Transactional
    suspend fun createReservationOrder(targetKimpRate: Float, quantity: Float, position: Boolean): ReservationOrder {
        val kimp = exchangeRateService.getKimpPerAndRelatedCoinPrices().kimpPer
        val exchangeRatePrice = exchangeRateService.getExchangeRatePrice().basePrice
        return reservationOrderRepository.saveAndFlush(
            ReservationOrder(
                coinName = "BTC",
                unCompletedQuantity = quantity,
                targetKimpRate = targetKimpRate,
                curKimp = kimp,
                curExchangeRatePrice = exchangeRatePrice,
                position = position,
                createdAt = Instant.now()
            )
        )
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    fun updateReservationOrder(id: Long, targetKimpRate: Float, unCompletedQuantity: Float): ReservationOrder {
        val updatedReservationOrder = reservationOrderRepository.findById(id).get()
        updatedReservationOrder.targetKimpRate = targetKimpRate
        updatedReservationOrder.unCompletedQuantity = unCompletedQuantity
        return reservationOrderRepository.save(updatedReservationOrder)
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    fun deleteReservationOrder(id: Long): ReservationOrder {
        val deletedReservation = reservationOrderRepository.findById(id).get()
        deletedReservation.unCompletedQuantity = 0F
        deletedReservation.endAt = Instant.now()
        return reservationOrderRepository.save(deletedReservation)
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    suspend fun tradeReservationOrder(kimp: KimpDTO) = coroutineScope {
        // 1. 김프 매수, 매도 타겟 하나씩 가져오기
        // 2. 김프 가격과 매수 타겟 비교 -> 맞으면 업비트, 바이낸스 주문 가능한지 조사 -> 매수
        // 3. 김프 가격과 매도 타겟 비교 -> 맞으면 업비트, 바이낸스 주문 가능한지 조사 -> 매도
        val aliveBuyOneReservationOrder = getAliveBuyOneReservationOrder()
        val aliveSellOneReservationOrder = getAliveSellOneReservationOrder()
        val isBuyReservationGoalReached = if (aliveBuyOneReservationOrder != null) aliveBuyOneReservationOrder.targetKimpRate < kimp.kimpPer else false
        val isSellReservationGoalReached = if (aliveSellOneReservationOrder != null) aliveSellOneReservationOrder?.targetKimpRate > kimp.kimpPer else false

        if (isBuyReservationGoalReached) {
            if (isBuyTradePossible(kimp.upbitPrice, kimp.binancePrice, aliveBuyOneReservationOrder!!.unCompletedQuantity)) {
                try {
                    tradeReservationOrder(aliveBuyOneReservationOrder, true)
                } catch (e: Error) {
                }
            }
        }
        if (isSellReservationGoalReached) {
            if (isSellTradePossible(aliveSellOneReservationOrder!!.unCompletedQuantity)) {
                try {
                    tradeReservationOrder(aliveSellOneReservationOrder, true)
                } catch (e: Error) {
                }
            }
        }
    }

    private suspend fun isBuyTradePossible(upbitPrice: Float, binancePrice: Float, unCompletedQuantity: Float): Boolean = coroutineScope {
        val isUpbitBuyTradePossible = async { upbitCoinExchangeService.isBuyTradePossible(upbitPrice, unCompletedQuantity) }
        val isBinanceBuyTradePossible = async { binanceCoinExchangeService.isBuyTradePossible(binancePrice, unCompletedQuantity) }

        return@coroutineScope isUpbitBuyTradePossible.await() && isBinanceBuyTradePossible.await()
    }

    private suspend fun isSellTradePossible(unCompletedQuantity: Float): Boolean = coroutineScope {
        val isUpbitSellTradePossible = async { upbitCoinExchangeService.isSellTradePossible(unCompletedQuantity) }
        val isBinanceSellTradePossible = async { binanceCoinExchangeService.isSellTradePossible(unCompletedQuantity) }
        return@coroutineScope isUpbitSellTradePossible.await() && isBinanceSellTradePossible.await()
    }

    private suspend fun tradeReservationOrder(reservationOrder: ReservationOrder, isBuy: Boolean) = coroutineScope {
        val quantity = if (reservationOrder.unCompletedQuantity > DEFAULT_ORDER) DEFAULT_ORDER else reservationOrder.unCompletedQuantity
        // TODO: 알고리즘 테스트 이후에 주석 풀 것 (실제 거래)
//        withContext(Dispatchers.Default) {
//            upbitCoinExchangeService.tradeCoin(isBuy, quantity.toFloat())
//            binanceCoinExchangeService.tradeCoin(!isBuy, quantity.toFloat())
//        }
        completedTrade(reservationOrder, quantity.toFloat())
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    fun completedTrade(reservationOrder: ReservationOrder, quantity: Float): ReservationOrder {
        return if (reservationOrder.unCompletedQuantity - quantity > 0) {
            reservationOrder.unCompletedQuantity = reservationOrder.unCompletedQuantity - quantity
            reservationOrder.completedQuantity = reservationOrder.completedQuantity?.plus(quantity)
            reservationOrderRepository.saveAndFlush(reservationOrder)
        } else {
            reservationOrder.unCompletedQuantity = reservationOrder.unCompletedQuantity - quantity
            reservationOrder.completedQuantity = reservationOrder.completedQuantity?.plus(quantity)
            reservationOrder.endAt = Instant.now()
            reservationOrderRepository.saveAndFlush(reservationOrder)
        }
    }
}
