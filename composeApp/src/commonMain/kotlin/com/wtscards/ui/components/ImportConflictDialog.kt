package com.wtscards.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.wtscards.domain.usecase.ImportStrategy
import com.wtscards.ui.theme.accentPrimary
import com.wtscards.ui.theme.accentSecondary
import com.wtscards.ui.theme.bgPrimary
import com.wtscards.ui.theme.dialogBg
import com.wtscards.ui.theme.textPrimary
import com.wtscards.ui.theme.textSecondary
import com.wtscards.ui.theme.warningColor

@Composable
fun ImportConflictDialog(
    collisionCount: Int,
    totalCount: Int,
    onStrategySelected: (ImportStrategy) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = dialogBg
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Import Conflict Detected",
                    style = MaterialTheme.typography.headlineSmall,
                    color = warningColor
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Found $collisionCount existing cards out of $totalCount total cards.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = textSecondary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "How would you like to proceed?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = textPrimary
                )

                Spacer(modifier = Modifier.height(24.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { onStrategySelected(ImportStrategy.OVERWRITE_ALL) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = warningColor,
                            contentColor = bgPrimary
                        )
                    ) {
                        Text("Overwrite Entire Database")
                    }

                    Button(
                        onClick = { onStrategySelected(ImportStrategy.UPDATE_PRICES_ONLY) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accentPrimary,
                            contentColor = bgPrimary
                        )
                    ) {
                        Text("Update Prices + Add New")
                    }

                    Button(
                        onClick = { onStrategySelected(ImportStrategy.SAFE_IMPORT) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accentSecondary,
                            contentColor = bgPrimary
                        )
                    ) {
                        Text("Safe Import (New Only)")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancel", color = textSecondary)
                    }
                }
            }
        }
    }
}
