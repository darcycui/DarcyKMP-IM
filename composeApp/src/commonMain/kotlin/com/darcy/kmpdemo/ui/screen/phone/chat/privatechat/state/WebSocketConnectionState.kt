package com.darcy.kmpdemo.ui.screen.phone.chat.privatechat.state

sealed class WebSocketConnectionState(
    val message: String = ""
) {
    data object Connected : WebSocketConnectionState("已连接")
    data object Disconnected : WebSocketConnectionState("已断开")
    data class Error(val message1: String) : WebSocketConnectionState("错误:$message1")
}