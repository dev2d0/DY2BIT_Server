package com.example.dy2bit.coinExchange.service

import com.example.dy2bit.coinExchange.model.CoinExchangeService
import com.example.dy2bit.coinExchange.model.dto.BinanceAccountDTO
import com.example.dy2bit.coinExchange.model.dto.BinanceTradeResultDTO
import com.example.dy2bit.coinExchange.model.dto.CoinPriceDTO
import com.example.dy2bit.model.dto.BinancePriceDTO
import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.coroutines.coroutineScope
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.apache.commons.codec.binary.Hex
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.math.abs

@Service
class BinanceCoinExchangeService(
    private val okHttpClient: OkHttpClient,
    @Value("\${binance-access.key}") private val binanceAccessKey: String,
    @Value("\${binance-secret.key}") private val binanceSecretKey: String,
) : CoinExchangeService {
    companion object {
        const val DEPOSIT_RATE = 0.9
    }
    // 선물: https://fapi.binance.com/fapi/v1/ticker/price?symbol=BTCUSDT
    // 현물: https://api.binance.com/api/v1/ticker/price?symbol=BTCUSDT
    override val coinExchangeApi = "https://fapi.binance.com"

    override fun getCurrentBitPrice(): CoinPriceDTO {
        val getBitPriceUrl = "/fapi/v1/ticker/price?symbol=BTCUSDT"
        val httpResponse = okHttpClient.newCall(
            Request.Builder()
                .url("$coinExchangeApi$getBitPriceUrl")
                .addHeader("Accept", "application/json")
                .get()
                .build()
        ).execute().body

        return CoinPriceDTO(Gson().fromJson(JsonParser().parse(httpResponse?.string()), BinancePriceDTO::class.java))
    }

    // TODO: 바이낸스 계좌 조회 & 주문 가능 여부
    override suspend fun isBuyTradePossible(curPrice: Float, unCompletedQuantity: Float): Boolean {
        val currentAccountState = getAccount()

        return currentAccountState.availableBalance * currentAccountState.btcAccount!!.leverage * DEPOSIT_RATE > curPrice * unCompletedQuantity
    }

    // TODO: 바이낸스 계좌 조회 & 주문 가능 여부
    override suspend fun isSellTradePossible(unCompletedQuantity: Float): Boolean {
        val currentAccountState = getAccount()
        return abs(currentAccountState.btcAccount!!.positionAmt) >= unCompletedQuantity
    }

    suspend fun getAccount(): BinanceAccountDTO = coroutineScope {
        val timestamp = System.currentTimeMillis().toString()
        val beforeQuery = "timestamp=$timestamp"

        val hmacSha256 = Mac.getInstance("HmacSHA256")
        val secKey = SecretKeySpec(binanceSecretKey.toByteArray(), "HmacSHA256")
        hmacSha256.init(secKey)

        val actualSign = String(Hex.encodeHex(hmacSha256.doFinal(beforeQuery.toByteArray())))
        val queryString = "$beforeQuery&signature=$actualSign"

        println("바이낸스 잔고 조회"+actualSign)
        println("바이낸스 잔고 조회1"+queryString)

        val httpResponse = okHttpClient.newCall(
            Request.Builder()
                .url("$coinExchangeApi/fapi/v2/account?$queryString")
                .get()
                .addHeader("Content-Type", "application/json")
                .addHeader("X-MBX-APIKEY", binanceAccessKey)
                .build()
        ).execute().body
        return@coroutineScope BinanceAccountDTO(Gson().fromJson(JsonParser().parse(httpResponse?.string()), BinanceAccountDTO::class.java))
    }

    // TODO: 바이낸스로 코인 매수, 매도 주문 요청 로직
    suspend fun tradeCoin(position: Boolean, quantity: Float) = coroutineScope {
        val timestamp = System.currentTimeMillis().toString()
        val positionType = if (position) "BUY" else "SELL"
        val beforeQuery = "symbol=BTCUSDT&side=${positionType}&type=MARKET&quantity=${quantity}&timestamp=$timestamp"

        val hmacSha256 = Mac.getInstance("HmacSHA256")
        val secKey = SecretKeySpec(binanceSecretKey.toByteArray(), "HmacSHA256")
        hmacSha256.init(secKey)

        val actualSign = String(Hex.encodeHex(hmacSha256.doFinal(beforeQuery.toByteArray())))
        val queryString = "$beforeQuery&signature=$actualSign"

        println("바이낸스 주문"+actualSign)
        println("바이낸스 주문1"+queryString)

        val formBody: RequestBody = FormBody.Builder()
            .add("symbol", "BTCUSDT")
            .add("side", positionType)
            .add("type", "MARKET")
            .add("quantity", quantity.toString())
            .add("timestamp", timestamp)
            .add("signature", actualSign)
            .build()

        println("바이낸스 매수 중1"+queryString)
        println("바이낸스 매수 중2"+formBody.toString())
        println("바이낸스 매수 중3"+actualSign)

        val httpResponse = okHttpClient.newCall(
            Request.Builder()
                .url("$coinExchangeApi/fapi/v1/order")
                .post(formBody)
                .addHeader("Content-Type", "application/json")
                .addHeader("X-MBX-APIKEY", binanceAccessKey)
                .build()
        ).execute().body

        return@coroutineScope Gson().fromJson(JsonParser().parse(httpResponse?.string()), BinanceTradeResultDTO::class.java)
    }
}
