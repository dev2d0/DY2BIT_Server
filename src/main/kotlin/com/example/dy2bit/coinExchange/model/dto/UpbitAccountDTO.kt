package com.example.dy2bit.coinExchange.model.dto

import com.google.gson.annotations.SerializedName

data class UpbitAccountDTO(
    @SerializedName(value = "bid_account")
    val buyAccount: BidAccount,
    @SerializedName(value = "ask_account")
    val sellAccount: AskAccount,
) {
    data class BidAccount(
        @SerializedName(value = "balance")
        val balanceKRW: Float,
    )

    data class AskAccount(
        @SerializedName(value = "balance")
        val balanceBTC: Float,
    )
}
