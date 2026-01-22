package com.wtscards.ui.screens.import

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wtscards.ui.theme.accentPrimary
import com.wtscards.ui.theme.bgHover
import com.wtscards.ui.theme.bgSurface
import com.wtscards.ui.theme.borderDivider
import com.wtscards.ui.theme.successColor
import com.wtscards.ui.theme.textOnAccent
import com.wtscards.ui.theme.textPrimary
import com.wtscards.ui.theme.textSecondary
import com.wtscards.ui.theme.textTertiary

@Composable
fun ImportScreen(
    uiState: ImportUiState,
    onBrowseFiles: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        when (val state = uiState.importState) {
            is ImportState.Idle -> IdleContent(onBrowseFiles = onBrowseFiles)
            is ImportState.Hovering -> HoveringContent(state.fileName)
            is ImportState.Parsing -> LoadingContent("Parsing CSV...")
            is ImportState.ConflictDetected -> Unit // Dialog handled externally
            is ImportState.Importing -> LoadingContent("Importing cards...")
            is ImportState.Success -> SuccessContent(state.count)
            is ImportState.Error -> ErrorContent(state.message)
        }
    }
}

@Composable
private fun IdleContent(onBrowseFiles: () -> Unit) {
    DropZoneBox(isHovering = false) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = textSecondary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Drag and drop your SportsCardPro\n.csv data to import",
                style = MaterialTheme.typography.bodyLarge,
                color = textSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "or",
                style = MaterialTheme.typography.bodyMedium,
                color = textTertiary
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onBrowseFiles,
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentPrimary,
                    contentColor = textOnAccent
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FolderOpen,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text("Browse files")
            }
        }
    }
}

@Composable
private fun HoveringContent(fileName: String) {
    DropZoneBox(isHovering = true) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = accentPrimary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Upload $fileName",
                style = MaterialTheme.typography.bodyLarge,
                color = textPrimary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun LoadingContent(message: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(color = accentPrimary)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = textSecondary
        )
    }
}

@Composable
private fun SuccessContent(count: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Successfully imported $count cards!",
            style = MaterialTheme.typography.headlineSmall,
            color = successColor,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ErrorContent(message: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Import Error",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = textSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun DropZoneBox(
    isHovering: Boolean,
    content: @Composable () -> Unit
) {
    val backgroundColor = if (isHovering) bgHover else bgSurface
    val borderColor = if (isHovering) accentPrimary else borderDivider

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(
                width = 2.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
