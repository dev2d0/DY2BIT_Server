package com.example.dy2bit.coinExchange.model

import com.example.dy2bit.coinExchange.model.type.CoinExchangeType
import com.example.dy2bit.coinExchange.service.BinanceCoinExchangeService
import com.example.dy2bit.coinExchange.service.UpbitCoinExchangeService
import okhttp3.OkHttpClient
import org.springframework.stereotype.Component

@Component
class CoinExchangeServiceFactory(
    private val okHttpClient: OkHttpClient,
) {

    fun coinExchangeServiceFactory(type: CoinExchangeType,): CoinExchangeService {
        return when (type) {
            CoinExchangeType.UPBIT -> {
                UpbitCoinExchangeService(okHttpClient)
            }
            CoinExchangeType.BINANCE -> {
                BinanceCoinExchangeService(okHttpClient)
            }
        }
    }
}
