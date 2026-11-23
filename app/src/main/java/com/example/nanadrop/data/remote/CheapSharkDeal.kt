package com.example.nanadrop.data.remote

import com.google.gson.annotations.SerializedName

data class CheapSharkDeal(
    @SerializedName("dealID") val dealID: String,
    @SerializedName("title") val title: String,
    @SerializedName("salePrice") val salePrice: String,
    @SerializedName("normalPrice") val normalPrice: String,
    @SerializedName("savings") val savings: String,
    @SerializedName("thumb") val thumb: String,
    @SerializedName("dealRating") val dealRating: String? = null,
    @SerializedName("storeID") val storeID: String? = null
)
