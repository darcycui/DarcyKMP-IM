package com.darcy.kmpdemo.platform

class IOSIPlatform : IPlatform {
    override val name: String = "iOS with Kotlin"
    override val version: String = "1.0.0"
}

actual object Platform {
    actual fun getPlatform(): IPlatform {
        return IOSIPlatform()
    }

    actual fun isPhonePlatform(): Boolean {
        return true
    }

    actual fun isJvmPlatform(): Boolean {
        return false
    }

    actual fun isJsPlatform(): Boolean {
        return false
    }
}