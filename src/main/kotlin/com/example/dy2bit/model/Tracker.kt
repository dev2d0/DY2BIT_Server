package com.example.dy2bit.model

import java.time.Instant
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
class Tracker(
    @get:Id
    @get:GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long,

    @get:Column(nullable = false)
    var coinName: String,

    @get:Column(nullable = false)
    var minRate: Float,

    @get:Column(nullable = true)
    var maxRate: Float,

    @get:Column(nullable = false)
    var minRateAt: Instant,

    @get:Column(nullable = true)
    var maxRateAt: Instant,

    @get:Column(nullable = true)
    var createdAt: Instant
)
