package com.wtscards.ui.screens.orders

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.wtscards.data.model.Order
import com.wtscards.data.model.OrderStatus
import com.wtscards.domain.usecase.CardUseCase
import com.wtscards.domain.usecase.OrderUseCase
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
    private val coroutineScope: CoroutineScope
) {
    var uiState by mutableStateOf(OrderUiState())
        private set

    init {
        observeOrders()
        observeCards()
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
            .catch { e ->
                // Silently fail for cards loading
            }
            .launchIn(coroutineScope)
    }

    fun onToggleFabExpanded() {
        uiState = uiState.copy(isFabExpanded = !uiState.isFabExpanded)
    }

    fun onCollapseFab() {
        uiState = uiState.copy(isFabExpanded = false)
    }

    fun onShowCreateDialog() {
        uiState = uiState.copy(showCreateDialog = true, isFabExpanded = false)
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
                shippingPrice = shippingPriceStr
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
        // Limit to 2 characters and convert to uppercase
        val filtered = state.take(2).uppercase().filter { it.isLetter() }
        uiState = uiState.copy(
            createFormState = uiState.createFormState.copy(state = filtered)
        )
    }

    fun onZipcodeChanged(zipcode: String) {
        // Only allow alphanumeric characters
        val filtered = zipcode.filter { it.isLetterOrDigit() }
        uiState = uiState.copy(
            createFormState = uiState.createFormState.copy(zipcode = filtered)
        )
    }

    fun onShippingTypeChanged(shippingType: String) {
        // Set default price based on shipping type
        val defaultPrice = when (shippingType) {
            "Bubble mailer" -> "5.00"
            "Envelope" -> "1.00"
            "Box" -> "10.00"
            "Other" -> "0.00"
            else -> "0.00"
        }
        uiState = uiState.copy(
            createFormState = uiState.createFormState.copy(
                shippingType = shippingType,
                shippingPrice = defaultPrice
            )
        )
    }

    fun onShippingPriceChanged(price: String) {
        // Only allow digits and decimal point
        val filtered = price.filter { it.isDigit() || it == '.' }
        // Only allow one decimal point
        if (filtered.count { it == '.' } <= 1) {
            uiState = uiState.copy(
                createFormState = uiState.createFormState.copy(shippingPrice = filtered)
            )
        }
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

                if (isEditMode) {
                    // Find the existing order to preserve createdAt and cards
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
                        createdAt = existingOrder?.createdAt ?: System.currentTimeMillis(),
                        cards = existingOrder?.cards ?: emptyList()
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
                        createdAt = System.currentTimeMillis(),
                        cards = emptyList()
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
            // Auto-populate prices from card data
            val selectedCards = uiState.availableCards.filter { it.id in dialogState.selectedCardIds }
            val initialPrices = selectedCards.associate { card ->
                card.id to if (card.priceInPennies > 0) {
                    (card.priceInPennies / 100.0).toString()
                } else {
                    ""
                }
            }
            
            uiState = uiState.copy(
                addCardsDialogState = dialogState.copy(
                    step = AddCardsStep.CONFIRM_PRICES,
                    cardPrices = initialPrices
                )
            )
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
                    // Convert prices from dollars to pennies
                    val cardPrices = dialogState.cardPrices.mapValues { (_, priceStr) ->
                        val priceDouble = priceStr.toDoubleOrNull() ?: 0.0
                        (priceDouble * 100).toLong()
                    }

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

    fun clearToast() {
        uiState = uiState.copy(toast = null)
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

    // Upgrade Shipping Dialog
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

    // Split Order Dialog
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

    private fun String.toTitleCase(): String {
        return split(" ").joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { it.uppercase() }
        }
    }
}
