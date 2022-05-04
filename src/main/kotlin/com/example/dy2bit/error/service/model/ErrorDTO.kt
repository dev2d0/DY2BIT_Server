package com.example.dy2bit.error.service.model

import com.example.dy2bit.model.Error

data class ErrorDTO(
    val id: Long,
    val errorTarget: String?,
    val errorMessage: String?,
    val errorFoundedAt: Long?,
    val createdAt: Long
) {
    constructor(error: Error) : this (
        id = error.id!!,
        errorTarget = error.errorTarget,
        errorMessage = error.errorMessage,
        errorFoundedAt = error.errorFoundedAt?.toEpochMilli(),
        createdAt = error.createdAt.toEpochMilli()
    )
}
