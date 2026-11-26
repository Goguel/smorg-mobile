package br.ufrn.smorg

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import java.io.File

fun main() =
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "SmOrg — Mobile & Desktop (DIM0524)",
        ) {
            App(dataDirectory = desktopDataDirectory())
        }
    }

fun desktopDataDirectory(): String = File(System.getProperty("user.home"), ".smorg-mobile").apply { mkdirs() }.path
