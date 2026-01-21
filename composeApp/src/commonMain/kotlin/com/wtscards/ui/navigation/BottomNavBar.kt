package com.wtscards.ui.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.wtscards.ui.theme.accentPrimary
import com.wtscards.ui.theme.bgSecondary
import com.wtscards.ui.theme.textPrimary
import com.wtscards.ui.theme.textTertiary

@Composable
fun BottomNavBar(
    currentRoute: String,
    onNavigate: (NavigationItem) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = bgSecondary
    ) {
        NavigationItem.entries.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(item) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = accentPrimary,
                    selectedTextColor = accentPrimary,
                    unselectedIconColor = textTertiary,
                    unselectedTextColor = textTertiary,
                    indicatorColor = bgSecondary
                )
            )
        }
    }
}
