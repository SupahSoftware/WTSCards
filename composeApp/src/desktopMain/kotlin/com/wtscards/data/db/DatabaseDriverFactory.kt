package com.wtscards.data.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.wtscards.db.WTSCardsDatabase
import java.io.File

object DatabaseDriverFactory {
    private const val DATABASE_NAME = "wtscards.db"
    private const val SCHEMA_VERSION = 7 // Increment this to force recreation

    fun createDriver(): SqlDriver {
        val appDataDir = getAppDataDirectory()
        appDataDir.mkdirs()
        val databaseFile = File(appDataDir, DATABASE_NAME)
        val versionFile = File(appDataDir, "schema_version")

        val currentVersion = if (versionFile.exists()) {
            versionFile.readText().toIntOrNull() ?: 0
        } else {
            0
        }

        // If schema version changed, delete old database
        if (databaseFile.exists() && currentVersion < SCHEMA_VERSION) {
            databaseFile.delete()
        }

        val databaseExists = databaseFile.exists()
        val driver = JdbcSqliteDriver("jdbc:sqlite:${databaseFile.absolutePath}")

        if (!databaseExists) {
            WTSCardsDatabase.Schema.create(driver)
            versionFile.writeText(SCHEMA_VERSION.toString())
        }

        return driver
    }

    private fun getAppDataDirectory(): File {
        val os = System.getProperty("os.name").lowercase()
        val userHome = System.getProperty("user.home")

        return when {
            os.contains("win") -> File(System.getenv("APPDATA") ?: "$userHome/AppData/Roaming", "WTSCards")
            os.contains("mac") -> File(userHome, "Library/Application Support/WTSCards")
            else -> File(userHome, ".wtscards")
        }
    }
}
