package com.wtscards.data.repository

interface CollectionRepository {
    // TODO: Define collection data operations
    suspend fun getCollectionItems(): List<Any>
}

class CollectionRepositoryImpl : CollectionRepository {
    override suspend fun getCollectionItems(): List<Any> {
        // TODO: Implement actual data fetching
        return emptyList()
    }
}
