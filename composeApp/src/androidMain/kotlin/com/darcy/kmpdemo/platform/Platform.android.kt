package com.darcy.kmpdemo.platform

import android.os.Build

class AndroidIPlatform : IPlatform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
    override val version: String = "1.0.0"

    override fun toString(): String {
        return "AndroidPlatform(name='$name', version='$version')"
    }
}

actual object Platform {
    actual fun getPlatform(): IPlatform = AndroidIPlatform()
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