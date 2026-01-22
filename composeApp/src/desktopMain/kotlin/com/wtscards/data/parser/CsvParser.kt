package com.wtscards.data.parser

import com.wtscards.data.model.Card
import java.io.File

object CsvParser {

    fun parseFile(file: File): Result<List<Card>> {
        return try {
            val lines = file.readLines()
            if (lines.isEmpty()) {
                return Result.failure(IllegalArgumentException("CSV file is empty"))
            }

            val cards = lines.drop(1) // Skip header row
                .filter { it.isNotBlank() }
                .mapNotNull { line -> parseLine(line) }

            Result.success(cards)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseLine(line: String): Card? {
        return try {
            val columns = parseCSVLine(line)
            if (columns.size < 10) return null

            val sportsCardProId = columns[0].trim()
            val nameInfo = columns[1].trim()
            val setName = columns[2].trim()
            val priceInPennies = columns[3].trim().toLongOrNull() ?: 0L
            val gradedString = columns[4].trim()
            val quantity = columns[9].trim().toIntOrNull() ?: 1

            if (sportsCardProId.isBlank()) return null

            Card(
                sportsCardProId = sportsCardProId,
                name = nameInfo,
                setName = setName,
                priceInPennies = priceInPennies,
                gradedString = gradedString,
                quantity = quantity
            )
        } catch (e: Exception) {
            null
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
}
