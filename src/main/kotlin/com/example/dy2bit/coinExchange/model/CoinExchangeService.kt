package com.example.dy2bit.coinExchange.model

// 언젠가 빗썸, 바이비트 등의 거래소가 추가될 가능성이 있으므로 인터페이스화 시켜둠.
interface CoinExchangeService {
    val coinExchangeApi: String

    fun getCurrentBitPrice(): Any
    fun getAccountAndCheckTradePossible(): Boolean
    fun tradeCoin(position: Boolean, quantity: Float): Boolean
}
