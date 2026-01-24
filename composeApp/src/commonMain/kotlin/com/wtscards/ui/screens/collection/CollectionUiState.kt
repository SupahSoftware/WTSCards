package com.wtscards.ui.screens.collection

import com.wtscards.data.model.Card

enum class SortOption {
    NAME_ASC,
    NAME_DESC,
    PRICE_ASC,
    PRICE_DESC
}

data class ToastMessage(
    val message: String,
    val isError: Boolean
)

data class EditCardFormState(
    val cardId: String = "",
    val name: String = "",
    val cardNumber: String = "",
    val setName: String = "",
    val parallelName: String = "",
    val gradeOption: String = "Ungraded",
    val priceText: String = "",
    val nameSuggestions: List<String> = emptyList(),
    val setNameSuggestions: List<String> = emptyList(),
    val parallelNameSuggestions: List<String> = emptyList(),
    val isSaving: Boolean = false
)

data class CollectionUiState(
    val isLoading: Boolean = false,
    val allCards: List<Card> = emptyList(),
    val displayedCards: List<Card> = emptyList(),
    val searchQuery: String = "",
    val sortOption: SortOption = SortOption.PRICE_DESC,
    val error: String? = null,
    val isEditMode: Boolean = false,
    val selectedCardIds: Set<String> = emptySet(),
    val showDeleteConfirmDialog: Boolean = false,
    val showEditCardDialog: Boolean = false,
    val editCardForm: EditCardFormState = EditCardFormState(),
    val toastMessage: ToastMessage? = null
)
