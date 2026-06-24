package com.darcy.kmpdemo.platform

import com.darcy.kmpdemo.network.http.urls.WebSockets

actual object WebSocketPlatform {
    actual fun getWebsocketUrl(): String {
        return WebSockets.WEBSOCKET_URL
    }
}