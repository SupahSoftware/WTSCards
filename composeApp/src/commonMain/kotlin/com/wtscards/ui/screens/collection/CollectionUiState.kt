package com.wtscards.ui.screens.collection

import com.wtscards.data.model.Card

enum class SortOption {
    NAME_ASC,
    NAME_DESC,
    PRICE_ASC,
    PRICE_DESC
}

data class CollectionUiState(
    val isLoading: Boolean = false,
    val allCards: List<Card> = emptyList(),
    val displayedCards: List<Card> = emptyList(),
    val searchQuery: String = "",
    val sortOption: SortOption = SortOption.PRICE_DESC,
    val error: String? = null,
    val isEditMode: Boolean = false,
    val selectedCardIds: Set<String> = emptySet(),
    val showDeleteConfirmDialog: Boolean = false
)
