package com.wtscards.data.db

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.wtscards.data.model.Card
import com.wtscards.db.CardEntity
import com.wtscards.db.WTSCardsDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class CardLocalDataSource(private val database: WTSCardsDatabase) {

    private val queries = database.cardQueries

    fun getAllCardsFlow(): Flow<List<Card>> {
        return queries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities -> entities.map { it.toCard() } }
    }

    suspend fun getAllCards(): List<Card> = withContext(Dispatchers.IO) {
        queries.selectAll().executeAsList().map { it.toCard() }
    }

    suspend fun getCardById(id: String): Card? = withContext(Dispatchers.IO) {
        queries.selectById(id).executeAsOneOrNull()?.toCard()
    }

    suspend fun getCardsByIds(ids: List<String>): List<Card> = withContext(Dispatchers.IO) {
        queries.selectByIds(ids).executeAsList().map { it.toCard() }
    }

    suspend fun insertCard(card: Card) = withContext(Dispatchers.IO) {
        queries.insert(
            sportsCardProId = card.sportsCardProId,
            name = card.name,
            setName = card.setName,
            priceInPennies = card.priceInPennies,
            gradedString = card.gradedString,
            quantity = card.quantity.toLong()
        )
    }

    suspend fun insertOrReplaceCard(card: Card) = withContext(Dispatchers.IO) {
        queries.insertOrReplace(
            sportsCardProId = card.sportsCardProId,
            name = card.name,
            setName = card.setName,
            priceInPennies = card.priceInPennies,
            gradedString = card.gradedString,
            quantity = card.quantity.toLong()
        )
    }

    suspend fun insertOrReplaceCards(cards: List<Card>) = withContext(Dispatchers.IO) {
        queries.transaction {
            cards.forEach { card ->
                queries.insertOrReplace(
                    sportsCardProId = card.sportsCardProId,
                    name = card.name,
                    setName = card.setName,
                    priceInPennies = card.priceInPennies,
                    gradedString = card.gradedString,
                    quantity = card.quantity.toLong()
                )
            }
        }
    }

    suspend fun updatePrice(id: String, priceInPennies: Long) = withContext(Dispatchers.IO) {
        queries.updatePrice(priceInPennies, id)
    }

    suspend fun deleteAllCards() = withContext(Dispatchers.IO) {
        queries.deleteAll()
    }

    suspend fun deleteCardsByIds(ids: List<String>) = withContext(Dispatchers.IO) {
        queries.deleteByIds(ids)
    }

    suspend fun getCount(): Long = withContext(Dispatchers.IO) {
        queries.count().executeAsOne()
    }

    private fun CardEntity.toCard(): Card {
        return Card(
            sportsCardProId = sportsCardProId,
            name = name,
            setName = setName,
            priceInPennies = priceInPennies,
            gradedString = gradedString,
            quantity = quantity.toInt()
        )
    }
}
