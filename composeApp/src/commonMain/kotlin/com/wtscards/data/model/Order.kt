package com.wtscards.data.model

data class Order(
    val id: String,
    val name: String,
    val streetAddress: String,
    val city: String,
    val state: String,
    val zipcode: String,
    val shippingType: String? = null,
    val shippingCost: Long = 0,
    val createdAt: Long,
    val cards: List<Card> = emptyList()
)
