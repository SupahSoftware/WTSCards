package com.wtscards.domain.usecase

import com.wtscards.data.db.CardLocalDataSource
import com.wtscards.data.model.Card
import kotlinx.coroutines.flow.Flow

class CardUseCaseImpl(
    private val localDataSource: CardLocalDataSource
) : CardUseCase {

    override fun getAllCardsFlow(): Flow<List<Card>> {
        return localDataSource.getAllCardsFlow()
    }

    override suspend fun getAllCards(): List<Card> {
        return localDataSource.getAllCards()
    }

    override suspend fun findCollisions(cards: List<Card>): List<Card> {
        val incomingIds = cards.mapNotNull { it.sportsCardProId }
        if (incomingIds.isEmpty()) return emptyList()
        return localDataSource.getCardsBySportsCardProIds(incomingIds)
    }

    override suspend fun importCards(cards: List<Card>, strategy: ImportStrategy) {
        when (strategy) {
            ImportStrategy.OVERWRITE_ALL -> importWithOverwrite(cards)
            ImportStrategy.UPDATE_PRICES_ONLY -> importWithPriceUpdates(cards)
            ImportStrategy.SAFE_IMPORT -> importNewCardsOnly(cards)
        }
    }

    private suspend fun importWithOverwrite(cards: List<Card>) {
        localDataSource.deleteAllCards()
        localDataSource.insertOrReplaceCards(cards)
    }

    private suspend fun importWithPriceUpdates(cards: List<Card>) {
        val existingCards = localDataSource.getAllCards()
        val existingBySportsCardProId = buildExistingCardsMap(existingCards)

        cards.forEach { card ->
            updateOrInsertCard(card, existingBySportsCardProId)
        }
    }

    private fun buildExistingCardsMap(existingCards: List<Card>): Map<String, Card> {
        return existingCards
            .filter { it.sportsCardProId != null }
            .associateBy { it.sportsCardProId!! }
    }

    private suspend fun updateOrInsertCard(card: Card, existingBySportsCardProId: Map<String, Card>) {
        val existingCard = card.sportsCardProId?.let { existingBySportsCardProId[it] }
        if (existingCard != null) {
            localDataSource.updatePrice(existingCard.id, card.priceInPennies)
        } else {
            localDataSource.insertCard(card)
        }
    }

    private suspend fun importNewCardsOnly(cards: List<Card>) {
        val existingIds = getExistingSportsCardProIds()
        val newCards = cards.filter { it.sportsCardProId !in existingIds }
        newCards.forEach { localDataSource.insertCard(it) }
    }

    private suspend fun getExistingSportsCardProIds(): Set<String> {
        return localDataSource.getAllCards()
            .mapNotNull { it.sportsCardProId }
            .toSet()
    }

    override suspend fun deleteCards(cardIds: List<String>) {
        localDataSource.deleteCardsByIds(cardIds)
    }

    override suspend fun addCard(card: Card) {
        localDataSource.insertCard(card)
    }

    override suspend fun addCards(cards: List<Card>) {
        cards.forEach { localDataSource.insertCard(it) }
    }

    override suspend fun updateCard(card: Card) {
        localDataSource.insertOrReplaceCard(card)
    }
}
