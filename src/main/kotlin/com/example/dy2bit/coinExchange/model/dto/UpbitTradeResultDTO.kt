package com.example.dy2bit.coinExchange.model.dto

import com.google.gson.annotations.SerializedName

data class UpbitTradeResultDTO(
    @SerializedName(value = "uuid")
    val uuid: String,
    @SerializedName(value = "side")
    val side: String,
    @SerializedName(value = "ord_type")
    val ord_type: String,
    @SerializedName(value = "price")
    val price: String,
    @SerializedName(value = "avg_price")
    val avg_price: String,
    @SerializedName(value = "volume")
    val volume: Float? = 0F,
    @SerializedName(value = "executed_volume")
    val executed_volume: Float? = 0F,
    @SerializedName(value = "remaining_volume")
    val remaining_volume: Float? = 0F,
    @SerializedName(value = "locked")
    val locked: Float? = 0F,
    @SerializedName(value = "state")
    val state: String,
    @SerializedName(value = "remaining_fee")
    val remaining_fee: Float? = 0F,
    @SerializedName(value = "error")
    val error: UpbitError? = null,
) {
    data class UpbitError(
        val name: String,
        val message: String,
    )
}
