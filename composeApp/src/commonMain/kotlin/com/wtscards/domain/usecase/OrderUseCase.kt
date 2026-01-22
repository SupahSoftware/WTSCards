package com.wtscards.domain.usecase

import com.wtscards.data.model.Order
import kotlinx.coroutines.flow.Flow

interface OrderUseCase {
    fun getAllOrdersFlow(): Flow<List<Order>>
    suspend fun getAllOrders(): List<Order>
    suspend fun getOrderById(id: String): Order?
    suspend fun createOrder(order: Order)
    suspend fun deleteOrder(id: String)
    suspend fun addCardsToOrder(orderId: String, cardPrices: Map<String, Long>)
}
