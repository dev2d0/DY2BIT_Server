package com.example.dy2bit.tracker.controller

import com.example.dy2bit.tracker.service.TrackerService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class TrackerController(
        private val trackerService: TrackerService
) {
    @GetMapping("/api/getUpbitCurrentBitPrice")
    suspend fun getCurrentBitPrice() {
        trackerService.updateMinMaxGimpPrice()
    }
}
