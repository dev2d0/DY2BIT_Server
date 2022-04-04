package com.example.dy2bit.tracker.controller

import com.example.dy2bit.tracker.service.TrackerService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.web.bind.annotation.RestController

@RestController
class TrackerController(
    private val trackerService: TrackerService,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    // TODO: 나중에 스케줄러 풀어주기
    // @Scheduled(fixedDelay=3000)
    suspend fun trackerEveryJob() {
        logger.info("trackerEveryJob started")
        trackerService.trackerEveryJob()
        logger.info("trackerEveryJob ended")
    }

}
