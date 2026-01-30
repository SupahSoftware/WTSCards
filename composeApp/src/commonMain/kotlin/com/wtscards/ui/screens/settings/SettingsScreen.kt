package com.wtscards.ui.screens.settings

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.defaultScrollbarStyle
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wtscards.domain.model.BackupInfo
import com.wtscards.ui.components.ScrollableList
import com.wtscards.ui.theme.accentPrimary
import com.wtscards.ui.theme.bgSecondary
import com.wtscards.ui.theme.bgSurface
import com.wtscards.ui.theme.errorColor
import com.wtscards.ui.theme.successColor
import com.wtscards.ui.theme.textOnAccent
import com.wtscards.ui.theme.textPrimary
import com.wtscards.ui.theme.textSecondary
import com.wtscards.ui.theme.textTertiary
import kotlinx.coroutines.delay

@Composable
fun SettingsScreen(
        uiState: SettingsUiState,
        onPreBodyTextChanged: (String) -> Unit,
        onPostBodyTextChanged: (String) -> Unit,
        onListingNicePricesEnabledChanged: (Boolean) -> Unit,
        onListingDefaultDiscountChanged: (String) -> Unit,
        onFreeShippingEnabledChanged: (Boolean) -> Unit,
        onFreeShippingThresholdChanged: (String) -> Unit,
        onNicePricesEnabledChanged: (Boolean) -> Unit,
        onDefaultDiscountChanged: (String) -> Unit,
        onEnvelopeCostChanged: (String) -> Unit,
        onEnvelopeLengthChanged: (String) -> Unit,
        onEnvelopeWidthChanged: (String) -> Unit,
        onBubbleMailerCostChanged: (String) -> Unit,
        onBubbleMailerLengthChanged: (String) -> Unit,
        onBubbleMailerWidthChanged: (String) -> Unit,
        onBoxCostChanged: (String) -> Unit,
        onBoxLengthChanged: (String) -> Unit,
        onBoxWidthChanged: (String) -> Unit,
        onBoxHeightChanged: (String) -> Unit,
        onSave: () -> Unit,
        onClearToast: () -> Unit,
        onBackupNow: () -> Unit,
        onShowRestoreDialog: () -> Unit,
        onDismissRestoreDialog: () -> Unit,
        onSelectBackupToRestore: (BackupInfo) -> Unit,
        onConfirmRestore: () -> Unit,
        onDismissRestoreConfirmation: () -> Unit,
        modifier: Modifier = Modifier
) {
        Box(modifier = modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                                Text(
                                        text = "Settings",
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = textPrimary,
                                        modifier = Modifier.weight(1f)
                                )

                                Button(
                                        onClick = onSave,
                                        enabled = !uiState.isSaving,
                                        colors =
                                                ButtonDefaults.buttonColors(
                                                        containerColor = accentPrimary,
                                                        contentColor = textOnAccent,
                                                        disabledContainerColor = bgSurface,
                                                        disabledContentColor = textTertiary
                                                ),
                                        shape = RoundedCornerShape(8.dp)
                                ) { Text(if (uiState.isSaving) "Saving..." else "Save") }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        val scrollState = rememberScrollState()

                        Box(modifier = Modifier.fillMaxSize()) {
                                Column(
                                        modifier =
                                                Modifier.fillMaxSize()
                                                        .padding(end = 24.dp)
                                                        .verticalScroll(scrollState)
                                ) {
                                        Text(
                                                text = "Listings",
                                                style = MaterialTheme.typography.headlineSmall,
                                                color = accentPrimary
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))

                                        SettingTextField(
                                                label = "Default pre-body text",
                                                description =
                                                        "This text will be automatically inserted before your listed cards when you click copy body",
                                                value = uiState.preBodyText,
                                                onValueChange = onPreBodyTextChanged
                                        )

                                        Spacer(modifier = Modifier.height(20.dp))

                                        SettingTextField(
                                                label = "Default post-body text",
                                                description =
                                                        "This text will be automatically inserted after your listed cards when you click copy body",
                                                value = uiState.postBodyText,
                                                onValueChange = onPostBodyTextChanged
                                        )

                                        Spacer(modifier = Modifier.height(20.dp))

                                        SettingToggle(
                                                label = "Nice prices",
                                                description =
                                                        "Round card prices up to the nearest dollar on new listings",
                                                checked = uiState.listingNicePricesEnabled,
                                                onCheckedChange = onListingNicePricesEnabledChanged
                                        )

                                        Spacer(modifier = Modifier.height(20.dp))

                                        SettingSingleLineTextField(
                                                label = "Default discount percentage",
                                                description =
                                                        "Default discount to apply to new listings. Enter as a whole number (e.g. 5 for 5%)",
                                                value = uiState.listingDefaultDiscount,
                                                onValueChange = onListingDefaultDiscountChanged,
                                                placeholder = "0",
                                                suffix = "%"
                                        )

                                        Spacer(modifier = Modifier.height(24.dp))

                                        HorizontalDivider(
                                                color = bgSecondary,
                                                modifier = Modifier.padding(horizontal = 16.dp)
                                        )

                                        Spacer(modifier = Modifier.height(24.dp))

                                        Text(
                                                text = "Orders",
                                                style = MaterialTheme.typography.headlineSmall,
                                                color = accentPrimary
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))

                                        SettingToggle(
                                                label =
                                                        "Free shipping on orders over a certain amount",
                                                description =
                                                        "When enabled, shipping cost will not be added to the order total if the card subtotal meets the threshold",
                                                checked = uiState.freeShippingEnabled,
                                                onCheckedChange = onFreeShippingEnabledChanged
                                        )

                                        if (uiState.freeShippingEnabled) {
                                                Spacer(modifier = Modifier.height(8.dp))
                                                SettingSingleLineTextField(
                                                        label = "Free shipping threshold",
                                                        value = uiState.freeShippingThreshold,
                                                        onValueChange =
                                                                onFreeShippingThresholdChanged,
                                                        placeholder = "0.00",
                                                        prefix = "$"
                                                )
                                        }

                                        Spacer(modifier = Modifier.height(20.dp))

                                        SettingToggle(
                                                label = "Nice prices",
                                                description =
                                                        "Round card prices up to the nearest dollar on the orders screen",
                                                checked = uiState.nicePricesEnabled,
                                                onCheckedChange = onNicePricesEnabledChanged
                                        )

                                        Spacer(modifier = Modifier.height(20.dp))

                                        SettingSingleLineTextField(
                                                label = "Default discount percentage",
                                                description =
                                                        "Default discount to apply to new orders. Enter as a whole number (e.g. 5 for 5%)",
                                                value = uiState.defaultDiscount,
                                                onValueChange = onDefaultDiscountChanged,
                                                placeholder = "0",
                                                suffix = "%"
                                        )

                                        Spacer(modifier = Modifier.height(24.dp))

                                        HorizontalDivider(
                                                color = bgSecondary,
                                                modifier = Modifier.padding(horizontal = 16.dp)
                                        )

                                        Spacer(modifier = Modifier.height(24.dp))

                                        Text(
                                                text = "Shipping",
                                                style = MaterialTheme.typography.headlineSmall,
                                                color = accentPrimary
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                                text =
                                                        "Default costs and dimensions for each package type. These will be pre-filled when creating orders.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = textSecondary
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))

                                        Text(
                                                text = "Envelope",
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = textPrimary
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                                SettingSingleLineTextField(
                                                        label = "Default Cost",
                                                        value = uiState.envelopeCost,
                                                        onValueChange = onEnvelopeCostChanged,
                                                        placeholder = "1.00",
                                                        prefix = "$"
                                                )
                                                SettingSingleLineTextField(
                                                        label = "Length",
                                                        value = uiState.envelopeLength,
                                                        onValueChange = onEnvelopeLengthChanged,
                                                        placeholder = "3.5",
                                                        suffix = "in"
                                                )
                                                SettingSingleLineTextField(
                                                        label = "Width",
                                                        value = uiState.envelopeWidth,
                                                        onValueChange = onEnvelopeWidthChanged,
                                                        placeholder = "6.5",
                                                        suffix = "in"
                                                )
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))

                                        Text(
                                                text = "Bubble Mailer",
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = textPrimary
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                                SettingSingleLineTextField(
                                                        label = "Default Cost",
                                                        value = uiState.bubbleMailerCost,
                                                        onValueChange = onBubbleMailerCostChanged,
                                                        placeholder = "7.00",
                                                        prefix = "$"
                                                )
                                                SettingSingleLineTextField(
                                                        label = "Length",
                                                        value = uiState.bubbleMailerLength,
                                                        onValueChange = onBubbleMailerLengthChanged,
                                                        placeholder = "6",
                                                        suffix = "in"
                                                )
                                                SettingSingleLineTextField(
                                                        label = "Width",
                                                        value = uiState.bubbleMailerWidth,
                                                        onValueChange = onBubbleMailerWidthChanged,
                                                        placeholder = "9",
                                                        suffix = "in"
                                                )
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))

                                        Text(
                                                text = "Box",
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = textPrimary
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                                SettingSingleLineTextField(
                                                        label = "Default Cost",
                                                        value = uiState.boxCost,
                                                        onValueChange = onBoxCostChanged,
                                                        placeholder = "10.00",
                                                        prefix = "$"
                                                )
                                                SettingSingleLineTextField(
                                                        label = "Length",
                                                        value = uiState.boxLength,
                                                        onValueChange = onBoxLengthChanged,
                                                        placeholder = "6",
                                                        suffix = "in"
                                                )
                                                SettingSingleLineTextField(
                                                        label = "Width",
                                                        value = uiState.boxWidth,
                                                        onValueChange = onBoxWidthChanged,
                                                        placeholder = "9",
                                                        suffix = "in"
                                                )
                                                SettingSingleLineTextField(
                                                        label = "Height",
                                                        value = uiState.boxHeight,
                                                        onValueChange = onBoxHeightChanged,
                                                        placeholder = "6",
                                                        suffix = "in"
                                                )
                                        }

                                        Spacer(modifier = Modifier.height(24.dp))

                                        HorizontalDivider(
                                                color = bgSecondary,
                                                modifier = Modifier.padding(horizontal = 16.dp)
                                        )

                                        Spacer(modifier = Modifier.height(24.dp))

                                        Text(
                                                text = "Database Backups",
                                                style = MaterialTheme.typography.headlineSmall,
                                                color = accentPrimary
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))

                                        Text(
                                                text =
                                                        if (uiState.lastBackupDate != null) {
                                                                "Last backup: ${uiState.lastBackupDate}"
                                                        } else {
                                                                "No backups yet"
                                                        },
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = textSecondary
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                                Button(
                                                        onClick = onBackupNow,
                                                        enabled = !uiState.isCreatingBackup,
                                                        colors =
                                                                ButtonDefaults.buttonColors(
                                                                        containerColor =
                                                                                accentPrimary,
                                                                        contentColor = textOnAccent,
                                                                        disabledContainerColor =
                                                                                bgSurface,
                                                                        disabledContentColor =
                                                                                textTertiary
                                                                ),
                                                        shape = RoundedCornerShape(8.dp)
                                                ) {
                                                        Text(
                                                                if (uiState.isCreatingBackup)
                                                                        "Creating Backup..."
                                                                else "Backup Now"
                                                        )
                                                }

                                                OutlinedButton(
                                                        onClick = onShowRestoreDialog,
                                                        enabled = !uiState.isRestoring,
                                                        colors =
                                                                ButtonDefaults.outlinedButtonColors(
                                                                        contentColor = textPrimary
                                                                ),
                                                        shape = RoundedCornerShape(8.dp)
                                                ) {
                                                        Text(
                                                                if (uiState.isRestoring)
                                                                        "Restoring..."
                                                                else "Restore from Backup"
                                                        )
                                                }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(
                                                text =
                                                        "Backups are created automatically every 24 hours. The 10 most recent backups are kept.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = textTertiary
                                        )
                                }

                                VerticalScrollbar(
                                        adapter = rememberScrollbarAdapter(scrollState),
                                        modifier =
                                                Modifier.align(Alignment.CenterEnd)
                                                        .fillMaxHeight()
                                                        .padding(end = 4.dp),
                                        style =
                                                defaultScrollbarStyle()
                                                        .copy(
                                                                unhoverColor =
                                                                        accentPrimary.copy(
                                                                                alpha = 0.4f
                                                                        ),
                                                                hoverColor =
                                                                        accentPrimary.copy(
                                                                                alpha = 0.7f
                                                                        )
                                                        )
                                )
                        }
                }

                uiState.toast?.let { toast ->
                        SettingsToast(
                                toast = toast,
                                onDismiss = onClearToast,
                                modifier = Modifier.align(Alignment.BottomCenter)
                        )
                }

                if (uiState.showRestoreDialog) {
                        RestoreBackupListDialog(
                                backups = uiState.availableBackups,
                                onSelectBackup = onSelectBackupToRestore,
                                onDismiss = onDismissRestoreDialog
                        )
                }

                uiState.showRestoreConfirmation?.let { backup ->
                        RestoreConfirmationDialog(
                                backup = backup,
                                onConfirm = onConfirmRestore,
                                onDismiss = onDismissRestoreConfirmation
                        )
                }
        }
}

@Composable
private fun RestoreBackupListDialog(
        backups: List<BackupInfo>,
        onSelectBackup: (BackupInfo) -> Unit,
        onDismiss: () -> Unit
) {
        AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(text = "Restore from Backup", color = textPrimary) },
                text = {
                        if (backups.isEmpty()) {
                                Text(text = "No backups available.", color = textSecondary)
                        } else {
                                ScrollableList(
                                        modifier = Modifier.height(300.dp),
                                        scrollbarPadding = 16.dp
                                ) {
                                        items(backups) { backup ->
                                                Column(
                                                        modifier =
                                                                Modifier.fillMaxWidth()
                                                                        .clickable {
                                                                                onSelectBackup(
                                                                                        backup
                                                                                )
                                                                        }
                                                                        .padding(
                                                                                vertical = 12.dp,
                                                                                horizontal = 4.dp
                                                                        )
                                                ) {
                                                        Text(
                                                                text = backup.displayDate,
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .bodyLarge,
                                                                color = textPrimary
                                                        )
                                                        Text(
                                                                text = backup.fileName,
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .bodySmall,
                                                                color = textTertiary
                                                        )
                                                }
                                                HorizontalDivider(color = bgSecondary)
                                        }
                                }
                        }
                },
                confirmButton = {},
                dismissButton = {
                        TextButton(onClick = onDismiss) { Text("Cancel", color = accentPrimary) }
                },
                containerColor = bgSurface
        )
}

@Composable
private fun RestoreConfirmationDialog(
        backup: BackupInfo,
        onConfirm: () -> Unit,
        onDismiss: () -> Unit
) {
        AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(text = "Restore Backup?", color = textPrimary) },
                text = {
                        Column {
                                Text(
                                        text =
                                                "This will overwrite ALL current data with data from the selected backup. The app will close and you will need to reopen it.",
                                        color = textSecondary,
                                        style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                        text = "Backup: ${backup.displayDate}",
                                        color = textPrimary,
                                        style = MaterialTheme.typography.bodyMedium
                                )
                        }
                },
                confirmButton = {
                        Button(
                                onClick = onConfirm,
                                colors =
                                        ButtonDefaults.buttonColors(
                                                containerColor = errorColor,
                                                contentColor = textOnAccent
                                        ),
                                shape = RoundedCornerShape(8.dp)
                        ) { Text("Restore") }
                },
                dismissButton = {
                        TextButton(onClick = onDismiss) { Text("Cancel", color = accentPrimary) }
                },
                containerColor = bgSurface
        )
}

@Composable
private fun SettingsToast(
        toast: SettingsToastState,
        onDismiss: () -> Unit,
        modifier: Modifier = Modifier
) {
        LaunchedEffect(toast.message) {
                delay(3000)
                onDismiss()
        }

        Box(
                modifier =
                        modifier.padding(16.dp)
                                .fillMaxWidth()
                                .background(
                                        color = if (toast.isError) errorColor else successColor,
                                        shape = RoundedCornerShape(8.dp)
                                )
                                .padding(16.dp)
        ) {
                Text(
                        text = toast.message,
                        color = textOnAccent,
                        style = MaterialTheme.typography.bodyMedium
                )
        }
}

@Composable
private fun SettingTextField(
        label: String,
        description: String,
        value: String,
        onValueChange: (String) -> Unit
) {
        Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = label, style = MaterialTheme.typography.bodyLarge, color = textPrimary)

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = textSecondary
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                        value = value,
                        onValueChange = onValueChange,
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 6,
                        colors =
                                OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = textPrimary,
                                        unfocusedTextColor = textPrimary,
                                        focusedBorderColor = accentPrimary,
                                        unfocusedBorderColor = textTertiary,
                                        cursorColor = accentPrimary,
                                        focusedContainerColor = bgSurface,
                                        unfocusedContainerColor = bgSurface
                                ),
                        shape = RoundedCornerShape(8.dp)
                )
        }
}

@Composable
private fun SettingToggle(
        label: String,
        description: String,
        checked: Boolean,
        onCheckedChange: (Boolean) -> Unit
) {
        Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                ) {
                        Column(modifier = Modifier.weight(1f)) {
                                Text(
                                        text = label,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = textPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                        text = description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = textSecondary
                                )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Switch(
                                checked = checked,
                                onCheckedChange = onCheckedChange,
                                colors =
                                        SwitchDefaults.colors(
                                                checkedThumbColor = textOnAccent,
                                                checkedTrackColor = accentPrimary,
                                                uncheckedThumbColor = textTertiary,
                                                uncheckedTrackColor = bgSecondary
                                        )
                        )
                }
        }
}

@Composable
private fun SettingSingleLineTextField(
        label: String,
        value: String,
        onValueChange: (String) -> Unit,
        placeholder: String = "",
        prefix: String? = null,
        suffix: String? = null,
        description: String? = null,
        modifier: Modifier = Modifier
) {
        Column(modifier = modifier) {
                Text(text = label, style = MaterialTheme.typography.bodyLarge, color = textPrimary)

                if (description != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                                text = description,
                                style = MaterialTheme.typography.bodySmall,
                                color = textSecondary
                        )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                        value = value,
                        onValueChange = onValueChange,
                        modifier = Modifier.width(200.dp),
                        singleLine = true,
                        placeholder = { Text(text = placeholder, color = textTertiary) },
                        prefix =
                                if (prefix != null) {
                                        { Text(prefix, color = textPrimary) }
                                } else null,
                        suffix =
                                if (suffix != null) {
                                        { Text(suffix, color = textPrimary) }
                                } else null,
                        colors =
                                OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = textPrimary,
                                        unfocusedTextColor = textPrimary,
                                        focusedBorderColor = accentPrimary,
                                        unfocusedBorderColor = textTertiary,
                                        cursorColor = accentPrimary,
                                        focusedContainerColor = bgSurface,
                                        unfocusedContainerColor = bgSurface
                                ),
                        shape = RoundedCornerShape(8.dp)
                )
        }
}
