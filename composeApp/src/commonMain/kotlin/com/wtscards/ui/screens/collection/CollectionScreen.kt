package com.wtscards.ui.screens.collection

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.wtscards.data.model.Card
import com.wtscards.ui.theme.accentPrimary
import com.wtscards.ui.theme.bgSurface
import com.wtscards.ui.theme.successColor
import com.wtscards.ui.theme.textPrimary
import com.wtscards.ui.theme.textSecondary
import com.wtscards.ui.theme.textTertiary

@Composable
fun CollectionScreen(
    uiState: CollectionUiState,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(color = accentPrimary)
            }
            uiState.error != null -> {
                Text(
                    text = uiState.error,
                    color = MaterialTheme.colorScheme.error
                )
            }
            uiState.cards.isEmpty() -> {
                Text(
                    text = "Your collection is empty",
                    style = MaterialTheme.typography.bodyLarge,
                    color = textSecondary
                )
            }
            else -> {
                CardList(cards = uiState.cards)
            }
        }
    }
}

@Composable
private fun CardList(cards: List<Card>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(cards, key = { it.sportsCardProId }) { card ->
            CardRow(card = card)
        }
    }
}

@Composable
private fun CardRow(card: Card) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(bgSurface)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = card.name,
                style = MaterialTheme.typography.bodyLarge,
                color = textPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = card.setName,
                style = MaterialTheme.typography.bodySmall,
                color = textSecondary
            )
            if (card.gradedString.isNotBlank() && card.gradedString.lowercase() != "ungraded") {
                Text(
                    text = card.gradedString,
                    style = MaterialTheme.typography.bodySmall,
                    color = accentPrimary
                )
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = formatPrice(card.priceInPennies),
                style = MaterialTheme.typography.bodyLarge,
                color = successColor
            )
            if (card.quantity > 1) {
                Text(
                    text = "x${card.quantity}",
                    style = MaterialTheme.typography.bodySmall,
                    color = textTertiary
                )
            }
        }
    }
}

private fun formatPrice(priceInPennies: Long): String {
    val dollars = priceInPennies / 100.0
    return "$${String.format("%.2f", dollars)}"
}
