package com.wtscards.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
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
    ),
    AddCard(
        label = "Add Card",
        icon = Icons.Default.Add,
        route = "addcard"
    ),
    Listings(
        label = "Listings",
        icon = Icons.Default.FormatListBulleted,
        route = "listings"
    ),
    Orders(
        label = "Orders",
        icon = Icons.Default.ShoppingBag,
        route = "orders"
    ),
    Import(
        label = "Import",
        icon = Icons.Default.FileUpload,
        route = "import"
    ),
    Settings(
        label = "Settings",
        icon = Icons.Default.Settings,
        route = "settings"
    )
}
