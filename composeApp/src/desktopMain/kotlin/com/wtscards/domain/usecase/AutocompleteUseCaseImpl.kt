package com.wtscards.domain.usecase

import com.wtscards.data.db.AutocompleteLocalDataSource

class AutocompleteUseCaseImpl(
    private val localDataSource: AutocompleteLocalDataSource
) : AutocompleteUseCase {

    override suspend fun getPlayerNameSuggestions(prefix: String): List<String> {
        if (prefix.isBlank()) return emptyList()
        return localDataSource.getPlayerNamesByPrefix(prefix)
    }

    override suspend fun getSetNameSuggestions(prefix: String): List<String> {
        if (prefix.isBlank()) return emptyList()
        return localDataSource.getSetNamesByPrefix(prefix)
    }

    override suspend fun getParallelNameSuggestions(prefix: String): List<String> {
        if (prefix.isBlank()) return emptyList()
        return localDataSource.getParallelNamesByPrefix(prefix)
    }

    override suspend fun addPlayerName(name: String) {
        if (name.isNotBlank()) {
            localDataSource.insertPlayerNames(setOf(name))
        }
    }

    override suspend fun addSetName(name: String) {
        if (name.isNotBlank()) {
            localDataSource.insertSetNames(setOf(name))
        }
    }

    override suspend fun addParallelName(name: String) {
        if (name.isNotBlank()) {
            localDataSource.insertParallelNames(setOf(name))
        }
    }
}
