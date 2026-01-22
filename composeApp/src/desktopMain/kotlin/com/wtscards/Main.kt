package com.wtscards

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.awtTransferable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import com.wtscards.data.db.CardLocalDataSource
import com.wtscards.data.db.DatabaseDriverFactory
import com.wtscards.data.parser.CsvParser
import com.wtscards.db.WTSCardsDatabase
import com.wtscards.domain.usecase.CardUseCaseImpl
import com.wtscards.ui.screens.import.ImportViewModel
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
fun main() = application {
    val screenSize = Toolkit.getDefaultToolkit().screenSize
    val preferredWidth = 1920
    val preferredHeight = 1080

    val (windowWidth, windowHeight) = if (screenSize.width >= preferredWidth && screenSize.height >= preferredHeight) {
        preferredWidth to preferredHeight
    } else {
        (screenSize.width * 0.9).toInt() to (screenSize.height * 0.9).toInt()
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = "WTSCards",
        state = WindowState(width = windowWidth.dp, height = windowHeight.dp)
    ) {
        val coroutineScope = rememberCoroutineScope()

        val dependencies = remember {
            val driver = DatabaseDriverFactory.createDriver()
            val database = WTSCardsDatabase(driver)
            val localDataSource = CardLocalDataSource(database)
            val cardUseCase = CardUseCaseImpl(localDataSource)

            AppDependencies(
                cardUseCase = cardUseCase,
                coroutineScope = coroutineScope
            )
        }

        val importViewModel = remember {
            ImportViewModel(dependencies.cardUseCase, coroutineScope)
        }

        val dragAndDropTarget = remember {
            object : DragAndDropTarget {
                override fun onStarted(event: DragAndDropEvent) {
                    try {
                        val transferable = event.awtTransferable
                        if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                            val files = transferable.getTransferData(DataFlavor.javaFileListFlavor) as? List<*>
                            val file = files?.firstOrNull() as? File
                            if (file != null && file.extension.lowercase() == "csv") {
                                importViewModel.onFileHoverStart(file.name)
                            }
                        }
                    } catch (e: Exception) {
                        importViewModel.onFileHoverStart("file.csv")
                    }
                }

                override fun onEnded(event: DragAndDropEvent) {
                    importViewModel.onFileHoverEnd()
                }

                override fun onDrop(event: DragAndDropEvent): Boolean {
                    try {
                        val transferable = event.awtTransferable
                        if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                            val files = transferable.getTransferData(DataFlavor.javaFileListFlavor) as? List<*>
                            val file = files?.firstOrNull() as? File

                            if (file == null) {
                                return false
                            }

                            if (file.extension.lowercase() != "csv") {
                                importViewModel.onImportError("Only CSV files are supported")
                                return true
                            }

                            val result = CsvParser.parseFile(file)
                            result.fold(
                                onSuccess = { cards ->
                                    if (cards.isEmpty()) {
                                        importViewModel.onImportError("No valid card data found in CSV")
                                    } else {
                                        importViewModel.onFileDropped(cards)
                                    }
                                },
                                onFailure = { error ->
                                    importViewModel.onImportError(error.message ?: "Failed to parse CSV file")
                                }
                            )
                            return true
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        importViewModel.onImportError("Failed to process file")
                    }
                    return false
                }
            }
        }

        val onBrowseFiles: () -> Unit = onBrowseFiles@{
            val fileChooser = JFileChooser().apply {
                dialogTitle = "Select CSV File"
                fileFilter = FileNameExtensionFilter("CSV Files (*.csv)", "csv")
                isAcceptAllFileFilterUsed = false
            }

            val result = fileChooser.showOpenDialog(null)
            if (result == JFileChooser.APPROVE_OPTION) {
                val file = fileChooser.selectedFile
                if (file.extension.lowercase() != "csv") {
                    importViewModel.onImportError("Only CSV files are supported")
                    return@onBrowseFiles
                }

                val parseResult = CsvParser.parseFile(file)
                parseResult.fold(
                    onSuccess = { cards ->
                        if (cards.isEmpty()) {
                            importViewModel.onImportError("No valid card data found in CSV")
                        } else {
                            importViewModel.onFileDropped(cards)
                        }
                    },
                    onFailure = { error ->
                        importViewModel.onImportError(error.message ?: "Failed to parse CSV file")
                    }
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .dragAndDropTarget(
                    shouldStartDragAndDrop = { event ->
                        try {
                            event.awtTransferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)
                        } catch (e: Exception) {
                            true
                        }
                    },
                    target = dragAndDropTarget
                )
        ) {
            App(
                dependencies = dependencies,
                importViewModel = importViewModel,
                onBrowseFiles = onBrowseFiles
            )
        }
    }
}
