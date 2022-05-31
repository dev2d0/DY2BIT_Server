package com.example.dy2bit.reservationOrder.service

import com.example.dy2bit.coinExchange.model.dto.KimpDTO
import com.example.dy2bit.coinExchange.service.BinanceCoinExchangeService
import com.example.dy2bit.coinExchange.service.ExchangeRateService
import com.example.dy2bit.coinExchange.service.UpbitCoinExchangeService
import com.example.dy2bit.error.service.ErrorService
import com.example.dy2bit.model.ReservationOrder
import com.example.dy2bit.repository.ReservationOrderRepository
import com.example.dy2bit.reservationOrder.model.dto.UserAccountDTO
import com.example.dy2bit.utils.exception.Dy2bitException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class ReservationOrderService(
    private val reservationOrderRepository: ReservationOrderRepository,
    private val exchangeRateService: ExchangeRateService,
    private val upbitCoinExchangeService: UpbitCoinExchangeService,
    private val errorService: ErrorService,
    private val binanceCoinExchangeService: BinanceCoinExchangeService,
    @Value("\${dy2bit-secret.key}") private val dy2bitSecretKey: String,
) {
    companion object {
        const val DEFAULT_ORDER: Float = 0.03F
    }

    private val logger = LoggerFactory.getLogger(this::class.java)

    @Transactional
    fun getReservationOrderList(): List<ReservationOrder> {
        return reservationOrderRepository.findByEndAtIsNull()
    }

    @Transactional
    fun getAliveBuyOneReservationOrder(): ReservationOrder? {
        return getReservationOrderList()
            .filter { it.position }
            .maxByOrNull { it.targetKimpRate }
    }

    @Transactional
    fun getAliveSellOneReservationOrder(): ReservationOrder? {
        return getReservationOrderList()
            .filter { !it.position }
            .minByOrNull { it.targetKimpRate }
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
        if (errorService.getError().errorFoundedAt != null) return@coroutineScope false else {
            // 1. 김프 매수, 매도 타겟 하나씩 가져오기
            // 2. 김프 가격과 매수 타겟 비교 -> 맞으면 업비트, 바이낸스 주문 가능한지 조사 -> 매수
            // 3. 김프 가격과 매도 타겟 비교 -> 맞으면 업비트, 바이낸스 주문 가능한지 조사 -> 매도
            val aliveBuyOneReservationOrder = getAliveBuyOneReservationOrder()
            val aliveSellOneReservationOrder = getAliveSellOneReservationOrder()
            val isBuyReservationGoalReached = aliveBuyOneReservationOrder != null && aliveBuyOneReservationOrder.targetKimpRate > kimp.kimpPer
            val isSellReservationGoalReached = aliveSellOneReservationOrder != null && aliveSellOneReservationOrder.targetKimpRate < kimp.kimpPer
            if (isBuyReservationGoalReached) {
                if (isBuyTradePossible(kimp.upbitPrice, kimp.binancePrice, aliveBuyOneReservationOrder!!.unCompletedQuantity)) {
                    try {
                        tradeReservationOrder(aliveBuyOneReservationOrder, true, kimp.upbitPrice)
                    } catch (e: Error) {
                    }
                }
            }
            if (isSellReservationGoalReached) {
                if (isSellTradePossible(aliveSellOneReservationOrder!!.unCompletedQuantity)) {
                    try {
                        tradeReservationOrder(aliveSellOneReservationOrder, false, kimp.upbitPrice)
                    } catch (e: Error) {
                    }
                } else false
            } else false
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

    private suspend fun tradeReservationOrder(reservationOrder: ReservationOrder, isBuy: Boolean, upbitPrice: Float) = coroutineScope {
        val quantity = if (reservationOrder.unCompletedQuantity > DEFAULT_ORDER) DEFAULT_ORDER else reservationOrder.unCompletedQuantity
        val buyPrice = if (isBuy) Math.round(quantity * upbitPrice) else null
        try {
            val upbitTrade = async { upbitCoinExchangeService.tradeCoin(isBuy, quantity, buyPrice) }.await()
            logger.info("upbit에서 주문 발생 $upbitTrade")
            if (upbitTrade.error != null) errorService.reportError("Upbit", upbitTrade.error.toString())

            val binanceTrade = binanceCoinExchangeService.tradeCoin(!isBuy, quantity)
            logger.info("binance에서 주문 발생 $binanceTrade")
            if (binanceTrade.code != null && binanceTrade.msg != null) errorService.reportError("Binance", "${binanceTrade.code}${binanceTrade.msg}")
            completedTrade(reservationOrder, quantity)
        } catch (e: Exception) {
            logger.info("에러 발생 $e")
        }
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

    fun checkDy2bitSecretKey(secretKey: String): Boolean {
        if (secretKey != dy2bitSecretKey) {
            throw Dy2bitException("시크릿 키가 일치하지 않습니다.")
        } else {
            return true
        }
    }

    @Transactional
    fun getHistoryReservationOrderList(): List<ReservationOrder> {
        return reservationOrderRepository.findByEndAtIsNotNullOrderByCreatedAtDesc()
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    fun deleteHistoryReservationOrder(id: Long) {
        return reservationOrderRepository.deleteById(id)
    }
}
