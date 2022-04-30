package com.example.dy2bit.member.model.dto

import org.springframework.http.HttpStatus

data class UserLoginDTO(
    val status: HttpStatus,
    val token: String,
)
