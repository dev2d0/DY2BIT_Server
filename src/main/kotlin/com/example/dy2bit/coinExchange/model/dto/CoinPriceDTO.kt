package com.example.dy2bit.coinExchange.model.dto

import com.example.dy2bit.model.dto.BinancePriceDTO
import com.example.dy2bit.model.dto.UpbitPriceDTO

data class CoinPriceDTO(
    val market: String,
    val price: Float,
) {
    constructor(upbitPriceDTO: UpbitPriceDTO) : this(
        market = upbitPriceDTO.market,
        price = upbitPriceDTO.trade_price,
    )

    constructor(binancePriceDTO: BinancePriceDTO) : this(
        market = binancePriceDTO.symbol,
        price = binancePriceDTO.price.toFloat(),
    )
}
