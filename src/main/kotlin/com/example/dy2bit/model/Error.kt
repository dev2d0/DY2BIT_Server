package com.example.dy2bit.model

import java.time.Instant
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
class Error(
    @get:Id
    @get:GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @get:Column(nullable = true)
    var errorTarget: String?,

    @get:Column(nullable = true)
    var errorMessage: String?,

    @get:Column(nullable = true)
    var errorFoundedAt: Instant? = null,

    @get:Column(nullable = false)
    var createdAt: Instant
)
