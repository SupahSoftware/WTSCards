package com.wtscards.ui.screens.orders

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.wtscards.data.model.Order
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

    fun onShowCreateDialog() {
        uiState = uiState.copy(showCreateDialog = true)
    }

    fun onDismissCreateDialog() {
        uiState = uiState.copy(
            showCreateDialog = false,
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

    fun onCreateOrder() {
        if (!uiState.createFormState.isValid()) return

        uiState = uiState.copy(
            createFormState = uiState.createFormState.copy(isSaving = true)
        )

        coroutineScope.launch {
            try {
                val form = uiState.createFormState
                val order = Order(
                    id = UUID.randomUUID().toString(),
                    name = form.name.toTitleCase(),
                    streetAddress = form.streetAddress.toTitleCase(),
                    city = form.city.toTitleCase(),
                    state = form.state.uppercase(),
                    zipcode = form.zipcode,
                    createdAt = System.currentTimeMillis(),
                    cards = emptyList()
                )

                orderUseCase.createOrder(order)

                uiState = uiState.copy(
                    showCreateDialog = false,
                    createFormState = CreateOrderFormState()
                )
            } catch (e: Exception) {
                uiState = uiState.copy(
                    error = e.message ?: "Failed to create order",
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
                        toastMessage = "Cards added to order successfully"
                    )
                } catch (e: Exception) {
                    uiState = uiState.copy(
                        addCardsDialogState = dialogState.copy(isSaving = false),
                        toastMessage = e.message ?: "Failed to add cards to order"
                    )
                }
            }
        }
    }

    fun clearToast() {
        uiState = uiState.copy(toastMessage = null)
    }

    private fun String.toTitleCase(): String {
        return split(" ").joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { it.uppercase() }
        }
    }
}
