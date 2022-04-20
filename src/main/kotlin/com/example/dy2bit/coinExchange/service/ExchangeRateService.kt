package com.example.dy2bit.coinExchange.service

import com.example.dy2bit.coinExchange.model.dto.KimpDTO
import com.example.dy2bit.model.dto.ExchangeRatePriceDTO
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.stereotype.Service

@Service
class ExchangeRateService(
    private val okHttpClient: OkHttpClient,
    private val upbitCoinExchangeService: UpbitCoinExchangeService,
    private val binanceCoinExchangeService: BinanceCoinExchangeService,
) {
    val exchangeRateApi = "https://quotation-api-cdn.dunamu.com/v1/forex/recent?codes=FRX.KRWUSD"

    fun getExchangeRatePrice(): ExchangeRatePriceDTO {
        val httpResponse = okHttpClient.newCall(
            Request.Builder()
                .url(exchangeRateApi)
                .addHeader("Accept", "application/json")
                .get()
                .build()
        ).execute().body
        val jsonArray: JsonArray = JsonParser().parse(httpResponse?.string()) as JsonArray
        val response = jsonArray.get(0)

        return Gson().fromJson(response, ExchangeRatePriceDTO::class.java)
    }

    suspend fun getKimpPerAndRelatedCoinPrices(): KimpDTO = coroutineScope {
        val getUpbitPrice = async { upbitCoinExchangeService.getCurrentBitPrice() }
        val getBinancePrice = async { binanceCoinExchangeService.getCurrentBitPrice() }
        val getExchangeRatePrice = async { getExchangeRatePrice() }

        val upbitPrice = getUpbitPrice.await().price
        val binancePrice = getBinancePrice.await().price
        val exchangeRatePrice = getExchangeRatePrice.await().basePrice

        // 김프 퍼센트 = (업비트 가격/(바이낸스 가격*환율)-1)*100
        val kimpPer = (upbitPrice / (binancePrice * exchangeRatePrice) - 1) * 100
        return@coroutineScope KimpDTO(
            kimpPer = kimpPer,
            upbitPrice = upbitPrice,
            binancePrice = binancePrice,
            exchangeRatePrice = exchangeRatePrice,
        )
    }
}
