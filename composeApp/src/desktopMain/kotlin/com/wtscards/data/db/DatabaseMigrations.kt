package com.wtscards.data.db

import app.cash.sqldelight.db.SqlDriver

object DatabaseMigrations {

    fun migrate(driver: SqlDriver, fromVersion: Int, toVersion: Int) {
        var version = fromVersion
        while (version < toVersion) {
            when (version) {
                14 -> migrateFrom14To15(driver)
                15 -> migrateFrom15To16(driver)
            }
            version++
        }
    }

    private fun migrateFrom14To15(driver: SqlDriver) {
        driver.execute(null, "ALTER TABLE ListingEntity ADD COLUMN discount INTEGER NOT NULL DEFAULT 0", 0)
        driver.execute(null, "ALTER TABLE ListingEntity ADD COLUMN nicePrices INTEGER NOT NULL DEFAULT 0", 0)
    }

    private fun migrateFrom15To16(driver: SqlDriver) {
        driver.execute(null, "ALTER TABLE ListingEntity ADD COLUMN imageUrl TEXT", 0)
    }
}
