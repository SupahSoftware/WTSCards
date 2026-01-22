package com.wtscards.domain.usecase

import com.wtscards.data.model.Card
import kotlinx.coroutines.flow.Flow

interface CardUseCase {
    fun getAllCardsFlow(): Flow<List<Card>>
    suspend fun getAllCards(): List<Card>
    suspend fun findCollisions(cards: List<Card>): List<Card>
    suspend fun importCards(cards: List<Card>, strategy: ImportStrategy)
    suspend fun deleteCards(cardIds: List<String>)
    suspend fun addCard(card: Card)
    suspend fun addCards(cards: List<Card>)
}
