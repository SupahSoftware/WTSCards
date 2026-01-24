package com.wtscards.domain.usecase

import com.wtscards.data.db.ListingLocalDataSource
import com.wtscards.data.model.Listing
import kotlinx.coroutines.flow.Flow

class ListingUseCaseImpl(
    private val listingLocalDataSource: ListingLocalDataSource
) : ListingUseCase {

    override fun getAllListingsFlow(): Flow<List<Listing>> {
        return listingLocalDataSource.getAllListingsFlow()
    }

    override suspend fun getAllListings(): List<Listing> {
        return listingLocalDataSource.getAllListings()
    }

    override suspend fun getListingById(id: String): Listing? {
        return listingLocalDataSource.getListingById(id)
    }

    override suspend fun createListing(listing: Listing) {
        listingLocalDataSource.insertListing(listing)
    }

    override suspend fun updateTitle(listingId: String, title: String) {
        listingLocalDataSource.updateTitle(listingId, title)
    }

    override suspend fun deleteListing(id: String) {
        listingLocalDataSource.deleteListing(id)
    }

    override suspend fun addCardsToListing(listingId: String, cardIds: List<String>) {
        listingLocalDataSource.addCardsToListing(listingId, cardIds)
    }

    override suspend fun removeCardFromListing(listingId: String, cardId: String) {
        listingLocalDataSource.removeCardFromListing(listingId, cardId)
    }
}
