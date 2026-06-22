package com.darcy.kmpdemo.platform

class IOSPlatform : Platform {
    override val name: String = "iOS with Kotlin"
    override val version: String = "1.0.0"
}

actual fun getPlatform(): Platform {
    return IOSPlatform()
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