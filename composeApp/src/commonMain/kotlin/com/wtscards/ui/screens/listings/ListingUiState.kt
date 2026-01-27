package com.wtscards.ui.screens.listings

import com.wtscards.data.model.Card
import com.wtscards.data.model.Listing

data class ListingUiState(
    val isLoading: Boolean = false,
    val listings: List<Listing> = emptyList(),
    val error: String? = null,
    val searchQuery: String = "",
    val showCreateDialog: Boolean = false,
    val createFormState: CreateListingFormState = CreateListingFormState(),
    val addCardsDialogState: ListingAddCardsDialogState? = null,
    val removeCardDialogState: ListingRemoveCardDialogState? = null,
    val deleteListingDialogState: DeleteListingDialogState? = null,
    val availableCards: List<Card> = emptyList(),
    val toast: ListingToastState? = null,
    val preBodyText: String = "",
    val postBodyText: String = ""
) {
    val filteredListings: List<Listing>
        get() {
            if (searchQuery.isBlank()) return listings
            val query = searchQuery.lowercase()
            return listings.filter { listing ->
                listing.title.lowercase().contains(query) ||
                listing.cards.any { card -> card.name.lowercase().contains(query) }
            }
        }
}

data class ListingToastState(
    val message: String,
    val isError: Boolean = false
)

data class CreateListingFormState(
    val title: String = "",
    val isSaving: Boolean = false
) {
    fun isValid(): Boolean = title.isNotBlank() && !isSaving
}

data class ListingAddCardsDialogState(
    val listingId: String,
    val searchQuery: String = "",
    val selectedCardIds: Set<String> = emptySet(),
    val isSaving: Boolean = false
)

data class ListingRemoveCardDialogState(
    val listingId: String,
    val cardId: String,
    val cardName: String,
    val isRemoving: Boolean = false
)

data class DeleteListingDialogState(
    val listingId: String,
    val listingTitle: String,
    val isDeleting: Boolean = false
)
