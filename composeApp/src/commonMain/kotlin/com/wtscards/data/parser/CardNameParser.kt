package com.wtscards.data.parser

/**
 * Parses card names to extract player name and parallel name.
 *
 * Card name formats:
 * - "Firstname Lastname [Parallel name] #Card-123"
 * - "Firstname Lastname #Card-123"
 * - "Elly de la Cruz [Red Speckle] #Card-123"
 * - "Josh Beckwith Jr. [Holofoil] #Card-123"
 */
object CardNameParser {

    data class ParsedCardName(
        val playerName: String?,
        val parallelName: String?
    )

    /**
     * Parses a card name and extracts the player name and parallel name.
     *
     * @param cardName The full card name string
     * @return ParsedCardName containing extracted player name and parallel name (if present)
     */
    fun parse(cardName: String): ParsedCardName {
        if (cardName.isBlank()) {
            return ParsedCardName(null, null)
        }

        // Extract parallel name from brackets (if present)
        val parallelRegex = """\[([^\]]+)\]""".toRegex()
        val parallelMatch = parallelRegex.find(cardName)
        val parallelName = parallelMatch?.groupValues?.get(1)?.trim()

        // Remove the parallel name bracket section to isolate player name
        val withoutParallel = if (parallelMatch != null) {
            cardName.replace(parallelMatch.value, "").trim()
        } else {
            cardName
        }

        // Remove card number (starts with #) - everything from # to end of that word
        val cardNumberRegex = """#\S*""".toRegex()
        val withoutCardNumber = withoutParallel.replace(cardNumberRegex, "").trim()

        // The remaining text is the player name
        val playerName = withoutCardNumber.takeIf { it.isNotBlank() }

        return ParsedCardName(
            playerName = playerName,
            parallelName = parallelName
        )
    }
}
