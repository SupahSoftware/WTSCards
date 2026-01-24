package com.wtscards.data.model

data class Listing(
    val id: String,
    val title: String,
    val createdAt: Long,
    val cards: List<Card> = emptyList()
)
