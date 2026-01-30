package com.wtscards.domain.usecase

import com.wtscards.db.WTSCardsDatabase
import com.wtscards.domain.model.BackupInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Serializable
data class DatabaseBackup(
    val version: Int = 1,
    val createdAt: String,
    val cards: List<BackupCard>,
    val orders: List<BackupOrder>,
    val orderCards: List<BackupOrderCard>,
    val listings: List<BackupListing>,
    val listingCards: List<BackupListingCard>,
    val settings: List<BackupSetting>,
    val playerNames: List<String>,
    val setNames: List<String>,
    val parallelNames: List<String>
)

@Serializable
data class BackupCard(
    val id: String,
    val sportsCardProId: String? = null,
    val name: String,
    val setName: String,
    val priceInPennies: Long,
    val gradedString: String,
    val priceSold: Long? = null
)

@Serializable
data class BackupOrder(
    val id: String,
    val name: String,
    val streetAddress: String,
    val city: String,
    val state: String,
    val zipcode: String,
    val shippingType: String? = null,
    val shippingCost: Long,
    val status: String,
    val createdAt: Long,
    val trackingNumber: String? = null,
    val discount: Long,
    val length: Double = 0.0,
    val width: Double = 0.0,
    val height: Double = 0.0,
    val pounds: Long = 0,
    val ounces: Long = 0
)

@Serializable
data class BackupOrderCard(
    val orderId: String,
    val cardId: String
)

@Serializable
data class BackupListing(
    val id: String,
    val title: String,
    val createdAt: Long,
    val discount: Long = 0,
    val nicePrices: Long = 0
)

@Serializable
data class BackupListingCard(
    val listingId: String,
    val cardId: String
)

@Serializable
data class BackupSetting(
    val key: String,
    val value: String
)

class BackupUseCaseImpl(
    private val database: WTSCardsDatabase,
    private val backupDir: File
) : BackupUseCase {

    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
    }

    private val fileTimestampFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")
    private val displayFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a")
    private val backupFilePattern = Regex("""wtscards-backup-(\d{4}-\d{2}-\d{2}_\d{2}-\d{2}-\d{2})\.json""")

    override suspend fun createBackup(): Result<BackupInfo> = withContext(Dispatchers.IO) {
        runCatching {
            backupDir.mkdirs()

            val now = LocalDateTime.now()
            val timestamp = now.format(fileTimestampFormatter)
            val fileName = "wtscards-backup-$timestamp.json"
            val backupFile = File(backupDir, fileName)

            val backup = exportDatabase(now)
            backupFile.writeText(json.encodeToString(backup))

            pruneOldBackups()

            BackupInfo(
                fileName = fileName,
                displayDate = now.format(displayFormatter)
            )
        }
    }

    override suspend fun createBackupIfNeeded() {
        withContext(Dispatchers.IO) {
            val backups = getBackupFiles()
            if (backups.isEmpty()) {
                createBackup()
                return@withContext
            }

            val newestFile = backups.first()
            val match = backupFilePattern.matchEntire(newestFile.name) ?: run {
                createBackup()
                return@withContext
            }

            val backupTime = LocalDateTime.parse(match.groupValues[1], fileTimestampFormatter)
            val hoursSinceBackup = ChronoUnit.HOURS.between(backupTime, LocalDateTime.now())

            if (hoursSinceBackup >= 24) {
                createBackup()
            }
        }
    }

    override suspend fun getAvailableBackups(): List<BackupInfo> = withContext(Dispatchers.IO) {
        getBackupFiles().mapNotNull { file ->
            val match = backupFilePattern.matchEntire(file.name) ?: return@mapNotNull null
            val backupTime = LocalDateTime.parse(match.groupValues[1], fileTimestampFormatter)
            BackupInfo(
                fileName = file.name,
                displayDate = backupTime.format(displayFormatter)
            )
        }
    }

    override suspend fun getLastBackupDisplayDate(): String? = withContext(Dispatchers.IO) {
        val newestFile = getBackupFiles().firstOrNull() ?: return@withContext null
        val match = backupFilePattern.matchEntire(newestFile.name) ?: return@withContext null
        val backupTime = LocalDateTime.parse(match.groupValues[1], fileTimestampFormatter)
        backupTime.format(displayFormatter)
    }

    override suspend fun restoreFromBackup(fileName: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val backupFile = File(backupDir, fileName)
            if (!backupFile.exists()) {
                throw IllegalArgumentException("Backup file not found: $fileName")
            }

            val backup = json.decodeFromString<DatabaseBackup>(backupFile.readText())
            importDatabase(backup)
        }
    }

    private fun exportDatabase(now: LocalDateTime): DatabaseBackup {
        val cards = database.cardQueries.selectAll().executeAsList().map { card ->
            BackupCard(
                id = card.id,
                sportsCardProId = card.sportsCardProId,
                name = card.name,
                setName = card.setName,
                priceInPennies = card.priceInPennies,
                gradedString = card.gradedString,
                priceSold = card.priceSold
            )
        }

        val orders = database.orderQueries.selectAll().executeAsList().map { order ->
            BackupOrder(
                id = order.id,
                name = order.name,
                streetAddress = order.streetAddress,
                city = order.city,
                state = order.state,
                zipcode = order.zipcode,
                shippingType = order.shippingType,
                shippingCost = order.shippingCost,
                status = order.status,
                createdAt = order.createdAt,
                trackingNumber = order.trackingNumber,
                discount = order.discount,
                length = order.length,
                width = order.width,
                height = order.height,
                pounds = order.pounds,
                ounces = order.ounces
            )
        }

        val orderCards = database.orderQueries.selectAllOrderCards().executeAsList().map { oc ->
            BackupOrderCard(orderId = oc.orderId, cardId = oc.cardId)
        }

        val listings = database.listingQueries.selectAll().executeAsList().map { listing ->
            BackupListing(
                id = listing.id,
                title = listing.title,
                createdAt = listing.createdAt,
                discount = listing.discount,
                nicePrices = listing.nicePrices
            )
        }

        val listingCards = database.listingQueries.selectAllListingCards().executeAsList().map { lc ->
            BackupListingCard(listingId = lc.listingId, cardId = lc.cardId)
        }

        val settings = database.settingQueries.selectAll().executeAsList().map { setting ->
            BackupSetting(key = setting.settingKey, value = setting.settingValue)
        }

        val playerNames = database.autocompleteQueries.selectAllPlayerNames().executeAsList()
        val setNames = database.autocompleteQueries.selectAllSetNames().executeAsList()
        val parallelNames = database.autocompleteQueries.selectAllParallelNames().executeAsList()

        return DatabaseBackup(
            createdAt = now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            cards = cards,
            orders = orders,
            orderCards = orderCards,
            listings = listings,
            listingCards = listingCards,
            settings = settings,
            playerNames = playerNames,
            setNames = setNames,
            parallelNames = parallelNames
        )
    }

    private fun importDatabase(backup: DatabaseBackup) {
        database.transaction {
            // Clear all junction tables first (before parent tables due to foreign keys)
            database.orderQueries.deleteAllOrderCards()
            database.listingQueries.deleteAllListingCards()

            // Clear parent tables
            database.orderQueries.deleteAllOrders()
            database.listingQueries.deleteAllListings()
            database.cardQueries.deleteAll()
            database.settingQueries.deleteAllSettings()
            database.autocompleteQueries.deleteAllPlayerNames()
            database.autocompleteQueries.deleteAllSetNames()
            database.autocompleteQueries.deleteAllParallelNames()

            // Restore cards first (referenced by orders and listings)
            backup.cards.forEach { card ->
                database.cardQueries.insert(
                    id = card.id,
                    sportsCardProId = card.sportsCardProId,
                    name = card.name,
                    setName = card.setName,
                    priceInPennies = card.priceInPennies,
                    gradedString = card.gradedString,
                    priceSold = card.priceSold
                )
            }

            // Restore orders
            backup.orders.forEach { order ->
                database.orderQueries.insert(
                    id = order.id,
                    name = order.name,
                    streetAddress = order.streetAddress,
                    city = order.city,
                    state = order.state,
                    zipcode = order.zipcode,
                    shippingType = order.shippingType,
                    shippingCost = order.shippingCost,
                    status = order.status,
                    createdAt = order.createdAt,
                    trackingNumber = order.trackingNumber,
                    discount = order.discount,
                    length = order.length,
                    width = order.width,
                    height = order.height,
                    pounds = order.pounds,
                    ounces = order.ounces
                )
            }

            // Restore order-card associations
            backup.orderCards.forEach { oc ->
                database.orderQueries.insertOrderCard(
                    orderId = oc.orderId,
                    cardId = oc.cardId
                )
            }

            // Restore listings
            backup.listings.forEach { listing ->
                database.listingQueries.insert(
                    id = listing.id,
                    title = listing.title,
                    createdAt = listing.createdAt,
                    discount = listing.discount,
                    nicePrices = listing.nicePrices
                )
            }

            // Restore listing-card associations
            backup.listingCards.forEach { lc ->
                database.listingQueries.insertListingCard(
                    listingId = lc.listingId,
                    cardId = lc.cardId
                )
            }

            // Restore settings
            backup.settings.forEach { setting ->
                database.settingQueries.upsert(
                    settingKey = setting.key,
                    settingValue = setting.value
                )
            }

            // Restore autocomplete data
            backup.playerNames.forEach { name ->
                database.autocompleteQueries.insertPlayerName(name)
            }
            backup.setNames.forEach { name ->
                database.autocompleteQueries.insertSetName(name)
            }
            backup.parallelNames.forEach { name ->
                database.autocompleteQueries.insertParallelName(name)
            }
        }
    }

    private fun getBackupFiles(): List<File> {
        if (!backupDir.exists()) return emptyList()
        return backupDir.listFiles()
            ?.filter { it.isFile && backupFilePattern.matches(it.name) }
            ?.sortedByDescending { it.name }
            ?: emptyList()
    }

    private fun pruneOldBackups() {
        val backups = getBackupFiles()
        if (backups.size > MAX_BACKUPS) {
            backups.drop(MAX_BACKUPS).forEach { it.delete() }
        }
    }

    companion object {
        private const val MAX_BACKUPS = 10
    }
}
