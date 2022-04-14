package com.example.dy2bit.model

import java.time.Instant
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
class ReservationOrder(
    @get:Id
    @get:GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @get:Column(nullable = false)
    var coinName: String,

    @get:Column(nullable = false)
    var unCompletedQuantity: Float,

    @get:Column(nullable = true)
    var completedQuantity: Float? = 0F,

    @get:Column(nullable = false)
    var targetKimpRate: Float,

    @get:Column(nullable = false)
    var curKimp: Float,

    @get:Column(nullable = false)
    var curExchangeRatePrice: Float,

    @get:Column(nullable = false)
    // 업비트 기준으로 참이면 매수 거짓이면 매도
    var position: Boolean,

    @get:Column(nullable = false)
    var createdAt: Instant,

    @get:Column(nullable = true)
    var endAt: Instant? = null
)
