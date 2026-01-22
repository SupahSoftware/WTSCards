package com.wtscards.data.model

data class Card(
    val id: String,
    val sportsCardProId: String?,
    val name: String,
    val setName: String,
    val priceInPennies: Long,
    val gradedString: String,
    val priceSold: Long? = null
)
