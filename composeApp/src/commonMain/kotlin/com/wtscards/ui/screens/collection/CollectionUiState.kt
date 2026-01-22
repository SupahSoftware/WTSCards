package com.wtscards.ui.screens.collection

import com.wtscards.data.model.Card

data class CollectionUiState(
    val isLoading: Boolean = false,
    val cards: List<Card> = emptyList(),
    val error: String? = null
)
