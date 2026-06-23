package com.darcy.kmpdemo.platform

actual object TimePlatform {
    actual fun getCurrentTimeStamp(): String {
        return js("new Date().toISOString().replace('T', ' ').slice(0, -1)") as String
    }
}