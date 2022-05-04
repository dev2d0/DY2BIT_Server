package com.example.dy2bit.error.service

import com.example.dy2bit.error.service.model.ErrorDTO
import com.example.dy2bit.model.Error
import com.example.dy2bit.repository.ErrorRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class ErrorService(
    private val errorRepository: ErrorRepository,
) {
    @Transactional
    fun getError(): Error {
        println(errorRepository.findAllByCreatedAtNotNull().last().toString())
        return errorRepository.findAllByCreatedAtNotNull().last()
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    fun reportError(errorTarget: String, errorMessage: String): ErrorDTO {
        val reportedError = getError()
        reportedError.errorTarget = errorTarget
        reportedError.errorMessage = errorMessage
        reportedError.errorFoundedAt = Instant.now()
        return ErrorDTO(errorRepository.save(reportedError))
    }
}
