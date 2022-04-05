package com.example.dy2bit.coinExchange.service

import com.example.dy2bit.model.dto.UpbitPriceDTO
import com.example.dy2bit.coinExchange.model.CoinExchangeService
import com.example.dy2bit.coinExchange.model.dto.CoinPriceDTO
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.stereotype.Service

@Service
class UpbitCoinExchangeService(
    private val okHttpClient: OkHttpClient,
) : CoinExchangeService {
    override val coinExchangeApi = "https://api.upbit.com/v1/candles/minutes/1?market=KRW-BTC&count=1"

    override fun getCurrentBitPrice(): CoinPriceDTO {
        val httpResponse = okHttpClient.newCall(
            Request.Builder()
                .url(coinExchangeApi)
                .addHeader("Accept", "application/json")
                .get()
                .build()
        ).execute().body
        val jsonArray: JsonArray = JsonParser().parse(httpResponse?.string()) as JsonArray
        val response = jsonArray.get(0)

        return CoinPriceDTO(Gson().fromJson(response, UpbitPriceDTO::class.java))
    }

    // TODO: 업비트 계좌 조회 & 주문 가능 여부
    override fun getAccountAndCheckTradePossible(): Boolean {
        return false
    }

    // TODO: 업비트로 코인 매수, 매도 주문 요청 로직
    override fun tradeCoin(position: Boolean, quantity: Float): Boolean {
        return false
    }
}
