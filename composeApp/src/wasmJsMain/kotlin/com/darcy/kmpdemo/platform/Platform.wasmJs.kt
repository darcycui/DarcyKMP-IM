package com.darcy.kmpdemo.platform


class WasmIPlatform : IPlatform {
    override val name: String = "Web with Kotlin/Wasm"
    override val version: String = "1.0.0"
}

actual object Platform {
    actual fun getPlatform(): IPlatform {
        return WasmIPlatform()
    }

    actual fun isPhonePlatform(): Boolean {
        return false
    }

    actual fun isJvmPlatform(): Boolean {
        return false
    }

    actual fun isJsPlatform(): Boolean {
        return true
    }
}