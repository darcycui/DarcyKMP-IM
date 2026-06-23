package com.darcy.kmpdemo.platform

@OptIn(ExperimentalWasmJsInterop::class)
private fun getCurrentTimeStampImpl(): String =
    js("new Date().toISOString().replace('T', ' ').slice(0, -1)")


actual object TimePlatform {
    actual fun getCurrentTimeStamp(): String {
        return getCurrentTimeStampImpl()
    }
}