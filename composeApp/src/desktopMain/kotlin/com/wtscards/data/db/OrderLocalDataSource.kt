package com.wtscards.data.db

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.wtscards.data.model.Card
import com.wtscards.data.model.Order
import com.wtscards.db.CardEntity
import com.wtscards.db.OrderEntity
import com.wtscards.db.WTSCardsDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class OrderLocalDataSource(private val database: WTSCardsDatabase) {

    private val orderQueries = database.orderQueries
    private val cardQueries = database.cardQueries

    fun getAllOrdersFlow(): Flow<List<Order>> {
        // Combine flows from OrderEntity, OrderCardEntity, and CardEntity tables
        // so updates to any of them trigger a refresh
        return kotlinx.coroutines.flow.combine(
            orderQueries.selectAll().asFlow().mapToList(Dispatchers.IO),
            orderQueries.selectAllOrderCards().asFlow().mapToList(Dispatchers.IO),
            cardQueries.selectAll().asFlow().mapToList(Dispatchers.IO)
        ) { orderEntities, _, _ ->
            // Re-fetch with cards whenever any table changes
            orderEntities.map { orderEntity ->
                orderEntity.toOrder(getCardsForOrder(orderEntity.id))
            }
        }
    }

    suspend fun getAllOrders(): List<Order> = withContext(Dispatchers.IO) {
        orderQueries.selectAll().executeAsList().map { orderEntity ->
            orderEntity.toOrder(getCardsForOrder(orderEntity.id))
        }
    }

    suspend fun getOrderById(id: String): Order? = withContext(Dispatchers.IO) {
        orderQueries.selectById(id).executeAsOneOrNull()?.let { orderEntity ->
            orderEntity.toOrder(getCardsForOrder(orderEntity.id))
        }
    }

    suspend fun insertOrder(order: Order) = withContext(Dispatchers.IO) {
        database.transaction {
            orderQueries.insert(
                id = order.id,
                name = order.name,
                streetAddress = order.streetAddress,
                city = order.city,
                state = order.state,
                zipcode = order.zipcode,
                shippingType = order.shippingType,
                shippingCost = order.shippingCost,
                status = order.status,
                createdAt = order.createdAt
            )

            // Insert order-card relationships
            order.cards.forEach { card ->
                orderQueries.insertOrderCard(
                    orderId = order.id,
                    cardId = card.id
                )
            }
        }
    }

    suspend fun updateOrder(order: Order) = withContext(Dispatchers.IO) {
        orderQueries.updateOrder(
            name = order.name,
            streetAddress = order.streetAddress,
            city = order.city,
            state = order.state,
            zipcode = order.zipcode,
            shippingType = order.shippingType,
            shippingCost = order.shippingCost,
            id = order.id
        )
    }

    suspend fun updateStatus(orderId: String, status: String) = withContext(Dispatchers.IO) {
        orderQueries.updateStatus(status = status, id = orderId)
    }

    suspend fun deleteOrder(id: String) = withContext(Dispatchers.IO) {
        database.transaction {
            orderQueries.deleteOrderCardsByOrderId(id)
            orderQueries.deleteById(id)
        }
    }

    suspend fun addCardsToOrder(orderId: String, cardIds: List<String>) = withContext(Dispatchers.IO) {
        database.transaction {
            cardIds.forEach { cardId ->
                orderQueries.insertOrderCard(
                    orderId = orderId,
                    cardId = cardId
                )
            }
        }
    }

    suspend fun removeCardFromOrder(orderId: String, cardId: String) = withContext(Dispatchers.IO) {
        orderQueries.deleteOrderCard(orderId, cardId)
    }

    suspend fun getCount(): Long = withContext(Dispatchers.IO) {
        orderQueries.count().executeAsOne()
    }

    private suspend fun getCardsForOrder(orderId: String): List<Card> {
        return orderQueries.selectCardsByOrderId(orderId)
            .executeAsList()
            .map { it.toCard() }
    }

    private fun OrderEntity.toOrder(cards: List<Card>): Order {
        return Order(
            id = id,
            name = name,
            streetAddress = streetAddress,
            city = city,
            state = state,
            zipcode = zipcode,
            shippingType = shippingType,
            shippingCost = shippingCost,
            status = status,
            createdAt = createdAt,
            cards = cards
        )
    }

    private fun CardEntity.toCard(): Card {
        return Card(
            id = id,
            sportsCardProId = sportsCardProId,
            name = name,
            setName = setName,
            priceInPennies = priceInPennies,
            gradedString = gradedString,
            priceSold = priceSold
        )
    }
}
