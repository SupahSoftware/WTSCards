package com.wtscards.ui.screens.listings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.wtscards.data.model.Listing
import com.wtscards.domain.usecase.CardUseCase
import com.wtscards.domain.usecase.ListingUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import java.util.UUID

class ListingViewModel(
    private val listingUseCase: ListingUseCase,
    private val cardUseCase: CardUseCase,
    private val coroutineScope: CoroutineScope
) {
    var uiState by mutableStateOf(ListingUiState())
        private set

    init {
        observeListings()
        observeCards()
    }

    private fun observeListings() {
        listingUseCase.getAllListingsFlow()
            .onStart { uiState = uiState.copy(isLoading = true) }
            .onEach { listings ->
                uiState = uiState.copy(
                    isLoading = false,
                    listings = listings,
                    error = null
                )
            }
            .catch { e ->
                uiState = uiState.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load listings"
                )
            }
            .launchIn(coroutineScope)
    }

    private fun observeCards() {
        cardUseCase.getAllCardsFlow()
            .onEach { cards ->
                uiState = uiState.copy(availableCards = cards)
            }
            .catch { /* Silently fail */ }
            .launchIn(coroutineScope)
    }

    fun onSearchQueryChanged(query: String) {
        uiState = uiState.copy(searchQuery = query)
    }

    // Create listing dialog
    fun onShowCreateDialog() {
        uiState = uiState.copy(showCreateDialog = true)
    }

    fun onDismissCreateDialog() {
        uiState = uiState.copy(
            showCreateDialog = false,
            createFormState = CreateListingFormState()
        )
    }

    fun onTitleChanged(title: String) {
        uiState = uiState.copy(
            createFormState = uiState.createFormState.copy(title = title)
        )
    }

    fun onCreateListing() {
        if (!uiState.createFormState.isValid()) return

        uiState = uiState.copy(
            createFormState = uiState.createFormState.copy(isSaving = true)
        )

        coroutineScope.launch {
            try {
                val listing = Listing(
                    id = UUID.randomUUID().toString(),
                    title = uiState.createFormState.title.trim(),
                    createdAt = System.currentTimeMillis(),
                    cards = emptyList()
                )
                listingUseCase.createListing(listing)

                uiState = uiState.copy(
                    showCreateDialog = false,
                    createFormState = CreateListingFormState(),
                    toast = ListingToastState("Listing created", isError = false)
                )
            } catch (e: Exception) {
                uiState = uiState.copy(
                    createFormState = uiState.createFormState.copy(isSaving = false),
                    toast = ListingToastState(e.message ?: "Failed to create listing", isError = true)
                )
            }
        }
    }

    // Add cards dialog
    fun onShowAddCardsDialog(listingId: String) {
        uiState = uiState.copy(
            addCardsDialogState = ListingAddCardsDialogState(listingId = listingId)
        )
    }

    fun onDismissAddCardsDialog() {
        uiState = uiState.copy(addCardsDialogState = null)
    }

    fun onAddCardsSearchChanged(query: String) {
        uiState.addCardsDialogState?.let { dialogState ->
            uiState = uiState.copy(
                addCardsDialogState = dialogState.copy(searchQuery = query)
            )
        }
    }

    fun onToggleCardSelection(cardId: String) {
        uiState.addCardsDialogState?.let { dialogState ->
            val newSelection = if (cardId in dialogState.selectedCardIds) {
                dialogState.selectedCardIds - cardId
            } else {
                dialogState.selectedCardIds + cardId
            }
            uiState = uiState.copy(
                addCardsDialogState = dialogState.copy(selectedCardIds = newSelection)
            )
        }
    }

    fun onConfirmAddCards() {
        uiState.addCardsDialogState?.let { dialogState ->
            if (dialogState.selectedCardIds.isEmpty()) return@let

            uiState = uiState.copy(
                addCardsDialogState = dialogState.copy(isSaving = true)
            )

            coroutineScope.launch {
                try {
                    listingUseCase.addCardsToListing(
                        dialogState.listingId,
                        dialogState.selectedCardIds.toList()
                    )

                    uiState = uiState.copy(
                        addCardsDialogState = null,
                        toast = ListingToastState("Cards added to listing", isError = false)
                    )
                } catch (e: Exception) {
                    uiState = uiState.copy(
                        addCardsDialogState = dialogState.copy(isSaving = false),
                        toast = ListingToastState(e.message ?: "Failed to add cards", isError = true)
                    )
                }
            }
        }
    }

    // Remove card dialog
    fun onShowRemoveCardDialog(listingId: String, cardId: String, cardName: String) {
        uiState = uiState.copy(
            removeCardDialogState = ListingRemoveCardDialogState(
                listingId = listingId,
                cardId = cardId,
                cardName = cardName
            )
        )
    }

    fun onDismissRemoveCardDialog() {
        uiState = uiState.copy(removeCardDialogState = null)
    }

    fun onConfirmRemoveCard() {
        uiState.removeCardDialogState?.let { dialogState ->
            uiState = uiState.copy(
                removeCardDialogState = dialogState.copy(isRemoving = true)
            )

            coroutineScope.launch {
                try {
                    listingUseCase.removeCardFromListing(dialogState.listingId, dialogState.cardId)

                    uiState = uiState.copy(
                        removeCardDialogState = null,
                        toast = ListingToastState("Card removed from listing", isError = false)
                    )
                } catch (e: Exception) {
                    uiState = uiState.copy(
                        removeCardDialogState = dialogState.copy(isRemoving = false),
                        toast = ListingToastState(e.message ?: "Failed to remove card", isError = true)
                    )
                }
            }
        }
    }

    // Delete listing dialog
    fun onShowDeleteListingDialog(listingId: String, title: String) {
        uiState = uiState.copy(
            deleteListingDialogState = DeleteListingDialogState(
                listingId = listingId,
                listingTitle = title
            )
        )
    }

    fun onDismissDeleteListingDialog() {
        uiState = uiState.copy(deleteListingDialogState = null)
    }

    fun onConfirmDeleteListing() {
        uiState.deleteListingDialogState?.let { dialogState ->
            uiState = uiState.copy(
                deleteListingDialogState = dialogState.copy(isDeleting = true)
            )

            coroutineScope.launch {
                try {
                    listingUseCase.deleteListing(dialogState.listingId)

                    uiState = uiState.copy(
                        deleteListingDialogState = null,
                        toast = ListingToastState("Listing deleted", isError = false)
                    )
                } catch (e: Exception) {
                    uiState = uiState.copy(
                        deleteListingDialogState = dialogState.copy(isDeleting = false),
                        toast = ListingToastState(e.message ?: "Failed to delete listing", isError = true)
                    )
                }
            }
        }
    }

    fun showCopyToast(message: String) {
        uiState = uiState.copy(toast = ListingToastState(message, isError = false))
    }

    fun clearToast() {
        uiState = uiState.copy(toast = null)
    }
}
