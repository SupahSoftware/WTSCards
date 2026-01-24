package com.wtscards.data.parser

object CardNameParser {

    data class ParsedCardName(
        val playerName: String?,
        val parallelName: String?
    )

    fun parse(cardName: String): ParsedCardName {
        if (cardName.isBlank()) {
            return ParsedCardName(null, null)
        }

        val parallelName = extractParallelName(cardName)
        val withoutParallel = removeParallelSection(cardName)
        val withoutCardNumber = removeCardNumber(withoutParallel)
        val playerName = extractPlayerName(withoutCardNumber)

        return ParsedCardName(
            playerName = playerName,
            parallelName = parallelName
        )
    }

    private fun extractParallelName(cardName: String): String? {
        val parallelRegex = """\[([^\]]+)\]""".toRegex()
        val parallelMatch = parallelRegex.find(cardName)
        return parallelMatch?.groupValues?.get(1)?.trim()
    }

    private fun removeParallelSection(cardName: String): String {
        val parallelRegex = """\[([^\]]+)\]""".toRegex()
        val parallelMatch = parallelRegex.find(cardName)
        return if (parallelMatch != null) {
            cardName.replace(parallelMatch.value, "").trim()
        } else {
            cardName
        }
    }

    private fun removeCardNumber(text: String): String {
        val cardNumberRegex = """#\S*""".toRegex()
        return text.replace(cardNumberRegex, "").trim()
    }

    private fun extractPlayerName(text: String): String? {
        return text.takeIf { it.isNotBlank() }
    }
}
