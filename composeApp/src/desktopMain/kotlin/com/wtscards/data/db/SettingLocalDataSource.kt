package com.wtscards.data.db

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.wtscards.db.WTSCardsDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SettingLocalDataSource(private val database: WTSCardsDatabase) {

    private val settingQueries = database.settingQueries

    fun getAllSettingsFlow(): Flow<Map<String, String>> {
        return settingQueries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities ->
                entities.associate { it.settingKey to it.settingValue }
            }
    }

    suspend fun getByKey(key: String): String? = withContext(Dispatchers.IO) {
        settingQueries.selectByKey(key).executeAsOneOrNull()
    }

    suspend fun upsert(key: String, value: String) = withContext(Dispatchers.IO) {
        settingQueries.upsert(key, value)
    }
}
