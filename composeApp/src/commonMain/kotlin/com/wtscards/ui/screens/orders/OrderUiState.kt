package com.wtscards.ui.screens.orders

import com.wtscards.data.model.Card
import com.wtscards.data.model.Order
import com.wtscards.data.model.OrderStatus

enum class OrderSortOption {
    DATE_DESC,  // Newest first (default)
    DATE_ASC,   // Oldest first
    TOTAL_DESC, // Most expensive first
    TOTAL_ASC   // Cheapest first
}

data class OrderUiState(
    val isLoading: Boolean = false,
    val orders: List<Order> = emptyList(),
    val error: String? = null,
    val searchQuery: String = "",
    val statusFilters: Set<String> = OrderStatus.allStatuses.toSet(),
    val sortOption: OrderSortOption = OrderSortOption.DATE_DESC,
    val isFabExpanded: Boolean = false,
    val showCreateDialog: Boolean = false,
    val showShippingLabelsDialog: Boolean = false,
    val editingOrderId: String? = null,
    val createFormState: CreateOrderFormState = CreateOrderFormState(),
    val addCardsDialogState: AddCardsDialogState? = null,
    val removeCardDialogState: RemoveCardDialogState? = null,
    val upgradeShippingDialogState: UpgradeShippingDialogState? = null,
    val splitOrderDialogState: SplitOrderDialogState? = null,
    val trackingNumberDialogState: TrackingNumberDialogState? = null,
    val availableCards: List<Card> = emptyList(),
    val toast: ToastState? = null,
    val freeShippingEnabled: Boolean = false,
    val freeShippingThreshold: Long = 0,
    val nicePricesEnabled: Boolean = false,
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
    val newStatusOrders: List<Order>
        get() = orders.filter { it.status == OrderStatus.NEW }

    val filteredOrders: List<Order>
        get() {
            var result = orders

            result = result.filter { it.status in statusFilters }

            if (searchQuery.isNotBlank()) {
                val query = searchQuery.lowercase()
                result = result.filter { order ->
                    order.name.lowercase().contains(query) ||
                    order.streetAddress.lowercase().contains(query) ||
                    order.city.lowercase().contains(query) ||
                    order.state.lowercase().contains(query) ||
                    order.zipcode.lowercase().contains(query) ||
                    order.cards.any { card -> card.name.lowercase().contains(query) }
                }
            }

            result = when (sortOption) {
                OrderSortOption.DATE_DESC -> result.sortedByDescending { it.createdAt }
                OrderSortOption.DATE_ASC -> result.sortedBy { it.createdAt }
                OrderSortOption.TOTAL_DESC -> result.sortedByDescending { order ->
                    order.cards.sumOf { it.priceSold ?: 0 } + order.shippingCost
                }
                OrderSortOption.TOTAL_ASC -> result.sortedBy { order ->
                    order.cards.sumOf { it.priceSold ?: 0 } + order.shippingCost
                }
            }

            return result
        }
}

data class ToastState(
    val message: String,
    val isError: Boolean = false
)

data class CreateOrderFormState(
    val name: String = "",
    val streetAddress: String = "",
    val city: String = "",
    val state: String = "",
    val zipcode: String = "",
    val shippingType: String = "Bubble mailer",
    val shippingPrice: String = "5.00",
    val trackingNumber: String = "",
    val discount: String = "0",
    val length: String = "0",
    val width: String = "0",
    val height: String = "0",
    val pounds: String = "0",
    val ounces: String = "0",
    val isSaving: Boolean = false
) {
    fun isValid(): Boolean {
        val stateValid = state.isEmpty() || state.length == 2
        val zipcodeValid = zipcode.isEmpty() || zipcode.all { it.isLetterOrDigit() }
        return name.isNotBlank() &&
                stateValid &&
                zipcodeValid &&
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

data class RemoveCardDialogState(
    val orderId: String,
    val cardId: String,
    val cardName: String,
    val isRemoving: Boolean = false
)

data class UpgradeShippingDialogState(
    val orderId: String,
    val cardCount: Int,
    val isProcessing: Boolean = false
)

data class SplitOrderDialogState(
    val orderId: String,
    val cardCount: Int,
    val splitCount: Int,
    val isProcessing: Boolean = false
)

data class TrackingNumberDialogState(
    val orderId: String,
    val trackingNumber: String = "",
    val isSaving: Boolean = false
)
