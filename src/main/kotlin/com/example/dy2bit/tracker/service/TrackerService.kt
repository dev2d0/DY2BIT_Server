package com.example.dy2bit.tracker.service

import com.example.dy2bit.model.Tracker
import com.example.dy2bit.model.dto.BinancePriceDTO
import com.example.dy2bit.model.dto.ExchangeRatePriceDTO
import com.example.dy2bit.model.dto.UpbitPriceDTO
import com.example.dy2bit.repository.TrackerRepository
import com.example.dy2bit.reservationOrder.service.ReservationOrderService
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.stereotype.Service
import java.time.*

@Service
class TrackerService(
    private val reservationOrderService: ReservationOrderService,
    private val trackerRepository: TrackerRepository,
    private val okHttpClient: OkHttpClient,
) {
    suspend fun trackerEveryJob() = runBlocking {
        val kimpPer = async { getKimpPer() }.await()
        async {
            reservationOrderService.tradeReservationOrder(kimpPer)
            updateTodayKimpMinMaxRate(kimpPer)
        }
    }.await()

    private fun updateTodayKimpMinMaxRate(kimpPer: Float): Tracker? {
        val startDatetime = LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 0, 0)).atZone(ZoneId.of("Asia/Seoul")).toInstant() //오늘 00:00:00
        val endDatetime = LocalDateTime.of(LocalDate.now(), LocalTime.of(23, 59, 59)).atZone(ZoneId.of("Asia/Seoul")).toInstant() //오늘 23:59:59

        val target = trackerRepository.findByMaxRateGreaterThanOrMinRateLessThanAndCreatedAtLessThanAndCreatedAtGreaterThan(kimpPer, startDatetime, endDatetime)
        return if (target != null) {
            if (target.maxRate < kimpPer) {
                target.maxRate = kimpPer
                target.maxRateAt = Instant.now()
            } else if (target.minRate > kimpPer) {
                target.minRate = kimpPer
                target.minRateAt = Instant.now()
            }
            trackerRepository.save(target)
        } else null
    }

    private fun getKimpPer(): Float = runBlocking {
        val getUpbitPrice = async { getUpbitCurrentBitPrice() }
        val getBinancePrice = async { getBinanceCurrentBitPrice() }
        val getExchangeRatePrice = async { getExchangeRatePrice() }

        val upbitPrice = getUpbitPrice.await().opening_price.toInt()
        val binancePrice = getBinancePrice.await().price.toFloat()
        val exchangeRatePrice = getExchangeRatePrice.await().basePrice

        // 김프 퍼센트 = (업비트 가격/(바이낸스 가격*환율)-1)*100
        val kimpPer = (upbitPrice / (binancePrice * exchangeRatePrice) - 1) * 100
        return@runBlocking kimpPer
    }

    // TODO: 코인 가격 가져오는 함수들의 url은 전역으로 빼고 symbol 값 넣을 수 있도록 하기
    private fun getUpbitCurrentBitPrice(): UpbitPriceDTO {
        val httpResponse = okHttpClient.newCall(
            Request.Builder()
                .url("https://api.upbit.com/v1/candles/minutes/1?market=KRW-BTC&count=1")
                .addHeader("Accept", "application/json")
                .get()
                .build()
        ).execute().body
        val jsonArray: JsonArray = JsonParser().parse(httpResponse?.string()) as JsonArray
        val response = jsonArray.get(0)

        return Gson().fromJson(response, UpbitPriceDTO::class.java)
    }

    private fun getBinanceCurrentBitPrice(): BinancePriceDTO {
        val httpResponse = okHttpClient.newCall(
            Request.Builder()
                // 선물: https://fapi.binance.com/fapi/v1/ticker/price?symbol=BTCUSDT
                // 현물: https://api.binance.com/api/v1/ticker/price?symbol=BTCUSDT
                .url("https://fapi.binance.com/fapi/v1/ticker/price?symbol=BTCUSDT")
                .addHeader("Accept", "application/json")
                .get()
                .build()
        ).execute().body

        return Gson().fromJson(httpResponse?.string(), BinancePriceDTO::class.java)
    }

    private fun getExchangeRatePrice(): ExchangeRatePriceDTO {
        val httpResponse = okHttpClient.newCall(
            Request.Builder()
                .url("https://quotation-api-cdn.dunamu.com/v1/forex/recent?codes=FRX.KRWUSD")
                .addHeader("Accept", "application/json")
                .get()
                .build()
        ).execute().body
        val jsonArray: JsonArray = JsonParser().parse(httpResponse?.string()) as JsonArray
        val response = jsonArray.get(0)

        return Gson().fromJson(response, ExchangeRatePriceDTO::class.java)
    }
}
