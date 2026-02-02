package com.wtscards.data.model

data class Listing(
    val id: String,
    val title: String,
    val createdAt: Long,
    val cards: List<Card> = emptyList(),
    val discount: Int = 0,
    val nicePrices: Boolean = false,
    val imageUrl: String? = null,
    val lotPriceOverride: Long? = null
)
