package com.example.dy2bit

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class Dy2bitApplication

fun main(args: Array<String>) {
    runApplication<Dy2bitApplication>(*args)
}
