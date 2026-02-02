package com.wtscards.data.db

import app.cash.sqldelight.db.SqlDriver

object DatabaseMigrations {

    fun migrate(driver: SqlDriver, fromVersion: Int, toVersion: Int) {
        var version = fromVersion
        while (version < toVersion) {
            when (version) {
                14 -> migrateFrom14To15(driver)
                15 -> migrateFrom15To16(driver)
                16 -> migrateFrom16To17(driver)
                17 -> migrateFrom17To18(driver)
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

    private fun migrateFrom16To17(driver: SqlDriver) {
        driver.execute(null, "ALTER TABLE OrderEntity ADD COLUMN totalOverride INTEGER", 0)
    }

    private fun migrateFrom17To18(driver: SqlDriver) {
        driver.execute(null, "ALTER TABLE ListingEntity ADD COLUMN lotPriceOverride INTEGER", 0)
    }
}
