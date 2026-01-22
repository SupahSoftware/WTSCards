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
        val incomingIds = cards.map { it.sportsCardProId }
        return localDataSource.getCardsByIds(incomingIds)
    }

    override suspend fun importCards(cards: List<Card>, strategy: ImportStrategy) {
        when (strategy) {
            ImportStrategy.OVERWRITE_ALL -> {
                localDataSource.deleteAllCards()
                localDataSource.insertOrReplaceCards(cards)
            }
            ImportStrategy.UPDATE_PRICES_ONLY -> {
                val existingIds = localDataSource.getAllCards().map { it.sportsCardProId }.toSet()
                cards.forEach { card ->
                    if (card.sportsCardProId in existingIds) {
                        localDataSource.updatePrice(card.sportsCardProId, card.priceInPennies)
                    } else {
                        localDataSource.insertCard(card)
                    }
                }
            }
            ImportStrategy.SAFE_IMPORT -> {
                val existingIds = localDataSource.getAllCards().map { it.sportsCardProId }.toSet()
                val newCards = cards.filter { it.sportsCardProId !in existingIds }
                newCards.forEach { localDataSource.insertCard(it) }
            }
        }
    }
}
