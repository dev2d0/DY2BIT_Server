package com.example.dy2bit.coinExchange.service

import com.example.dy2bit.model.dto.ExchangeRatePriceDTO
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.stereotype.Service

@Service
class ExchagneRateService(
    private val okHttpClient: OkHttpClient,
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
}
