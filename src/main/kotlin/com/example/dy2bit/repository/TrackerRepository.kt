package com.example.dy2bit.repository

import com.example.dy2bit.model.Tracker
import org.springframework.data.jpa.repository.JpaRepository
import java.time.Instant

interface TrackerRepository : JpaRepository<Tracker, Long> {
//    @Query(
//        """
//        SELECT t
//        FROM Tracker t
//        WHERE
//            (t.maxRate < :kimpPer
//            OR t.minRate > :kimPer)
//            AND t.createdAt BETWEEN :startDateTime AND :endDateTime
//        """
//    )
    fun findByCreatedAtBetweenAndMaxRateLessThanOrMinRateGreaterThan(
        from: Instant,
        to: Instant,
        kimPer: Float,
        kimPer1: Float,
    ): List<Tracker?>
}
