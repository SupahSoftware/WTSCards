package com.wtscards.domain.usecase

import com.wtscards.data.db.CardLocalDataSource
import com.wtscards.data.db.OrderLocalDataSource
import com.wtscards.data.model.Order
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
}
