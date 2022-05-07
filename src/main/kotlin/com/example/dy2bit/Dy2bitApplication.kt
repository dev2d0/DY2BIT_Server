package com.example.dy2bit

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling
import java.util.*
import javax.annotation.PostConstruct

@SpringBootApplication
@EnableScheduling
class Dy2bitApplication

@PostConstruct
fun main(args: Array<String>) {
    TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"))
    runApplication<Dy2bitApplication>(*args)
}
