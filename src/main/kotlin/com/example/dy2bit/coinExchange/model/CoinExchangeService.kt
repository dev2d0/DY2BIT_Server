package com.example.dy2bit.coinExchange.model

import com.example.dy2bit.coinExchange.model.dto.CoinPriceDTO

// 언젠가 빗썸, 바이비트 등의 거래소가 추가될 가능성이 있으므로 인터페이스화 시켜둠.
interface CoinExchangeService {
    val coinExchangeApi: String

    fun getCurrentBitPrice(): CoinPriceDTO
    suspend fun isBuyTradePossible(curPrice: Float, unCompletedQuantity: Float): Boolean
    suspend fun isSellTradePossible(unCompletedQuantity: Float): Boolean
}
