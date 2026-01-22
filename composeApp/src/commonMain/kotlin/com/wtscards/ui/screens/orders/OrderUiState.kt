package com.wtscards.ui.screens.orders

import com.wtscards.data.model.Card
import com.wtscards.data.model.Order

data class OrderUiState(
    val isLoading: Boolean = false,
    val orders: List<Order> = emptyList(),
    val error: String? = null,
    val showCreateDialog: Boolean = false,
    val createFormState: CreateOrderFormState = CreateOrderFormState(),
    val addCardsDialogState: AddCardsDialogState? = null,
    val availableCards: List<Card> = emptyList(),
    val toastMessage: String? = null
)

data class CreateOrderFormState(
    val name: String = "",
    val streetAddress: String = "",
    val city: String = "",
    val state: String = "",
    val zipcode: String = "",
    val shippingType: String = "Bubble mailer",
    val shippingPrice: String = "5.00",
    val isSaving: Boolean = false
) {
    fun isValid(): Boolean {
        return name.isNotBlank() &&
                streetAddress.isNotBlank() &&
                city.isNotBlank() &&
                state.length == 2 &&
                zipcode.isNotBlank() &&
                zipcode.all { it.isLetterOrDigit() } &&
                !isSaving
    }
}

data class AddCardsDialogState(
    val orderId: String,
    val step: AddCardsStep = AddCardsStep.SELECT_CARDS,
    val searchQuery: String = "",
    val selectedCardIds: Set<String> = emptySet(),
    val cardPrices: Map<String, String> = emptyMap(), // cardId -> price string
    val isSaving: Boolean = false
)

enum class AddCardsStep {
    SELECT_CARDS,
    CONFIRM_PRICES
}
