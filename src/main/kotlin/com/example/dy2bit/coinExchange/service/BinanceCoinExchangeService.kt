package com.example.dy2bit.coinExchange.service

import com.example.dy2bit.model.dto.BinancePriceDTO
import com.example.dy2bit.coinExchange.model.CoinExchangeService
import com.example.dy2bit.coinExchange.model.dto.CoinPriceDTO
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.stereotype.Service

@Service
class BinanceCoinExchangeService(
    private val okHttpClient: OkHttpClient,
) : CoinExchangeService {
    // 선물: https://fapi.binance.com/fapi/v1/ticker/price?symbol=BTCUSDT
    // 현물: https://api.binance.com/api/v1/ticker/price?symbol=BTCUSDT
    override val coinExchangeApi = "https://fapi.binance.com/fapi/v1/ticker/price?symbol=BTCUSDT"

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

        return CoinPriceDTO(Gson().fromJson(response, BinancePriceDTO::class.java))
    }

    // TODO: 바이낸스 계좌 조회 & 주문 가능 여부
    override fun getAccountAndCheckTradePossible(): Boolean {
        return false
    }

    // TODO: 바이낸스로 코인 매수, 매도 주문 요청 로직
    override fun tradeCoin(position: Boolean, quantity: Float): Boolean {
        return false
    }
}
