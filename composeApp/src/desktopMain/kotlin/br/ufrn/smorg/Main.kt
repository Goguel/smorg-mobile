package br.ufrn.smorg

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() =
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "SmOrg — Mobile & Desktop (DIM0524)",
        ) {
            App()
        }
    }
