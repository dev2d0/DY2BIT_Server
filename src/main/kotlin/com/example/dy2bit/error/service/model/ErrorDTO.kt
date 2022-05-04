package com.example.dy2bit.error.service.model

import com.example.dy2bit.model.Error
import java.time.Instant

data class ErrorDTO(
    val id: Long,
    val errorTarget: String?,
    val errorMessage: String?,
    val errorFoundedAt: Instant?,
    val createdAt: Instant
) {
    constructor(error: Error): this (
        id = error.id!!,
        errorTarget = error.errorTarget,
        errorMessage = error.errorMessage,
        errorFoundedAt = error.errorFoundedAt,
        createdAt = error.createdAt
    )
}
