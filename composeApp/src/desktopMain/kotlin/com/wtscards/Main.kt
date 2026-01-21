package com.wtscards

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import java.awt.Toolkit

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
        App()
    }
}
