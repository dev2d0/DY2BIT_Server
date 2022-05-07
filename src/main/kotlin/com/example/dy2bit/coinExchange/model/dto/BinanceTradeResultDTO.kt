package com.example.dy2bit.coinExchange.model.dto

import com.google.gson.annotations.SerializedName

data class BinanceTradeResultDTO(
    @SerializedName(value = "price")
    val price: String,
    @SerializedName(value = "avgPrice")
    val avgPrice: String,
    @SerializedName(value = "origQty")
    val origQty: Float,
    @SerializedName(value = "cumQty")
    val cumQty: Float,
    @SerializedName(value = "executedQty")
    val executedQty: Float,
    @SerializedName(value = "side")
    val side: String,
    @SerializedName(value = "code")
    val code: String?,
    @SerializedName(value = "msg")
    val msg: String?,
)
