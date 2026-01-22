package com.wtscards.data.model

data class Card(
    val sportsCardProId: String,
    val name: String,
    val setName: String,
    val priceInPennies: Long,
    val gradedString: String,
    val quantity: Int
)
