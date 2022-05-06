package com.example.dy2bit.tracker.controller

import com.example.dy2bit.tracker.service.TrackerService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Controller

@Controller
class TrackerController(
    private val trackerService: TrackerService,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @Scheduled(fixedDelay = 4000)
    fun trackerEveryJob() {
        logger.info("trackerEveryJob started")
        trackerService.trackerEveryJob()
        logger.info("trackerEveryJob ended")
    }

    // 매일 00시00분에 하루에 한 번 오늘의 김프를 생성합니다.
    @Scheduled(cron = "0 0 0 * * *")
    fun createTodayKimp() {
        logger.info("createTodayKimp")
        trackerService.createTodayKimp()
    }
}
