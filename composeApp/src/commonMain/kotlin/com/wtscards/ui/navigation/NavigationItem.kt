package com.wtscards.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.ui.graphics.vector.ImageVector

enum class NavigationItem(
    val label: String,
    val icon: ImageVector,
    val route: String
) {
    Collection(
        label = "Collection",
        icon = Icons.Default.GridView,
        route = "collection"
    )
}
