package com.darcy.kmpdemo.platform

class JsIPlatform : IPlatform {
    override val name: String = "Web with Kotlin/JS"
    override val version: String = "1.0.0"
}

actual object Platform {
    actual fun getPlatform(): IPlatform {
        return JsIPlatform()
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