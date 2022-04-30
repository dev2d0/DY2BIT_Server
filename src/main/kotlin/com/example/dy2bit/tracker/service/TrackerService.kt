package com.example.dy2bit.tracker.service

import com.example.dy2bit.coinExchange.service.ExchangeRateService
import com.example.dy2bit.model.Tracker
import com.example.dy2bit.repository.TrackerRepository
import com.example.dy2bit.reservationOrder.service.ReservationOrderService
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Service
import java.time.*

@Service
class TrackerService(
    private val reservationOrderService: ReservationOrderService,
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
        val kimpPer = async { exchangeRateService.getKimpPerAndRelatedCoinPrices() }.await()
        async {
            reservationOrderService.tradeReservationOrder(kimpPer)
            updateTodayKimpMinMaxRate(kimpPer.kimpPer)
        }
    }.await()

    private fun updateTodayKimpMinMaxRate(kimpPer: Float): Tracker? {
        val startDatetime = LocalDateTime.now().with(LocalTime.MIN).toInstant(ZoneOffset.UTC)
        val endDatetime = LocalDateTime.now().with(LocalTime.MAX).toInstant(ZoneOffset.UTC)
        val now = LocalDateTime.now()
        println(startDatetime)
        println(endDatetime)
        println(now)
        val target = trackerRepository.findByCreatedAtBetweenAndMaxRateLessThanOrMinRateGreaterThan(startDatetime, endDatetime, kimpPer, kimpPer)
        println(target.last().toString())
        return if (target.isNotEmpty()) {
            val targetLast = target.last()
            if (targetLast!!.maxRate < kimpPer) {
                targetLast.maxRate = kimpPer
                targetLast.maxRateAt = Instant.now()
            }
            if (targetLast.minRate > kimpPer) {
                targetLast.minRate = kimpPer
                targetLast.minRateAt = Instant.now()
            }
            trackerRepository.save(targetLast)
        } else null
    }
}
