package com.wtscards.data.parser

import com.wtscards.data.model.Card
import java.io.File
import java.util.UUID

object CsvParser {

    fun parseFile(file: File): Result<List<Card>> {
        return try {
            val lines = file.readLines()
            if (lines.isEmpty()) {
                return Result.failure(IllegalArgumentException("CSV file is empty"))
            }

            val cards = parseAllLines(lines)
            Result.success(cards)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseAllLines(lines: List<String>): List<Card> {
        return lines.drop(1)
            .filter { it.isNotBlank() }
            .flatMap { line -> parseLine(line) }
    }

    private fun parseLine(line: String): List<Card> {
        return try {
            val columns = parseCSVLine(line)
            if (columns.size < 10) return emptyList()

            val cardData = extractCardData(columns)
            if (cardData == null) return emptyList()

            createCardInstances(cardData)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun extractCardData(columns: List<String>): CardData? {
        val sportsCardProId = columns[0].trim()
        if (sportsCardProId.isBlank()) return null

        return CardData(
            sportsCardProId = sportsCardProId,
            nameInfo = columns[1].trim(),
            setName = columns[2].trim(),
            priceInPennies = columns[3].trim().toLongOrNull() ?: 0L,
            gradedString = columns[4].trim(),
            quantity = columns[9].trim().toIntOrNull() ?: 1
        )
    }

    private fun createCardInstances(cardData: CardData): List<Card> {
        return List(cardData.quantity) {
            Card(
                id = UUID.randomUUID().toString(),
                sportsCardProId = cardData.sportsCardProId,
                name = cardData.nameInfo,
                setName = cardData.setName,
                priceInPennies = cardData.priceInPennies,
                gradedString = cardData.gradedString,
                priceSold = null
            )
        }
    }

    private fun parseCSVLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false

        for (char in line) {
            when {
                char == '"' -> inQuotes = !inQuotes
                char == ',' && !inQuotes -> {
                    result.add(current.toString())
                    current.clear()
                }
                else -> current.append(char)
            }
        }
        result.add(current.toString())

        return result
    }

    private data class CardData(
        val sportsCardProId: String,
        val nameInfo: String,
        val setName: String,
        val priceInPennies: Long,
        val gradedString: String,
        val quantity: Int
    )
}
