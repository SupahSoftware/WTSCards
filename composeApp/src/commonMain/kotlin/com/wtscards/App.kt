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
import com.wtscards.ui.components.ImportConflictDialog
import com.wtscards.ui.navigation.BottomNavBar
import com.wtscards.ui.navigation.NavigationItem
import com.wtscards.ui.screens.collection.CollectionScreen
import com.wtscards.ui.screens.collection.CollectionViewModel
import com.wtscards.ui.screens.import.ImportScreen
import com.wtscards.ui.screens.import.ImportState
import com.wtscards.ui.screens.import.ImportViewModel
import com.wtscards.ui.theme.WTSCardsTheme
import com.wtscards.ui.theme.bgPrimary

@Composable
fun App(
    dependencies: AppDependencies,
    importViewModel: ImportViewModel
) {
    WTSCardsTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = bgPrimary
        ) {
            MainScreen(
                dependencies = dependencies,
                importViewModel = importViewModel
            )
        }
    }
}

@Composable
fun MainScreen(
    dependencies: AppDependencies,
    importViewModel: ImportViewModel
) {
    var currentRoute by remember { mutableStateOf(NavigationItem.Collection.route) }

    val collectionViewModel = remember {
        CollectionViewModel(dependencies.cardUseCase, dependencies.coroutineScope)
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
            NavigationItem.Import.route -> {
                ImportScreen(
                    uiState = importViewModel.uiState,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }

        // Show conflict dialog when needed
        val importState = importViewModel.uiState.importState
        if (importState is ImportState.ConflictDetected) {
            ImportConflictDialog(
                collisionCount = importState.collisions.size,
                totalCount = importState.cards.size,
                onStrategySelected = { strategy ->
                    importViewModel.onImportStrategySelected(strategy)
                },
                onDismiss = {
                    importViewModel.onConflictDialogDismissed()
                }
            )
        }
    }
}
