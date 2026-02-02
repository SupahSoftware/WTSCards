package com.wtscards.domain.usecase

import com.wtscards.data.model.Listing
import kotlinx.coroutines.flow.Flow

interface ListingUseCase {
    fun getAllListingsFlow(): Flow<List<Listing>>
    suspend fun getAllListings(): List<Listing>
    suspend fun getListingById(id: String): Listing?
    suspend fun createListing(listing: Listing)
    suspend fun updateListing(listingId: String, title: String, discount: Int, nicePrices: Boolean)
    suspend fun updateImageUrl(listingId: String, imageUrl: String?)
    suspend fun deleteListing(id: String)
    suspend fun addCardsToListing(listingId: String, cardIds: List<String>)
    suspend fun removeCardFromListing(listingId: String, cardId: String)
    suspend fun updateLotPriceOverride(listingId: String, lotPriceOverride: Long?)
}
