package com.darcy.kmpdemo

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.darcy.kmpdemo.ui.theme.AppTheme

@OptIn(ExperimentalComposeUiApi::class)
fun main() {

    ComposeViewport {
        // 通过主题设置字体 解决中文乱码问题
        AppTheme {
            App()
        }
    }
}