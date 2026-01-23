package com.wtscards.domain.usecase

interface AutocompleteUseCase {
    suspend fun getPlayerNameSuggestions(prefix: String): List<String>
    suspend fun getSetNameSuggestions(prefix: String): List<String>
    suspend fun getParallelNameSuggestions(prefix: String): List<String>
    suspend fun addPlayerName(name: String)
    suspend fun addSetName(name: String)
    suspend fun addParallelName(name: String)
}
