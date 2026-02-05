package com.wtscards.ui.screens.listings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.wtscards.data.model.Listing
import com.wtscards.data.model.Order
import com.wtscards.data.model.OrderStatus
import com.wtscards.domain.usecase.CardUseCase
import com.wtscards.domain.usecase.ListingUseCase
import com.wtscards.domain.usecase.OrderUseCase
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
        private val orderUseCase: OrderUseCase,
        private val coroutineScope: CoroutineScope
) {
    var uiState by mutableStateOf(ListingUiState())
        private set

    init {
        observeListings()
        observeCards()
        observeSettings()
        observeOrders()
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

    private fun observeOrders() {
        orderUseCase
                .getAllOrdersFlow()
                .onEach { orders ->
                    val cardIdsInOrders = orders.flatMap { it.cards.map { card -> card.id } }.toSet()
                    uiState = uiState.copy(cardIdsInOrders = cardIdsInOrders)
                }
                .catch {}
                .launchIn(coroutineScope)
    }

    private fun observeSettings() {
        settingUseCase
                .getAllSettingsFlow()
                .onEach { settings ->
                    val defaultDiscount = (settings[SettingsViewModel.KEY_DEFAULT_DISCOUNT] ?: "0").toIntOrNull() ?: 0
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
                                                    ?: "0",
                                    defaultDiscount = defaultDiscount,
                                    defaultEnvelopeCost = settings[SettingsViewModel.KEY_SHIPPING_ENVELOPE_COST] ?: "1.00",
                                    defaultEnvelopeLength = settings[SettingsViewModel.KEY_SHIPPING_ENVELOPE_LENGTH] ?: "3.5",
                                    defaultEnvelopeWidth = settings[SettingsViewModel.KEY_SHIPPING_ENVELOPE_WIDTH] ?: "6.5",
                                    defaultBubbleMailerCost = settings[SettingsViewModel.KEY_SHIPPING_BUBBLE_MAILER_COST] ?: "7.00",
                                    defaultBubbleMailerLength = settings[SettingsViewModel.KEY_SHIPPING_BUBBLE_MAILER_LENGTH] ?: "6",
                                    defaultBubbleMailerWidth = settings[SettingsViewModel.KEY_SHIPPING_BUBBLE_MAILER_WIDTH] ?: "9",
                                    defaultBoxCost = settings[SettingsViewModel.KEY_SHIPPING_BOX_COST] ?: "10.00",
                                    defaultBoxLength = settings[SettingsViewModel.KEY_SHIPPING_BOX_LENGTH] ?: "6",
                                    defaultBoxWidth = settings[SettingsViewModel.KEY_SHIPPING_BOX_WIDTH] ?: "9",
                                    defaultBoxHeight = settings[SettingsViewModel.KEY_SHIPPING_BOX_HEIGHT] ?: "6"
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

    // Lot price override

    fun onShowLotPriceOverrideDialog(listingId: String, currentOverride: Long?) {
        val displayValue = if (currentOverride != null) {
            String.format("%.2f", currentOverride / 100.0)
        } else {
            ""
        }
        uiState = uiState.copy(
                lotPriceOverrideDialogState = LotPriceOverrideDialogState(
                        listingId = listingId,
                        lotPriceOverride = displayValue
                )
        )
    }

    fun onDismissLotPriceOverrideDialog() {
        uiState = uiState.copy(lotPriceOverrideDialogState = null)
    }

    fun onLotPriceOverrideChanged(value: String) {
        uiState.lotPriceOverrideDialogState?.let { dialogState ->
            val filtered = value.filter { it.isDigit() || it == '.' }
            if (filtered.count { it == '.' } <= 1) {
                uiState = uiState.copy(
                        lotPriceOverrideDialogState = dialogState.copy(lotPriceOverride = filtered)
                )
            }
        }
    }

    fun onConfirmLotPriceOverride() {
        uiState.lotPriceOverrideDialogState?.let { dialogState ->
            uiState = uiState.copy(
                    lotPriceOverrideDialogState = dialogState.copy(isSaving = true)
            )

            coroutineScope.launch {
                try {
                    val overrideValue = dialogState.lotPriceOverride.trim()
                    val pennies = if (overrideValue.isBlank()) {
                        null
                    } else {
                        ((overrideValue.toDoubleOrNull() ?: 0.0) * 100).toLong().takeIf { it > 0 }
                    }
                    listingUseCase.updateLotPriceOverride(dialogState.listingId, pennies)
                    uiState = uiState.copy(
                            lotPriceOverrideDialogState = null,
                            toast = ListingToastState(
                                    if (pennies != null) "Lot price override set" else "Lot price override cleared",
                                    isError = false
                            )
                    )
                } catch (e: Exception) {
                    uiState = uiState.copy(
                            lotPriceOverrideDialogState = dialogState.copy(isSaving = false),
                            toast = ListingToastState(
                                    e.message ?: "Failed to update lot price override",
                                    isError = true
                            )
                    )
                }
            }
        }
    }

    // Create order from listing flow

    fun onShowCreateOrderFromListing(listingId: String) {
        uiState = uiState.copy(
                createOrderFromListingState = CreateOrderFromListingState(
                        listingId = listingId,
                        orderShippingPrice = uiState.defaultBubbleMailerCost,
                        orderDiscount = uiState.defaultDiscount.toString(),
                        orderLength = uiState.defaultBubbleMailerLength,
                        orderWidth = uiState.defaultBubbleMailerWidth
                )
        )
    }

    fun onDismissCreateOrderFromListing() {
        uiState = uiState.copy(createOrderFromListingState = null)
    }

    fun onCreateOrderSearchChanged(query: String) {
        uiState.createOrderFromListingState?.let { state ->
            uiState = uiState.copy(createOrderFromListingState = state.copy(searchQuery = query))
        }
    }

    fun onToggleCreateOrderCardSelection(cardId: String) {
        uiState.createOrderFromListingState?.let { state ->
            val newSelection = if (cardId in state.selectedCardIds) {
                state.selectedCardIds - cardId
            } else {
                state.selectedCardIds + cardId
            }
            uiState = uiState.copy(createOrderFromListingState = state.copy(selectedCardIds = newSelection))
        }
    }

    fun onProceedToCreateOrderPriceConfirmation() {
        uiState.createOrderFromListingState?.let { state ->
            val listing = uiState.listings.find { it.id == state.listingId } ?: return@let
            val cardPrices = mutableMapOf<String, String>()
            listing.cards.filter { it.id in state.selectedCardIds }.forEach { card ->
                val adjustedPrice = calculateListingPrice(card.priceInPennies, listing.discount, listing.nicePrices)
                cardPrices[card.id] = if (adjustedPrice > 0) String.format("%.2f", adjustedPrice / 100.0) else ""
            }
            uiState = uiState.copy(
                    createOrderFromListingState = state.copy(
                            step = CreateOrderFromListingStep.CONFIRM_PRICES,
                            cardPrices = cardPrices
                    )
            )
        }
    }

    fun onCreateOrderCardPriceChanged(cardId: String, price: String) {
        uiState.createOrderFromListingState?.let { state ->
            uiState = uiState.copy(
                    createOrderFromListingState = state.copy(
                            cardPrices = state.cardPrices + (cardId to price)
                    )
            )
        }
    }

    fun onProceedToCreateOrderForm() {
        uiState.createOrderFromListingState?.let { state ->
            uiState = uiState.copy(
                    createOrderFromListingState = state.copy(
                            step = CreateOrderFromListingStep.CREATE_ORDER
                    )
            )
        }
    }

    fun onCreateOrderNameChanged(name: String) {
        uiState.createOrderFromListingState?.let { state ->
            uiState = uiState.copy(createOrderFromListingState = state.copy(orderName = name))
        }
    }

    fun onCreateOrderStreetAddressChanged(address: String) {
        uiState.createOrderFromListingState?.let { state ->
            uiState = uiState.copy(createOrderFromListingState = state.copy(orderStreetAddress = address))
        }
    }

    fun onCreateOrderCityChanged(city: String) {
        uiState.createOrderFromListingState?.let { state ->
            uiState = uiState.copy(createOrderFromListingState = state.copy(orderCity = city))
        }
    }

    fun onCreateOrderStateChanged(stateValue: String) {
        uiState.createOrderFromListingState?.let { state ->
            uiState = uiState.copy(createOrderFromListingState = state.copy(orderState = stateValue))
        }
    }

    fun onCreateOrderZipcodeChanged(zipcode: String) {
        uiState.createOrderFromListingState?.let { state ->
            uiState = uiState.copy(createOrderFromListingState = state.copy(orderZipcode = zipcode))
        }
    }

    fun onCreateOrderShippingTypeChanged(type: String) {
        uiState.createOrderFromListingState?.let { state ->
            val defaultPrice = getDefaultShippingPrice(type)
            val (defaultLength, defaultWidth, defaultHeight) = getDefaultDimensions(type)
            uiState = uiState.copy(
                    createOrderFromListingState = state.copy(
                            orderShippingType = type,
                            orderShippingPrice = defaultPrice,
                            orderLength = defaultLength,
                            orderWidth = defaultWidth,
                            orderHeight = defaultHeight
                    )
            )
        }
    }

    fun onCreateOrderShippingPriceChanged(price: String) {
        uiState.createOrderFromListingState?.let { state ->
            uiState = uiState.copy(createOrderFromListingState = state.copy(orderShippingPrice = price))
        }
    }

    fun onCreateOrderTrackingNumberChanged(trackingNumber: String) {
        uiState.createOrderFromListingState?.let { state ->
            uiState = uiState.copy(createOrderFromListingState = state.copy(orderTrackingNumber = trackingNumber))
        }
    }

    fun onCreateOrderDiscountChanged(discount: String) {
        uiState.createOrderFromListingState?.let { state ->
            val filtered = discount.filter { it.isDigit() }
            uiState = uiState.copy(createOrderFromListingState = state.copy(orderDiscount = filtered))
        }
    }

    fun onCreateOrderLengthChanged(length: String) {
        uiState.createOrderFromListingState?.let { state ->
            uiState = uiState.copy(createOrderFromListingState = state.copy(orderLength = length))
        }
    }

    fun onCreateOrderWidthChanged(width: String) {
        uiState.createOrderFromListingState?.let { state ->
            uiState = uiState.copy(createOrderFromListingState = state.copy(orderWidth = width))
        }
    }

    fun onCreateOrderHeightChanged(height: String) {
        uiState.createOrderFromListingState?.let { state ->
            uiState = uiState.copy(createOrderFromListingState = state.copy(orderHeight = height))
        }
    }

    fun onCreateOrderPoundsChanged(pounds: String) {
        uiState.createOrderFromListingState?.let { state ->
            uiState = uiState.copy(createOrderFromListingState = state.copy(orderPounds = pounds))
        }
    }

    fun onCreateOrderOuncesChanged(ounces: String) {
        uiState.createOrderFromListingState?.let { state ->
            uiState = uiState.copy(createOrderFromListingState = state.copy(orderOunces = ounces))
        }
    }

    fun onConfirmCreateOrderFromListing() {
        uiState.createOrderFromListingState?.let { state ->
            if (!state.isOrderValid()) return@let

            uiState = uiState.copy(createOrderFromListingState = state.copy(isSaving = true))

            coroutineScope.launch {
                try {
                    val shippingCostInPennies = ((state.orderShippingPrice.toDoubleOrNull() ?: 0.0) * 100).toLong()
                    val trackingNumber = state.orderTrackingNumber.trim().takeIf { it.isNotBlank() }
                    val discount = state.orderDiscount.toIntOrNull() ?: 0
                    val length = state.orderLength.toDoubleOrNull() ?: 0.0
                    val width = state.orderWidth.toDoubleOrNull() ?: 0.0
                    val height = state.orderHeight.toDoubleOrNull() ?: 0.0
                    val pounds = state.orderPounds.toIntOrNull() ?: 0
                    val ounces = state.orderOunces.toIntOrNull() ?: 0

                    val orderId = UUID.randomUUID().toString()
                    val order = Order(
                            id = orderId,
                            name = state.orderName.toTitleCase(),
                            streetAddress = state.orderStreetAddress.toTitleCase(),
                            city = state.orderCity.toTitleCase(),
                            state = state.orderState.uppercase(),
                            zipcode = state.orderZipcode,
                            shippingType = state.orderShippingType,
                            shippingCost = shippingCostInPennies,
                            status = OrderStatus.NEW,
                            createdAt = System.currentTimeMillis(),
                            trackingNumber = trackingNumber,
                            discount = discount,
                            cards = emptyList(),
                            length = length,
                            width = width,
                            height = height,
                            pounds = pounds,
                            ounces = ounces
                    )
                    orderUseCase.createOrder(order)

                    val cardPrices = state.cardPrices.mapValues { (_, priceStr) ->
                        ((priceStr.toDoubleOrNull() ?: 0.0) * 100).toLong()
                    }
                    orderUseCase.addCardsToOrder(orderId, cardPrices)

                    uiState = uiState.copy(
                            createOrderFromListingState = null,
                            toast = ListingToastState("Order created with ${state.selectedCardIds.size} card${if (state.selectedCardIds.size != 1) "s" else ""}", isError = false)
                    )
                } catch (e: Exception) {
                    uiState = uiState.copy(
                            createOrderFromListingState = state.copy(isSaving = false),
                            toast = ListingToastState(e.message ?: "Failed to create order", isError = true)
                    )
                }
            }
        }
    }

    private fun getDefaultShippingPrice(shippingType: String): String {
        return when (shippingType) {
            "Envelope" -> uiState.defaultEnvelopeCost
            "Bubble mailer" -> uiState.defaultBubbleMailerCost
            "Box" -> uiState.defaultBoxCost
            "Other" -> "0.00"
            else -> "0.00"
        }
    }

    private fun getDefaultDimensions(shippingType: String): Triple<String, String, String> {
        return when (shippingType) {
            "Envelope" -> Triple(uiState.defaultEnvelopeLength, uiState.defaultEnvelopeWidth, "0")
            "Bubble mailer" -> Triple(uiState.defaultBubbleMailerLength, uiState.defaultBubbleMailerWidth, "0")
            "Box" -> Triple(uiState.defaultBoxLength, uiState.defaultBoxWidth, uiState.defaultBoxHeight)
            else -> Triple("0", "0", "0")
        }
    }

    private fun calculateListingPrice(priceInPennies: Long, discountPercent: Int, nicePrices: Boolean): Long {
        if (priceInPennies <= 0) return priceInPennies
        val discounted = (priceInPennies * (100 - discountPercent) / 100.0)
        val rounded = kotlin.math.ceil(discounted).toLong()
        return if (nicePrices) {
            val remainder = rounded % 100
            if (remainder == 0L) rounded else rounded + (100 - remainder)
        } else {
            rounded
        }
    }

    private fun String.toTitleCase(): String {
        return split(" ").joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { it.uppercase() }
        }
    }

    fun showCopyToast(message: String) {
        uiState = uiState.copy(toast = ListingToastState(message, isError = false))
    }

    fun clearToast() {
        uiState = uiState.copy(toast = null)
    }
}
