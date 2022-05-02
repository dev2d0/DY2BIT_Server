package com.example.dy2bit.repository

import com.example.dy2bit.model.Tracker
import org.springframework.data.jpa.repository.JpaRepository
import java.time.Instant

interface TrackerRepository : JpaRepository<Tracker, Long> {
    fun findByCreatedAtBetweenAndMaxRateLessThanOrMinRateGreaterThan(
        from: Instant,
        to: Instant,
        kimPer: Float,
        kimPer1: Float,
    ): List<Tracker?>

    fun findAllByCoinNameOrderByCreatedAtDesc(coinName: String): List<Tracker>
}
