package com.example.dy2bit.utils

import com.example.dy2bit.utils.exception.Dy2bitException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class UtilService(
    @Value("\${dy2bit-secret.key}") private val dy2bitSecretKey: String,
) {
    fun checkDy2bitSecretKey(secretKey: String): Boolean {
        if (secretKey != dy2bitSecretKey) {
            throw Dy2bitException("시크릿 키가 일치하지 않습니다.")
        } else {
            return true
        }
    }
}
