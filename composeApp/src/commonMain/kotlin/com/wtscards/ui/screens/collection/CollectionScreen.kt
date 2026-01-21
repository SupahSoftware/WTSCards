package com.wtscards.ui.screens.collection

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.wtscards.ui.theme.accentPrimary
import com.wtscards.ui.theme.textSecondary

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
            uiState.items.isEmpty() -> {
                Text(
                    text = "Your collection is empty",
                    style = MaterialTheme.typography.bodyLarge,
                    color = textSecondary
                )
            }
            else -> {
                // TODO: Display collection grid
                Text(
                    text = "Collection: ${uiState.items.size} items",
                    style = MaterialTheme.typography.bodyLarge,
                    color = textSecondary
                )
            }
        }
    }
}
