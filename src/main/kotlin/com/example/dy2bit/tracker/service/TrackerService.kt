package com.example.dy2bit.tracker.service

import com.example.dy2bit.coinExchange.service.ExchangeRateService
import com.example.dy2bit.model.Tracker
import com.example.dy2bit.repository.TrackerRepository
import com.example.dy2bit.reservationOrder.service.ReservationOrderService
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
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

    fun trackerEveryJob() {
        runBlocking {
            val kimpPer = async { exchangeRateService.getKimpPerAndRelatedCoinPrices() }.await()
            async {
                reservationOrderService.tradeReservationOrder(kimpPer)
                updateTodayKimpMinMaxRate(kimpPer.kimpPer)
            }
        }
    }

    private fun updateTodayKimpMinMaxRate(kimpPer: Float): Tracker? {
        val startDatetime = LocalDateTime.now().with(LocalTime.MIN).toInstant(ZoneOffset.UTC)
        val endDatetime = LocalDateTime.now().with(LocalTime.MAX).toInstant(ZoneOffset.UTC)

        val target = trackerRepository.findByCreatedAtBetweenAndMaxRateLessThanOrMinRateGreaterThan(startDatetime, endDatetime, kimpPer, kimpPer)
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

    @Transactional
    fun getDailyKimpList(): List<Tracker> {
        return trackerRepository.findAllByCoinNameOrderByCreatedAtDesc("BTC")
    }
}
