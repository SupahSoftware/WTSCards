package com.wtscards.ui.screens.orders

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.wtscards.data.model.Order
import com.wtscards.data.model.OrderStatus
import com.wtscards.domain.usecase.CardUseCase
import com.wtscards.domain.usecase.OrderUseCase
import com.wtscards.domain.usecase.SettingUseCase
import com.wtscards.ui.screens.settings.SettingsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import java.util.UUID

class OrderViewModel(
    private val orderUseCase: OrderUseCase,
    private val cardUseCase: CardUseCase,
    private val settingUseCase: SettingUseCase,
    private val coroutineScope: CoroutineScope
) {
    var uiState by mutableStateOf(OrderUiState())
        private set

    init {
        observeOrders()
        observeCards()
        observeSettings()
    }

    private fun observeOrders() {
        orderUseCase.getAllOrdersFlow()
            .onStart { uiState = uiState.copy(isLoading = true) }
            .onEach { orders ->
                uiState = uiState.copy(
                    isLoading = false,
                    orders = orders,
                    error = null
                )
            }
            .catch { e ->
                uiState = uiState.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load orders"
                )
            }
            .launchIn(coroutineScope)
    }

    private fun observeCards() {
        cardUseCase.getAllCardsFlow()
            .onEach { cards ->
                uiState = uiState.copy(availableCards = cards)
            }
            .catch { }
            .launchIn(coroutineScope)
    }

    private fun observeSettings() {
        settingUseCase.getAllSettingsFlow()
            .onEach { settings ->
                val thresholdStr = settings[SettingsViewModel.KEY_FREE_SHIPPING_THRESHOLD] ?: ""
                val thresholdInPennies = ((thresholdStr.toDoubleOrNull() ?: 0.0) * 100).toLong()
                val defaultDiscount = (settings[SettingsViewModel.KEY_DEFAULT_DISCOUNT] ?: "0").toIntOrNull() ?: 0
                uiState = uiState.copy(
                    freeShippingEnabled = settings[SettingsViewModel.KEY_FREE_SHIPPING_ENABLED] == "true",
                    freeShippingThreshold = thresholdInPennies,
                    nicePricesEnabled = settings[SettingsViewModel.KEY_NICE_PRICES_ENABLED] == "true",
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
            .catch { }
            .launchIn(coroutineScope)
    }

    fun onToggleFabExpanded() {
        uiState = uiState.copy(isFabExpanded = !uiState.isFabExpanded)
    }

    fun onCollapseFab() {
        uiState = uiState.copy(isFabExpanded = false)
    }

    fun onShowCreateDialog() {
        uiState = uiState.copy(
            showCreateDialog = true,
            isFabExpanded = false,
            createFormState = CreateOrderFormState(
                discount = uiState.defaultDiscount.toString(),
                shippingPrice = uiState.defaultBubbleMailerCost,
                length = uiState.defaultBubbleMailerLength,
                width = uiState.defaultBubbleMailerWidth,
                height = "0"
            )
        )
    }

    fun onShowShippingLabelsDialog() {
        uiState = uiState.copy(showShippingLabelsDialog = true, isFabExpanded = false)
    }

    fun onDismissShippingLabelsDialog() {
        uiState = uiState.copy(showShippingLabelsDialog = false)
    }

    fun onShippingLabelsExported(orderIds: List<String>) {
        coroutineScope.launch {
            try {
                orderIds.forEach { orderId ->
                    orderUseCase.updateStatus(orderId, OrderStatus.LABEL_CREATED)
                }
                uiState = uiState.copy(
                    showShippingLabelsDialog = false,
                    toast = ToastState("Exported ${orderIds.size} shipping labels", isError = false)
                )
            } catch (e: Exception) {
                uiState = uiState.copy(
                    toast = ToastState(e.message ?: "Failed to update order statuses", isError = true)
                )
            }
        }
    }

    fun onShippingLabelsExportError(message: String) {
        uiState = uiState.copy(
            toast = ToastState(message, isError = true)
        )
    }

    fun onSingleOrderLabelExported(orderId: String) {
        coroutineScope.launch {
            try {
                orderUseCase.updateStatus(orderId, OrderStatus.LABEL_CREATED)
                uiState = uiState.copy(
                    toast = ToastState("Exported shipping label", isError = false)
                )
            } catch (e: Exception) {
                uiState = uiState.copy(
                    toast = ToastState(e.message ?: "Failed to update order status", isError = true)
                )
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        uiState = uiState.copy(searchQuery = query)
    }

    fun onStatusFilterToggled(status: String) {
        val currentFilters = uiState.statusFilters
        val newFilters = if (status in currentFilters) {
            currentFilters - status
        } else {
            currentFilters + status
        }
        uiState = uiState.copy(statusFilters = newFilters)
    }

    fun onSortOptionChanged(sortOption: OrderSortOption) {
        uiState = uiState.copy(sortOption = sortOption)
    }

    fun onEditOrder(order: Order) {
        val shippingPriceStr = (order.shippingCost / 100.0).let {
            if (it == it.toLong().toDouble()) {
                String.format("%.2f", it)
            } else {
                it.toString()
            }
        }
        uiState = uiState.copy(
            showCreateDialog = true,
            editingOrderId = order.id,
            createFormState = CreateOrderFormState(
                name = order.name,
                streetAddress = order.streetAddress,
                city = order.city,
                state = order.state,
                zipcode = order.zipcode,
                shippingType = order.shippingType ?: "Bubble mailer",
                shippingPrice = shippingPriceStr,
                trackingNumber = order.trackingNumber ?: "",
                discount = order.discount.toString(),
                length = if (order.length > 0) order.length.toBigDecimal().stripTrailingZeros().toPlainString() else "0",
                width = if (order.width > 0) order.width.toBigDecimal().stripTrailingZeros().toPlainString() else "0",
                height = if (order.height > 0) order.height.toBigDecimal().stripTrailingZeros().toPlainString() else "0",
                pounds = order.pounds.toString(),
                ounces = order.ounces.toString()
            )
        )
    }

    fun onDismissCreateDialog() {
        uiState = uiState.copy(
            showCreateDialog = false,
            editingOrderId = null,
            createFormState = CreateOrderFormState()
        )
    }

    fun onNameChanged(name: String) {
        uiState = uiState.copy(
            createFormState = uiState.createFormState.copy(name = name)
        )
    }

    fun onStreetAddressChanged(streetAddress: String) {
        uiState = uiState.copy(
            createFormState = uiState.createFormState.copy(streetAddress = streetAddress)
        )
    }

    fun onCityChanged(city: String) {
        uiState = uiState.copy(
            createFormState = uiState.createFormState.copy(city = city)
        )
    }

    fun onStateChanged(state: String) {
        val filtered = state.take(2).uppercase().filter { it.isLetter() }
        uiState = uiState.copy(
            createFormState = uiState.createFormState.copy(state = filtered)
        )
    }

    fun onZipcodeChanged(zipcode: String) {
        val filtered = zipcode.filter { it.isLetterOrDigit() }
        uiState = uiState.copy(
            createFormState = uiState.createFormState.copy(zipcode = filtered)
        )
    }

    fun onShippingTypeChanged(shippingType: String) {
        val defaultPrice = getDefaultShippingPrice(shippingType)
        val (defaultLength, defaultWidth, defaultHeight) = getDefaultDimensions(shippingType)
        uiState = uiState.copy(
            createFormState = uiState.createFormState.copy(
                shippingType = shippingType,
                shippingPrice = defaultPrice,
                length = defaultLength,
                width = defaultWidth,
                height = defaultHeight
            )
        )
    }

    private fun getDefaultDimensions(shippingType: String): Triple<String, String, String> {
        return when (shippingType) {
            "Envelope" -> Triple(uiState.defaultEnvelopeLength, uiState.defaultEnvelopeWidth, "0")
            "Bubble mailer" -> Triple(uiState.defaultBubbleMailerLength, uiState.defaultBubbleMailerWidth, "0")
            "Box" -> Triple(uiState.defaultBoxLength, uiState.defaultBoxWidth, uiState.defaultBoxHeight)
            else -> Triple("0", "0", "0")
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

    fun onShippingPriceChanged(price: String) {
        val filtered = price.filter { it.isDigit() || it == '.' }
        if (filtered.count { it == '.' } <= 1) {
            uiState = uiState.copy(
                createFormState = uiState.createFormState.copy(shippingPrice = filtered)
            )
        }
    }

    fun onCreateOrderTrackingNumberChanged(trackingNumber: String) {
        uiState = uiState.copy(
            createFormState = uiState.createFormState.copy(trackingNumber = trackingNumber)
        )
    }

    fun onDiscountChanged(discount: String) {
        val filtered = discount.filter { it.isDigit() }
        uiState = uiState.copy(
            createFormState = uiState.createFormState.copy(discount = filtered)
        )
    }

    fun onLengthChanged(value: String) {
        val filtered = value.filter { it.isDigit() || it == '.' }
        if (filtered.count { it == '.' } <= 1) {
            uiState = uiState.copy(
                createFormState = uiState.createFormState.copy(length = filtered)
            )
        }
    }

    fun onWidthChanged(value: String) {
        val filtered = value.filter { it.isDigit() || it == '.' }
        if (filtered.count { it == '.' } <= 1) {
            uiState = uiState.copy(
                createFormState = uiState.createFormState.copy(width = filtered)
            )
        }
    }

    fun onHeightChanged(value: String) {
        val filtered = value.filter { it.isDigit() || it == '.' }
        if (filtered.count { it == '.' } <= 1) {
            uiState = uiState.copy(
                createFormState = uiState.createFormState.copy(height = filtered)
            )
        }
    }

    fun onPoundsChanged(value: String) {
        val filtered = value.filter { it.isDigit() }
        uiState = uiState.copy(
            createFormState = uiState.createFormState.copy(pounds = filtered)
        )
    }

    fun onOuncesChanged(value: String) {
        val filtered = value.filter { it.isDigit() }
        val intValue = filtered.toIntOrNull() ?: 0
        val capped = if (intValue > 15) "15" else filtered
        uiState = uiState.copy(
            createFormState = uiState.createFormState.copy(ounces = capped)
        )
    }

    fun onCreateOrUpdateOrder() {
        if (!uiState.createFormState.isValid()) return

        uiState = uiState.copy(
            createFormState = uiState.createFormState.copy(isSaving = true)
        )

        val isEditMode = uiState.editingOrderId != null

        coroutineScope.launch {
            try {
                val form = uiState.createFormState
                val shippingCostInPennies = ((form.shippingPrice.toDoubleOrNull() ?: 0.0) * 100).toLong()

                val trackingNumber = form.trackingNumber.trim().takeIf { it.isNotBlank() }
                val discount = form.discount.toIntOrNull() ?: 0
                val length = form.length.toDoubleOrNull() ?: 0.0
                val width = form.width.toDoubleOrNull() ?: 0.0
                val height = form.height.toDoubleOrNull() ?: 0.0
                val pounds = form.pounds.toIntOrNull() ?: 0
                val ounces = form.ounces.toIntOrNull() ?: 0

                if (isEditMode) {
                    val existingOrder = uiState.orders.find { it.id == uiState.editingOrderId }
                    val order = Order(
                        id = uiState.editingOrderId!!,
                        name = form.name.toTitleCase(),
                        streetAddress = form.streetAddress.toTitleCase(),
                        city = form.city.toTitleCase(),
                        state = form.state.uppercase(),
                        zipcode = form.zipcode,
                        shippingType = form.shippingType,
                        shippingCost = shippingCostInPennies,
                        status = existingOrder?.status ?: OrderStatus.NEW,
                        createdAt = existingOrder?.createdAt ?: System.currentTimeMillis(),
                        trackingNumber = trackingNumber,
                        discount = discount,
                        cards = existingOrder?.cards ?: emptyList(),
                        length = length,
                        width = width,
                        height = height,
                        pounds = pounds,
                        ounces = ounces
                    )
                    orderUseCase.updateOrder(order)
                } else {
                    val order = Order(
                        id = UUID.randomUUID().toString(),
                        name = form.name.toTitleCase(),
                        streetAddress = form.streetAddress.toTitleCase(),
                        city = form.city.toTitleCase(),
                        state = form.state.uppercase(),
                        zipcode = form.zipcode,
                        shippingType = form.shippingType,
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
                }

                uiState = uiState.copy(
                    showCreateDialog = false,
                    editingOrderId = null,
                    createFormState = CreateOrderFormState()
                )
            } catch (e: Exception) {
                uiState = uiState.copy(
                    error = e.message ?: if (isEditMode) "Failed to update order" else "Failed to create order",
                    createFormState = uiState.createFormState.copy(isSaving = false)
                )
            }
        }
    }

    fun onShowAddCardsDialog(orderId: String) {
        uiState = uiState.copy(
            addCardsDialogState = AddCardsDialogState(orderId = orderId)
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

    fun onProceedToPriceConfirmation() {
        uiState.addCardsDialogState?.let { dialogState ->
            val selectedCards = uiState.availableCards.filter { it.id in dialogState.selectedCardIds }
            val initialPrices = buildInitialCardPrices(selectedCards)
            
            uiState = uiState.copy(
                addCardsDialogState = dialogState.copy(
                    step = AddCardsStep.CONFIRM_PRICES,
                    cardPrices = initialPrices
                )
            )
        }
    }

    private fun buildInitialCardPrices(selectedCards: List<com.wtscards.data.model.Card>): Map<String, String> {
        return selectedCards.associate { card ->
            card.id to if (card.priceInPennies > 0) {
                (card.priceInPennies / 100.0).toString()
            } else {
                ""
            }
        }
    }

    fun onCardPriceChanged(cardId: String, price: String) {
        uiState.addCardsDialogState?.let { dialogState ->
            val updatedPrices = dialogState.cardPrices.toMutableMap()
            updatedPrices[cardId] = price
            uiState = uiState.copy(
                addCardsDialogState = dialogState.copy(cardPrices = updatedPrices)
            )
        }
    }

    fun onConfirmAddCards() {
        uiState.addCardsDialogState?.let { dialogState ->
            uiState = uiState.copy(
                addCardsDialogState = dialogState.copy(isSaving = true)
            )

            coroutineScope.launch {
                try {
                    val cardPrices = convertCardPricesToPennies(dialogState)
                    orderUseCase.addCardsToOrder(dialogState.orderId, cardPrices)

                    uiState = uiState.copy(
                        addCardsDialogState = null,
                        toast = ToastState("Cards added to order successfully", isError = false)
                    )
                } catch (e: Exception) {
                    uiState = uiState.copy(
                        addCardsDialogState = dialogState.copy(isSaving = false),
                        toast = ToastState(e.message ?: "Failed to add cards to order", isError = true)
                    )
                }
            }
        }
    }

    fun onStatusChanged(orderId: String, newStatus: String) {
        coroutineScope.launch {
            try {
                orderUseCase.updateStatus(orderId, newStatus)
                uiState = uiState.copy(
                    toast = ToastState("Order updated to $newStatus", isError = false)
                )
            } catch (e: Exception) {
                uiState = uiState.copy(
                    toast = ToastState(e.message ?: "Failed to update order status", isError = true)
                )
            }
        }
    }

    fun showToast(message: String) {
        uiState = uiState.copy(toast = ToastState(message))
    }

    fun clearToast() {
        uiState = uiState.copy(toast = null)
    }

    private fun convertCardPricesToPennies(dialogState: AddCardsDialogState): Map<String, Long> {
        return dialogState.cardPrices.mapValues { (_, priceString) ->
            val priceDouble = priceString.toDoubleOrNull() ?: 0.0
            (priceDouble * 100).toLong()
        }
    }

    fun onDeleteOrder(orderId: String) {
        coroutineScope.launch {
            try {
                orderUseCase.deleteOrder(orderId)
                uiState = uiState.copy(
                    toast = ToastState("Order deleted", isError = false)
                )
            } catch (e: Exception) {
                uiState = uiState.copy(
                    toast = ToastState(e.message ?: "Failed to delete order", isError = true)
                )
            }
        }
    }

    fun onShowRemoveCardDialog(orderId: String, cardId: String, cardName: String) {
        uiState = uiState.copy(
            removeCardDialogState = RemoveCardDialogState(
                orderId = orderId,
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
                    orderUseCase.removeCardFromOrder(dialogState.orderId, dialogState.cardId)
                    uiState = uiState.copy(
                        removeCardDialogState = null,
                        toast = ToastState("Card removed from order", isError = false)
                    )
                } catch (e: Exception) {
                    uiState = uiState.copy(
                        removeCardDialogState = dialogState.copy(isRemoving = false),
                        toast = ToastState(e.message ?: "Failed to remove card", isError = true)
                    )
                }
            }
        }
    }

    fun onShowUpgradeShippingDialog(orderId: String, cardCount: Int) {
        uiState = uiState.copy(
            upgradeShippingDialogState = UpgradeShippingDialogState(
                orderId = orderId,
                cardCount = cardCount
            )
        )
    }

    fun onDismissUpgradeShippingDialog() {
        uiState = uiState.copy(upgradeShippingDialogState = null)
    }

    fun onConfirmUpgradeShipping() {
        uiState.upgradeShippingDialogState?.let { dialogState ->
            uiState = uiState.copy(
                upgradeShippingDialogState = dialogState.copy(isProcessing = true)
            )

            coroutineScope.launch {
                try {
                    orderUseCase.updateShippingType(
                        orderId = dialogState.orderId,
                        shippingType = "Bubble mailer",
                        shippingCost = 500L
                    )
                    uiState = uiState.copy(
                        upgradeShippingDialogState = null,
                        toast = ToastState("Upgraded to bubble mailer", isError = false)
                    )
                } catch (e: Exception) {
                    uiState = uiState.copy(
                        upgradeShippingDialogState = dialogState.copy(isProcessing = false),
                        toast = ToastState(e.message ?: "Failed to upgrade shipping", isError = true)
                    )
                }
            }
        }
    }

    fun onShowSplitOrderDialog(orderId: String, cardCount: Int) {
        val splitCount = kotlin.math.ceil(cardCount / 15.0).toInt()
        uiState = uiState.copy(
            splitOrderDialogState = SplitOrderDialogState(
                orderId = orderId,
                cardCount = cardCount,
                splitCount = splitCount
            )
        )
    }

    fun onDismissSplitOrderDialog() {
        uiState = uiState.copy(splitOrderDialogState = null)
    }

    fun onConfirmSplitOrder() {
        uiState.splitOrderDialogState?.let { dialogState ->
            uiState = uiState.copy(
                splitOrderDialogState = dialogState.copy(isProcessing = true)
            )

            coroutineScope.launch {
                try {
                    orderUseCase.splitOrder(dialogState.orderId, dialogState.splitCount)
                    uiState = uiState.copy(
                        splitOrderDialogState = null,
                        toast = ToastState("Order split into ${dialogState.splitCount} orders", isError = false)
                    )
                } catch (e: Exception) {
                    uiState = uiState.copy(
                        splitOrderDialogState = dialogState.copy(isProcessing = false),
                        toast = ToastState(e.message ?: "Failed to split order", isError = true)
                    )
                }
            }
        }
    }

    fun onShowTotalOverrideDialog(orderId: String, currentTotalOverride: Long?) {
        val valueStr = if (currentTotalOverride != null) {
            String.format("%.2f", currentTotalOverride / 100.0)
        } else {
            ""
        }
        uiState = uiState.copy(
            totalOverrideDialogState = TotalOverrideDialogState(
                orderId = orderId,
                totalOverride = valueStr
            )
        )
    }

    fun onDismissTotalOverrideDialog() {
        uiState = uiState.copy(totalOverrideDialogState = null)
    }

    fun onTotalOverrideChanged(value: String) {
        uiState.totalOverrideDialogState?.let { dialogState ->
            val filtered = value.filter { it.isDigit() || it == '.' }
            if (filtered.count { it == '.' } <= 1) {
                uiState = uiState.copy(
                    totalOverrideDialogState = dialogState.copy(totalOverride = filtered)
                )
            }
        }
    }

    fun onConfirmTotalOverride() {
        uiState.totalOverrideDialogState?.let { dialogState ->
            uiState = uiState.copy(
                totalOverrideDialogState = dialogState.copy(isSaving = true)
            )

            coroutineScope.launch {
                try {
                    val totalOverride = if (dialogState.totalOverride.isBlank()) {
                        null
                    } else {
                        ((dialogState.totalOverride.toDoubleOrNull() ?: 0.0) * 100).toLong()
                    }
                    orderUseCase.updateTotalOverride(dialogState.orderId, totalOverride)
                    uiState = uiState.copy(
                        totalOverrideDialogState = null,
                        toast = ToastState("Total override updated", isError = false)
                    )
                } catch (e: Exception) {
                    uiState = uiState.copy(
                        totalOverrideDialogState = dialogState.copy(isSaving = false),
                        toast = ToastState(e.message ?: "Failed to update total override", isError = true)
                    )
                }
            }
        }
    }

    fun onShowTrackingNumberDialog(orderId: String, currentTrackingNumber: String?) {
        uiState = uiState.copy(
            trackingNumberDialogState = TrackingNumberDialogState(
                orderId = orderId,
                trackingNumber = currentTrackingNumber ?: ""
            )
        )
    }

    fun onDismissTrackingNumberDialog() {
        uiState = uiState.copy(trackingNumberDialogState = null)
    }

    fun onTrackingNumberChanged(trackingNumber: String) {
        uiState.trackingNumberDialogState?.let { dialogState ->
            uiState = uiState.copy(
                trackingNumberDialogState = dialogState.copy(trackingNumber = trackingNumber)
            )
        }
    }

    fun onConfirmTrackingNumber() {
        uiState.trackingNumberDialogState?.let { dialogState ->
            uiState = uiState.copy(
                trackingNumberDialogState = dialogState.copy(isSaving = true)
            )

            coroutineScope.launch {
                try {
                    val trackingNumber = dialogState.trackingNumber.trim().ifBlank { null }
                    orderUseCase.updateTrackingNumber(dialogState.orderId, trackingNumber)
                    uiState = uiState.copy(
                        trackingNumberDialogState = null,
                        toast = ToastState("Tracking number updated", isError = false)
                    )
                } catch (e: Exception) {
                    uiState = uiState.copy(
                        trackingNumberDialogState = dialogState.copy(isSaving = false),
                        toast = ToastState(e.message ?: "Failed to update tracking number", isError = true)
                    )
                }
            }
        }
    }

    private fun String.toTitleCase(): String {
        return split(" ").joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { it.uppercase() }
        }
    }
}
