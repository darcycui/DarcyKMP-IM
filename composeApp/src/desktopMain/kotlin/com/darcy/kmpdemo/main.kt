package com.darcy.kmpdemo

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.darcy.kmpdemo.log.logD
//import io.github.kotlin.fibonacci.generateFibi

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "IM聊天",
    ) {
        App()
//        val x = generateFibi().take(3).last()
//        logD("fibonacci: $x")
    }
}