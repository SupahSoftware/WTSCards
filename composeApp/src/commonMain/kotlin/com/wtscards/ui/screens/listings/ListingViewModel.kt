package com.wtscards.ui.screens.listings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.wtscards.data.model.Listing
import com.wtscards.domain.usecase.CardUseCase
import com.wtscards.domain.usecase.ListingUseCase
import com.wtscards.domain.usecase.SettingUseCase
import com.wtscards.ui.screens.settings.SettingsViewModel
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class ListingViewModel(
        private val listingUseCase: ListingUseCase,
        private val cardUseCase: CardUseCase,
        private val settingUseCase: SettingUseCase,
        private val coroutineScope: CoroutineScope
) {
    var uiState by mutableStateOf(ListingUiState())
        private set

    init {
        observeListings()
        observeCards()
        observeSettings()
    }

    private fun observeListings() {
        listingUseCase
                .getAllListingsFlow()
                .onStart { uiState = uiState.copy(isLoading = true) }
                .onEach { listings ->
                    uiState = uiState.copy(isLoading = false, listings = listings, error = null)
                }
                .catch { e ->
                    uiState =
                            uiState.copy(
                                    isLoading = false,
                                    error = e.message ?: "Failed to load listings"
                            )
                }
                .launchIn(coroutineScope)
    }

    private fun observeCards() {
        cardUseCase
                .getAllCardsFlow()
                .onEach { cards -> uiState = uiState.copy(availableCards = cards) }
                .catch {}
                .launchIn(coroutineScope)
    }

    private fun observeSettings() {
        settingUseCase
                .getAllSettingsFlow()
                .onEach { settings ->
                    uiState =
                            uiState.copy(
                                    preBodyText = settings[SettingsViewModel.KEY_PRE_BODY_TEXT]
                                                    ?: "",
                                    postBodyText = settings[SettingsViewModel.KEY_POST_BODY_TEXT]
                                                    ?: "",
                                    listingNicePricesDefault =
                                            settings[
                                                    SettingsViewModel
                                                            .KEY_LISTING_NICE_PRICES_ENABLED] ==
                                                    "true",
                                    listingDefaultDiscount =
                                            settings[SettingsViewModel.KEY_LISTING_DEFAULT_DISCOUNT]
                                                    ?: "0"
                            )
                }
                .catch {}
                .launchIn(coroutineScope)
    }

    fun onSearchQueryChanged(query: String) {
        uiState = uiState.copy(searchQuery = query)
    }

    fun onShowCreateDialog() {
        uiState =
                uiState.copy(
                        showCreateDialog = true,
                        editingListingId = null,
                        createFormState =
                                CreateListingFormState(
                                        discount = uiState.listingDefaultDiscount,
                                        nicePrices = uiState.listingNicePricesDefault
                                )
                )
    }

    fun onDismissCreateDialog() {
        uiState =
                uiState.copy(
                        showCreateDialog = false,
                        editingListingId = null,
                        createFormState = CreateListingFormState()
                )
    }

    fun onEditListing(listing: Listing) {
        uiState =
                uiState.copy(
                        showCreateDialog = true,
                        editingListingId = listing.id,
                        createFormState =
                                CreateListingFormState(
                                        title = listing.title,
                                        discount = listing.discount.toString(),
                                        nicePrices = listing.nicePrices
                                )
                )
    }

    fun onTitleChanged(title: String) {
        uiState = uiState.copy(createFormState = uiState.createFormState.copy(title = title))
    }

    fun onDiscountChanged(value: String) {
        val filtered = value.filter { it.isDigit() }
        uiState = uiState.copy(createFormState = uiState.createFormState.copy(discount = filtered))
    }

    fun onNicePricesChanged(enabled: Boolean) {
        uiState = uiState.copy(createFormState = uiState.createFormState.copy(nicePrices = enabled))
    }

    fun onCreateListing() {
        if (!uiState.createFormState.isValid()) return

        uiState = uiState.copy(createFormState = uiState.createFormState.copy(isSaving = true))

        val editingId = uiState.editingListingId
        val discount = uiState.createFormState.discount.toIntOrNull() ?: 0
        val nicePrices = uiState.createFormState.nicePrices

        coroutineScope.launch {
            try {
                if (editingId != null) {
                    listingUseCase.updateListing(
                            listingId = editingId,
                            title = uiState.createFormState.title.trim(),
                            discount = discount,
                            nicePrices = nicePrices
                    )
                    uiState =
                            uiState.copy(
                                    showCreateDialog = false,
                                    editingListingId = null,
                                    createFormState = CreateListingFormState(),
                                    toast = ListingToastState("Listing updated", isError = false)
                            )
                } else {
                    val listing =
                            Listing(
                                    id = UUID.randomUUID().toString(),
                                    title = uiState.createFormState.title.trim(),
                                    createdAt = System.currentTimeMillis(),
                                    cards = emptyList(),
                                    discount = discount,
                                    nicePrices = nicePrices
                            )
                    listingUseCase.createListing(listing)
                    uiState =
                            uiState.copy(
                                    showCreateDialog = false,
                                    editingListingId = null,
                                    createFormState = CreateListingFormState(),
                                    toast = ListingToastState("Listing created", isError = false)
                            )
                }
            } catch (e: Exception) {
                uiState =
                        uiState.copy(
                                createFormState = uiState.createFormState.copy(isSaving = false),
                                toast =
                                        ListingToastState(
                                                e.message ?: "Failed to save listing",
                                                isError = true
                                        )
                        )
            }
        }
    }

    fun onShowAddCardsDialog(listingId: String) {
        uiState =
                uiState.copy(
                        addCardsDialogState = ListingAddCardsDialogState(listingId = listingId, shouldFocusSearch = true)
                )
    }

    fun onDismissAddCardsDialog() {
        uiState = uiState.copy(addCardsDialogState = null)
    }

    fun onAddCardsSearchChanged(query: String) {
        uiState.addCardsDialogState?.let { dialogState ->
            uiState = uiState.copy(addCardsDialogState = dialogState.copy(searchQuery = query))
        }
    }

    fun onToggleCardSelection(cardId: String) {
        uiState.addCardsDialogState?.let { dialogState ->
            val newSelection =
                    if (cardId in dialogState.selectedCardIds) {
                        dialogState.selectedCardIds - cardId
                    } else {
                        dialogState.selectedCardIds + cardId
                    }
            uiState =
                    uiState.copy(
                            addCardsDialogState =
                                    dialogState.copy(
                                            selectedCardIds = newSelection,
                                            shouldFocusSearch = true
                                    )
                    )
        }
    }

    fun onClearFocusSearchFlag() {
        uiState.addCardsDialogState?.let { dialogState ->
            uiState =
                    uiState.copy(addCardsDialogState = dialogState.copy(shouldFocusSearch = false))
        }
    }

    fun onConfirmAddCards() {
        uiState.addCardsDialogState?.let { dialogState ->
            if (dialogState.selectedCardIds.isEmpty()) return@let

            uiState = uiState.copy(addCardsDialogState = dialogState.copy(isSaving = true))

            coroutineScope.launch {
                try {
                    listingUseCase.addCardsToListing(
                            dialogState.listingId,
                            dialogState.selectedCardIds.toList()
                    )

                    uiState =
                            uiState.copy(
                                    addCardsDialogState = null,
                                    toast =
                                            ListingToastState(
                                                    "Cards added to listing",
                                                    isError = false
                                            )
                            )
                } catch (e: Exception) {
                    uiState =
                            uiState.copy(
                                    addCardsDialogState = dialogState.copy(isSaving = false),
                                    toast =
                                            ListingToastState(
                                                    e.message ?: "Failed to add cards",
                                                    isError = true
                                            )
                            )
                }
            }
        }
    }

    fun onShowRemoveCardDialog(listingId: String, cardId: String, cardName: String) {
        uiState =
                uiState.copy(
                        removeCardDialogState =
                                ListingRemoveCardDialogState(
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
            uiState = uiState.copy(removeCardDialogState = dialogState.copy(isRemoving = true))

            coroutineScope.launch {
                try {
                    listingUseCase.removeCardFromListing(dialogState.listingId, dialogState.cardId)

                    uiState =
                            uiState.copy(
                                    removeCardDialogState = null,
                                    toast =
                                            ListingToastState(
                                                    "Card removed from listing",
                                                    isError = false
                                            )
                            )
                } catch (e: Exception) {
                    uiState =
                            uiState.copy(
                                    removeCardDialogState = dialogState.copy(isRemoving = false),
                                    toast =
                                            ListingToastState(
                                                    e.message ?: "Failed to remove card",
                                                    isError = true
                                            )
                            )
                }
            }
        }
    }

    fun onShowDeleteListingDialog(listingId: String, title: String) {
        uiState =
                uiState.copy(
                        deleteListingDialogState =
                                DeleteListingDialogState(
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
            uiState = uiState.copy(deleteListingDialogState = dialogState.copy(isDeleting = true))

            coroutineScope.launch {
                try {
                    listingUseCase.deleteListing(dialogState.listingId)

                    uiState =
                            uiState.copy(
                                    deleteListingDialogState = null,
                                    toast = ListingToastState("Listing deleted", isError = false)
                            )
                } catch (e: Exception) {
                    uiState =
                            uiState.copy(
                                    deleteListingDialogState = dialogState.copy(isDeleting = false),
                                    toast =
                                            ListingToastState(
                                                    e.message ?: "Failed to delete listing",
                                                    isError = true
                                            )
                            )
                }
            }
        }
    }

    fun onShowImageUrlDialog(listingId: String, currentImageUrl: String?) {
        uiState =
                uiState.copy(
                        imageUrlDialogState =
                                ImageUrlDialogState(
                                        listingId = listingId,
                                        imageUrl = currentImageUrl ?: ""
                                )
                )
    }

    fun onDismissImageUrlDialog() {
        uiState = uiState.copy(imageUrlDialogState = null)
    }

    fun onImageUrlChanged(imageUrl: String) {
        uiState.imageUrlDialogState?.let { dialogState ->
            uiState =
                    uiState.copy(
                            imageUrlDialogState = dialogState.copy(imageUrl = imageUrl)
                    )
        }
    }

    fun onConfirmImageUrl() {
        uiState.imageUrlDialogState?.let { dialogState ->
            uiState =
                    uiState.copy(
                            imageUrlDialogState = dialogState.copy(isSaving = true)
                    )

            coroutineScope.launch {
                try {
                    val imageUrl = dialogState.imageUrl.trim().ifBlank { null }
                    listingUseCase.updateImageUrl(dialogState.listingId, imageUrl)
                    uiState =
                            uiState.copy(
                                    imageUrlDialogState = null,
                                    toast =
                                            ListingToastState(
                                                    "Image URL updated",
                                                    isError = false
                                            )
                            )
                } catch (e: Exception) {
                    uiState =
                            uiState.copy(
                                    imageUrlDialogState = dialogState.copy(isSaving = false),
                                    toast =
                                            ListingToastState(
                                                    e.message ?: "Failed to update image URL",
                                                    isError = true
                                            )
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
