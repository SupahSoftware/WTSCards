package com.wtscards.ui.screens.settings

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
    onFreeShippingEnabledChanged: (Boolean) -> Unit,
    onFreeShippingThresholdChanged: (String) -> Unit,
    onNicePricesEnabledChanged: (Boolean) -> Unit,
    onDefaultDiscountChanged: (String) -> Unit,
    onSave: () -> Unit,
    onClearToast: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
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
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accentPrimary,
                        contentColor = textOnAccent,
                        disabledContainerColor = bgSurface,
                        disabledContentColor = textTertiary
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(if (uiState.isSaving) "Saving..." else "Save")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Listings",
                    style = MaterialTheme.typography.titleMedium,
                    color = accentPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))

                SettingTextField(
                    label = "Default pre-body text",
                    description = "This text will be automatically inserted before your listed cards when you click copy body",
                    value = uiState.preBodyText,
                    onValueChange = onPreBodyTextChanged
                )

                Spacer(modifier = Modifier.height(20.dp))

                SettingTextField(
                    label = "Default post-body text",
                    description = "This text will be automatically inserted after your listed cards when you click copy body",
                    value = uiState.postBodyText,
                    onValueChange = onPostBodyTextChanged
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Orders",
                    style = MaterialTheme.typography.titleMedium,
                    color = accentPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))

                SettingToggle(
                    label = "Free shipping on orders over a certain amount",
                    description = "When enabled, shipping cost will not be added to the order total if the card subtotal meets the threshold",
                    checked = uiState.freeShippingEnabled,
                    onCheckedChange = onFreeShippingEnabledChanged
                )

                if (uiState.freeShippingEnabled) {
                    Spacer(modifier = Modifier.height(8.dp))
                    SettingSingleLineTextField(
                        label = "Free shipping threshold",
                        value = uiState.freeShippingThreshold,
                        onValueChange = onFreeShippingThresholdChanged,
                        placeholder = "0.00",
                        prefix = "$"
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                SettingToggle(
                    label = "Nice prices",
                    description = "Round card prices up to the nearest dollar on the orders screen",
                    checked = uiState.nicePricesEnabled,
                    onCheckedChange = onNicePricesEnabledChanged
                )

                Spacer(modifier = Modifier.height(20.dp))

                SettingSingleLineTextField(
                    label = "Default discount percentage",
                    description = "Default discount to apply to new orders. Enter as a whole number (e.g. 5 for 5%)",
                    value = uiState.defaultDiscount,
                    onValueChange = onDefaultDiscountChanged,
                    placeholder = "0",
                    suffix = "%"
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
    }
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
        modifier = modifier
            .padding(16.dp)
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

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 6,
            colors = OutlinedTextFieldDefaults.colors(
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
                colors = SwitchDefaults.colors(
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
    description: String? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = textPrimary
        )

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
            placeholder = {
                Text(text = placeholder, color = textTertiary)
            },
            prefix = if (prefix != null) {
                { Text(prefix, color = textPrimary) }
            } else null,
            suffix = if (suffix != null) {
                { Text(suffix, color = textPrimary) }
            } else null,
            colors = OutlinedTextFieldDefaults.colors(
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
