package com.wtscards.data.db

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.wtscards.data.model.Card
import com.wtscards.data.model.Listing
import com.wtscards.db.CardEntity
import com.wtscards.db.ListingEntity
import com.wtscards.db.WTSCardsDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ListingLocalDataSource(private val database: WTSCardsDatabase) {

    private val listingQueries = database.listingQueries
    private val cardQueries = database.cardQueries

    fun getAllListingsFlow(): Flow<List<Listing>> {
        return kotlinx.coroutines.flow.combine(
            listingQueries.selectAll().asFlow().mapToList(Dispatchers.IO),
            listingQueries.selectAllListingCards().asFlow().mapToList(Dispatchers.IO),
            cardQueries.selectAll().asFlow().mapToList(Dispatchers.IO)
        ) { listingEntities, _, _ ->
            listingEntities.map { listingEntity ->
                listingEntity.toListing(getCardsForListing(listingEntity.id))
            }
        }
    }

    suspend fun getAllListings(): List<Listing> = withContext(Dispatchers.IO) {
        listingQueries.selectAll().executeAsList().map { listingEntity ->
            listingEntity.toListing(getCardsForListing(listingEntity.id))
        }
    }

    suspend fun getListingById(id: String): Listing? = withContext(Dispatchers.IO) {
        listingQueries.selectById(id).executeAsOneOrNull()?.let { listingEntity ->
            listingEntity.toListing(getCardsForListing(listingEntity.id))
        }
    }

    suspend fun insertListing(listing: Listing) = withContext(Dispatchers.IO) {
        database.transaction {
            insertListingEntity(listing)
            insertListingCardRelationships(listing)
        }
    }

    private fun insertListingEntity(listing: Listing) {
        listingQueries.insert(
            id = listing.id,
            title = listing.title,
            createdAt = listing.createdAt,
            discount = listing.discount.toLong(),
            nicePrices = if (listing.nicePrices) 1L else 0L,
            imageUrl = listing.imageUrl,
            lotPriceOverride = listing.lotPriceOverride
        )
    }

    private fun insertListingCardRelationships(listing: Listing) {
        listing.cards.forEach { card ->
            listingQueries.insertListingCard(
                listingId = listing.id,
                cardId = card.id
            )
        }
    }

    suspend fun updateListing(listingId: String, title: String, discount: Int, nicePrices: Boolean) = withContext(Dispatchers.IO) {
        listingQueries.updateListing(
            title = title,
            discount = discount.toLong(),
            nicePrices = if (nicePrices) 1L else 0L,
            id = listingId
        )
    }

    suspend fun updateImageUrl(listingId: String, imageUrl: String?) = withContext(Dispatchers.IO) {
        listingQueries.updateImageUrl(imageUrl = imageUrl, id = listingId)
    }

    suspend fun updateLotPriceOverride(listingId: String, lotPriceOverride: Long?) = withContext(Dispatchers.IO) {
        listingQueries.updateLotPriceOverride(lotPriceOverride = lotPriceOverride, id = listingId)
    }

    suspend fun deleteListing(id: String) = withContext(Dispatchers.IO) {
        database.transaction {
            listingQueries.deleteListingCardsByListingId(id)
            listingQueries.deleteById(id)
        }
    }

    suspend fun addCardsToListing(listingId: String, cardIds: List<String>) = withContext(Dispatchers.IO) {
        database.transaction {
            cardIds.forEach { cardId ->
                listingQueries.insertListingCard(
                    listingId = listingId,
                    cardId = cardId
                )
            }
        }
    }

    suspend fun removeCardFromListing(listingId: String, cardId: String) = withContext(Dispatchers.IO) {
        listingQueries.deleteListingCard(listingId, cardId)
    }

    private suspend fun getCardsForListing(listingId: String): List<Card> {
        return listingQueries.selectCardsByListingId(listingId)
            .executeAsList()
            .map { it.toCard() }
            .sortedByDescending { it.priceInPennies }
    }

    private fun ListingEntity.toListing(cards: List<Card>): Listing {
        return Listing(
            id = id,
            title = title,
            createdAt = createdAt,
            cards = cards,
            discount = discount.toInt(),
            nicePrices = nicePrices != 0L,
            imageUrl = imageUrl,
            lotPriceOverride = lotPriceOverride
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
