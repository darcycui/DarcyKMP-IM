package com.darcy.kmpdemo.platform

class JsPlatform : Platform {
    override val name: String = "Web with Kotlin/JS"
    override val version: String = "1.0.0"
}

actual fun getPlatform(): Platform {
    return JsPlatform()
}

actual fun isPhonePlatform(): Boolean {
    return false
}

actual fun isJvmPlatform(): Boolean {
    return false
}