package com.example.dy2bit.reservationOrder.model.dto

import com.example.dy2bit.coinExchange.model.dto.BinanceAccountDTO
import com.example.dy2bit.coinExchange.model.dto.UpbitAccountDTO

data class UserAccountDTO(
    val upbitBuyAccountKRW: Float,
    val upbitSellAccountBTC: Float,
    val binanceBuyAccountUSDT: Float,
    val binanceSellAccountBTC: Float?,
    val binanceLeverage: Int?,
) {
    constructor(upbitAccount: UpbitAccountDTO, binanceAccount: BinanceAccountDTO) : this(
        upbitBuyAccountKRW = upbitAccount.buyAccount.balanceKRW,
        upbitSellAccountBTC = upbitAccount.sellAccount.balanceBTC,
        binanceBuyAccountUSDT = binanceAccount.availableBalance,
        binanceSellAccountBTC = binanceAccount.btcAccount?.positionAmt,
        binanceLeverage = binanceAccount.btcAccount?.leverage,
    )
}
