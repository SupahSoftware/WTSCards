package com.wtscards.data.db

import com.wtscards.db.WTSCardsDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AutocompleteLocalDataSource(private val database: WTSCardsDatabase) {

    private val queries = database.autocompleteQueries

    // Player Names
    suspend fun getPlayerNamesByPrefix(prefix: String): List<String> = withContext(Dispatchers.IO) {
        queries.selectPlayerNamesByPrefix(prefix).executeAsList()
    }

    suspend fun insertPlayerNames(names: Set<String>) = withContext(Dispatchers.IO) {
        queries.transaction {
            names.forEach { name ->
                queries.insertPlayerName(name)
            }
        }
    }

    // Set Names
    suspend fun getSetNamesByPrefix(prefix: String): List<String> = withContext(Dispatchers.IO) {
        queries.selectSetNamesByPrefix(prefix).executeAsList()
    }

    suspend fun insertSetNames(names: Set<String>) = withContext(Dispatchers.IO) {
        queries.transaction {
            names.forEach { name ->
                queries.insertSetName(name)
            }
        }
    }

    // Parallel Names
    suspend fun getParallelNamesByPrefix(prefix: String): List<String> = withContext(Dispatchers.IO) {
        queries.selectParallelNamesByPrefix(prefix).executeAsList()
    }

    suspend fun insertParallelNames(names: Set<String>) = withContext(Dispatchers.IO) {
        queries.transaction {
            names.forEach { name ->
                queries.insertParallelName(name)
            }
        }
    }
}
