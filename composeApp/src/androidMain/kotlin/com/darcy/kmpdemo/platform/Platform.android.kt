package com.darcy.kmpdemo.platform

import android.os.Build

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
    override val version: String = "1.0.0"

    override fun toString(): String {
        return "AndroidPlatform(name='$name', version='$version')"
    }

}

actual fun getPlatform(): Platform = AndroidPlatform()
actual fun isPhonePlatform(): Boolean {
    return true
}

actual fun isJvmPlatform(): Boolean {
    return false
}