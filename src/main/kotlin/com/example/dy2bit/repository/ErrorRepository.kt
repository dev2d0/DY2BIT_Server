package com.example.dy2bit.repository

import com.example.dy2bit.model.Error
import org.springframework.data.jpa.repository.JpaRepository

interface ErrorRepository : JpaRepository<Error, Long> {
    fun findAllByCreatedAtNotNull(): List<Error>
}
