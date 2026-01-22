package com.wtscards.data.model

data class Order(
    val id: String,
    val name: String,
    val streetAddress: String,
    val city: String,
    val state: String,
    val zipcode: String,
    val createdAt: Long,
    val cards: List<Card> = emptyList()
)
