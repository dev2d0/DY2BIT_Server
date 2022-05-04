package com.example.dy2bit.coinExchange.service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.dy2bit.coinExchange.model.CoinExchangeService
import com.example.dy2bit.coinExchange.model.dto.CoinPriceDTO
import com.example.dy2bit.coinExchange.model.dto.UpbitAccountDTO
import com.example.dy2bit.coinExchange.model.dto.UpbitTradeResultDTO
import com.example.dy2bit.model.dto.UpbitPriceDTO
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import javafx.util.Builder
import kotlinx.coroutines.coroutineScope
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.math.BigInteger
import java.security.MessageDigest
import java.util.*

@Service
class UpbitCoinExchangeService(
    private val okHttpClient: OkHttpClient,
    @Value("\${upbit-access.key}") private val upbitAccessKey: String,
    @Value("\${upbit-secret.key}") private val upbitSecretKey: String,
) : CoinExchangeService {
    companion object {
        const val DEPOSIT_RATE = 0.9
    }
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

    // TODO: 업비트 계좌 조회 & 매수 주문 가능 여부
    override suspend fun isBuyTradePossible(curPrice: Float, unCompletedQuantity: Float): Boolean {
        val currentAccountState = getAccount()

        return currentAccountState.buyAccount.balanceKRW * DEPOSIT_RATE > unCompletedQuantity * curPrice
    }

    // TODO: 업비트 계좌 조회 & 매도 주문 가능 여부
    override suspend fun isSellTradePossible(unCompletedQuantity: Float): Boolean {
        val currentAccountState = getAccount()

        return currentAccountState.sellAccount.balanceBTC >= unCompletedQuantity
    }

    suspend fun getAccount(): UpbitAccountDTO = coroutineScope {
        val queryString = "market=KRW-BTC"
        val authenticationToken = createUpbitToken(queryString)

        val httpResponse = okHttpClient.newCall(
            Request.Builder()
                .url("https://api.upbit.com/v1/orders/chance?$queryString")
                .get()
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", authenticationToken)
                .build()
        ).execute().body

        return@coroutineScope Gson().fromJson(JsonParser().parse(httpResponse?.string()), UpbitAccountDTO::class.java)
    }

    // TODO: 업비트로 코인 매수, 매도 주문 요청 로직
    suspend fun tradeCoin(position: Boolean, quantity: Float, price: Int?) = coroutineScope {
        val marketParams = "market=KRW-BTC"
        val positionSide = if (position) "bid" else "ask"
        val quantityVolume = if (position) null else "$quantity"
        val priceParams = "price=$price"
        val ordType = if (position) "price" else "market"

        val queryString = if (position) {
            "$marketParams&side=$positionSide&$priceParams&ord_type=$ordType"
        } else {
            "$marketParams&side=$positionSide&volume=$quantityVolume&ord_type=$ordType"
        }

        val authenticationToken = createUpbitToken(queryString)

        val formBody: RequestBody = if (position) {
            FormBody.Builder()
                .add("market", "KRW-BTC")
                .add("side", "bid")
                .add("price", "$price")
                .add("ord_type", "price")
                .build()
        } else {
            FormBody.Builder()
                .add("market", "KRW-BTC")
                .add("side", "ask")
                .add("volume", "$quantity")
                .add("ord_type", "market")
                .build()
        }

        val httpResponse = okHttpClient.newCall(
            Request.Builder()
                .url("https://api.upbit.com/v1/orders")
                .post(formBody)
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", authenticationToken)
                .build()
        ).execute().body

        return@coroutineScope Gson().fromJson(JsonParser().parse(httpResponse?.string()), UpbitTradeResultDTO::class.java)
    }

    private fun createUpbitToken(queryString: String): String {
        val md = MessageDigest.getInstance("SHA-512")
        md.update(queryString.toByteArray(charset("utf8")))

        val queryHash = String.format("%0128x", BigInteger(1, md.digest()))

        val algorithm = Algorithm.HMAC256(upbitSecretKey)
        val jwtToken = JWT.create()
            .withClaim("access_key", upbitAccessKey)
            .withClaim("nonce", UUID.randomUUID().toString())
            .withClaim("query_hash", queryHash)
            .withClaim("query_hash_alg", "SHA512")
            .sign(algorithm)
        return "Bearer $jwtToken"
    }
}
