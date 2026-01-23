package com.wtscards.domain.usecase

import com.wtscards.data.db.CardLocalDataSource
import com.wtscards.data.db.OrderLocalDataSource
import com.wtscards.data.model.Order
import com.wtscards.data.model.OrderStatus
import kotlinx.coroutines.flow.Flow

class OrderUseCaseImpl(
    private val orderLocalDataSource: OrderLocalDataSource,
    private val cardLocalDataSource: CardLocalDataSource
) : OrderUseCase {

    override fun getAllOrdersFlow(): Flow<List<Order>> {
        return orderLocalDataSource.getAllOrdersFlow()
    }

    override suspend fun getAllOrders(): List<Order> {
        return orderLocalDataSource.getAllOrders()
    }

    override suspend fun getOrderById(id: String): Order? {
        return orderLocalDataSource.getOrderById(id)
    }

    override suspend fun createOrder(order: Order) {
        orderLocalDataSource.insertOrder(order)
    }

    override suspend fun updateOrder(order: Order) {
        orderLocalDataSource.updateOrder(order)
    }

    override suspend fun updateStatus(orderId: String, status: String) {
        orderLocalDataSource.updateStatus(orderId, status)
    }

    override suspend fun deleteOrder(id: String) {
        orderLocalDataSource.deleteOrder(id)
    }

    override suspend fun addCardsToOrder(orderId: String, cardPrices: Map<String, Long>) {
        orderLocalDataSource.addCardsToOrder(orderId, cardPrices.keys.toList())
        cardPrices.forEach { (cardId, priceSold) ->
            cardLocalDataSource.updatePriceSold(cardId, priceSold)
        }
    }

    override suspend fun removeCardFromOrder(orderId: String, cardId: String) {
        orderLocalDataSource.removeCardFromOrder(orderId, cardId)
        cardLocalDataSource.clearPriceSold(cardId)
    }

    override suspend fun updateShippingType(orderId: String, shippingType: String, shippingCost: Long) {
        orderLocalDataSource.updateShippingType(orderId, shippingType, shippingCost)
    }

    override suspend fun splitOrder(orderId: String, splitCount: Int) {
        val originalOrder = orderLocalDataSource.getOrderById(orderId)
            ?: throw IllegalArgumentException("Order not found")

        val cards = originalOrder.cards
        val cardsPerOrder = kotlin.math.ceil(cards.size / splitCount.toDouble()).toInt()
        val cardChunks = cards.chunked(cardsPerOrder)

        // Update original order with first chunk of cards and reset to NEW status
        val firstChunk = cardChunks.first()
        orderLocalDataSource.replaceOrderCards(orderId, firstChunk.map { it.id })
        orderLocalDataSource.updateStatus(orderId, OrderStatus.NEW)

        // Create new orders for remaining chunks with NEW status
        cardChunks.drop(1).forEach { cardChunk ->
            val newOrder = Order(
                id = java.util.UUID.randomUUID().toString(),
                name = originalOrder.name,
                streetAddress = originalOrder.streetAddress,
                city = originalOrder.city,
                state = originalOrder.state,
                zipcode = originalOrder.zipcode,
                shippingType = originalOrder.shippingType,
                shippingCost = originalOrder.shippingCost,
                status = OrderStatus.NEW,
                createdAt = originalOrder.createdAt,
                cards = cardChunk
            )
            orderLocalDataSource.insertOrder(newOrder)
        }
    }
}
