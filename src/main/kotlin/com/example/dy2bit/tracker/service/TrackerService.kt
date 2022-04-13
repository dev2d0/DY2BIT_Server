package com.example.dy2bit.tracker.service

import com.example.dy2bit.coinExchange.model.CoinExchangeServiceFactory
import com.example.dy2bit.coinExchange.model.type.CoinExchangeType
import com.example.dy2bit.coinExchange.service.ExchangeRateService
import com.example.dy2bit.model.Tracker
import com.example.dy2bit.repository.TrackerRepository
import com.example.dy2bit.reservationOrder.service.ReservationOrderService
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

@Service
class TrackerService(
    private val reservationOrderService: ReservationOrderService,
    private val coinExchangeServiceFactory: CoinExchangeServiceFactory,
    private val exchangeRateService: ExchangeRateService,
    private val trackerRepository: TrackerRepository,
) {
    fun createTodayKimp() {
        trackerRepository.saveAndFlush(
            Tracker(
                coinName = "BTC",
                minRate = 100F,
                maxRate = -100F,
                minRateAt = Instant.now(),
                maxRateAt = Instant.now(),
                createdAt = Instant.now(),
            )
        )
    }

    suspend fun trackerEveryJob() = coroutineScope {
        val kimpPer = async { getKimpPer() }.await()
        async {
            reservationOrderService.tradeReservationOrder(kimpPer)
            updateTodayKimpMinMaxRate(kimpPer)
        }
    }.await()

    private fun updateTodayKimpMinMaxRate(kimpPer: Float): Tracker? {
        val startDatetime = LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 0, 0)).atZone(ZoneId.of("Asia/Seoul")).toInstant() //오늘 00:00:00
        val endDatetime = LocalDateTime.of(LocalDate.now(), LocalTime.of(23, 59, 59)).atZone(ZoneId.of("Asia/Seoul")).toInstant() //오늘 23:59:59

        val target = trackerRepository.findByMaxRateGreaterThanOrMinRateLessThanAndCreatedAtLessThanAndCreatedAtGreaterThan(kimpPer, startDatetime, endDatetime)
        return if (target != null) {
            if (target.maxRate < kimpPer) {
                target.maxRate = kimpPer
                target.maxRateAt = Instant.now()
            } else if (target.minRate > kimpPer) {
                target.minRate = kimpPer
                target.minRateAt = Instant.now()
            }
            trackerRepository.save(target)
        } else null
    }

    suspend fun getKimpPer(): Float = coroutineScope {
        val getUpbitPrice = async { coinExchangeServiceFactory.coinExchangeServiceFactory(CoinExchangeType.UPBIT).getCurrentBitPrice() }
        val getBinancePrice = async { coinExchangeServiceFactory.coinExchangeServiceFactory(CoinExchangeType.BINANCE).getCurrentBitPrice() }
        val getExchangeRatePrice = async { exchangeRateService.getExchangeRatePrice() }

        val upbitPrice = getUpbitPrice.await().price
        val binancePrice = getBinancePrice.await().price
        val exchangeRatePrice = getExchangeRatePrice.await().basePrice

        // 김프 퍼센트 = (업비트 가격/(바이낸스 가격*환율)-1)*100
        val kimpPer = (upbitPrice / (binancePrice * exchangeRatePrice) - 1) * 100
        return@coroutineScope kimpPer
    }
}
