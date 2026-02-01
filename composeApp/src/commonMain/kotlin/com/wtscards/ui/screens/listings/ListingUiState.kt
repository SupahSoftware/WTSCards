package com.wtscards.ui.screens.listings

import com.wtscards.data.model.Card
import com.wtscards.data.model.Listing

data class ListingUiState(
        val isLoading: Boolean = false,
        val listings: List<Listing> = emptyList(),
        val error: String? = null,
        val searchQuery: String = "",
        val showCreateDialog: Boolean = false,
        val editingListingId: String? = null,
        val createFormState: CreateListingFormState = CreateListingFormState(),
        val addCardsDialogState: ListingAddCardsDialogState? = null,
        val removeCardDialogState: ListingRemoveCardDialogState? = null,
        val deleteListingDialogState: DeleteListingDialogState? = null,
        val availableCards: List<Card> = emptyList(),
        val toast: ListingToastState? = null,
        val imageUrlDialogState: ImageUrlDialogState? = null,
        val preBodyText: String = "",
        val postBodyText: String = "",
        val listingNicePricesDefault: Boolean = false,
        val listingDefaultDiscount: String = "0",
        val createOrderFromListingState: CreateOrderFromListingState? = null,
        val defaultDiscount: Int = 0,
        val defaultEnvelopeCost: String = "1.00",
        val defaultEnvelopeLength: String = "3.5",
        val defaultEnvelopeWidth: String = "6.5",
        val defaultBubbleMailerCost: String = "7.00",
        val defaultBubbleMailerLength: String = "6",
        val defaultBubbleMailerWidth: String = "9",
        val defaultBoxCost: String = "10.00",
        val defaultBoxLength: String = "6",
        val defaultBoxWidth: String = "9",
        val defaultBoxHeight: String = "6"
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

data class ListingToastState(val message: String, val isError: Boolean = false)

data class CreateListingFormState(
        val title: String = "",
        val discount: String = "0",
        val nicePrices: Boolean = false,
        val isSaving: Boolean = false
) {
    fun isValid(): Boolean = title.isNotBlank() && !isSaving
}

data class ListingAddCardsDialogState(
        val listingId: String,
        val searchQuery: String = "",
        val selectedCardIds: Set<String> = emptySet(),
        val isSaving: Boolean = false,
        val shouldFocusSearch: Boolean = false
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

data class ImageUrlDialogState(
        val listingId: String,
        val imageUrl: String = "",
        val isSaving: Boolean = false
)

data class CreateOrderFromListingState(
        val listingId: String,
        val step: CreateOrderFromListingStep = CreateOrderFromListingStep.SELECT_CARDS,
        val searchQuery: String = "",
        val selectedCardIds: Set<String> = emptySet(),
        val cardPrices: Map<String, String> = emptyMap(),
        val orderName: String = "",
        val orderStreetAddress: String = "",
        val orderCity: String = "",
        val orderState: String = "",
        val orderZipcode: String = "",
        val orderShippingType: String = "Bubble mailer",
        val orderShippingPrice: String = "",
        val orderTrackingNumber: String = "",
        val orderDiscount: String = "0",
        val orderLength: String = "",
        val orderWidth: String = "",
        val orderHeight: String = "0",
        val orderPounds: String = "0",
        val orderOunces: String = "0",
        val isSaving: Boolean = false
) {
    fun isOrderValid(): Boolean {
        val stateValid = orderState.isEmpty() || orderState.length == 2
        val zipcodeValid = orderZipcode.isEmpty() || orderZipcode.all { it.isLetterOrDigit() }
        return orderName.isNotBlank() && stateValid && zipcodeValid && !isSaving
    }
}

enum class CreateOrderFromListingStep {
    SELECT_CARDS,
    CONFIRM_PRICES,
    CREATE_ORDER
}
