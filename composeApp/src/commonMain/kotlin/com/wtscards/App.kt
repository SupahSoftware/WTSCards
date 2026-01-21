package com.wtscards

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.wtscards.data.repository.CollectionRepositoryImpl
import com.wtscards.ui.navigation.BottomNavBar
import com.wtscards.ui.navigation.NavigationItem
import com.wtscards.ui.screens.collection.CollectionScreen
import com.wtscards.ui.screens.collection.CollectionViewModel
import com.wtscards.ui.theme.WTSCardsTheme
import com.wtscards.ui.theme.bgPrimary

@Composable
fun App() {
    WTSCardsTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = bgPrimary
        ) {
            MainScreen()
        }
    }
}

@Composable
fun MainScreen() {
    var currentRoute by remember { mutableStateOf(NavigationItem.Collection.route) }

    val collectionViewModel = remember {
        CollectionViewModel(CollectionRepositoryImpl())
    }

    Scaffold(
        containerColor = bgPrimary,
        bottomBar = {
            BottomNavBar(
                currentRoute = currentRoute,
                onNavigate = { item -> currentRoute = item.route }
            )
        }
    ) { paddingValues ->
        when (currentRoute) {
            NavigationItem.Collection.route -> {
                CollectionScreen(
                    uiState = collectionViewModel.uiState,
                    onRefresh = collectionViewModel::onRefresh,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}
