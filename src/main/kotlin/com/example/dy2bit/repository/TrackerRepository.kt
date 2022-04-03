package com.example.dy2bit.repository

import com.example.dy2bit.model.Tracker
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.Instant

interface TrackerRepository : JpaRepository<Tracker, Long> {
    @Query("""
        SELECT t
        FROM Tracker t
        WHERE
            (t.maxRate < :kimpPer
            OR t.minRate > :kimPer)
            AND t.createdAt BETWEEN :startDateTime AND :endDateTime
    """)
    fun findByMaxRateGreaterThanOrMinRateLessThanAndCreatedAtLessThanAndCreatedAtGreaterThan(kimPer: Float, startDateTime: Instant, endDateTime: Instant): Tracker?
}
