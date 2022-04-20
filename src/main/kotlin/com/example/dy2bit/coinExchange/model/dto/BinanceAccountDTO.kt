package com.example.dy2bit.coinExchange.model.dto

import com.google.gson.annotations.SerializedName

data class BinanceAccountDTO(
    @SerializedName(value = "positions")
    val accounts: List<Account>,
    val btcAccount: Account?,
    val availableBalance: Float,
) {
    data class Account(
        val symbol: String,
        val positionAmt: Float,
        val leverage: Int,
    )

    constructor(binanceAccount: BinanceAccountDTO) : this(
        accounts = binanceAccount.accounts.filter { it.symbol == "BTCUSDT" },
        btcAccount = binanceAccount.accounts.filter { it.symbol == "BTCUSDT" }.last(),
        availableBalance = binanceAccount.availableBalance,
    )
}
