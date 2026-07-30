package com.darcy.kmpdemo.platform

class JVMIPlatform : IPlatform {
    override val name: String = "Java ${System.getProperty("java.version")}"
    override val version: String = "1.0.0"

    override fun toString(): String {
        return "JVMPlatform(name='$name', version='$version')"
    }
}

actual object Platform {
    actual fun getPlatform(): IPlatform = JVMIPlatform()

    actual fun isPhonePlatform(): Boolean {
        return false
    }

    actual fun isJvmPlatform(): Boolean {
        return true
    }

    actual fun isJsPlatform(): Boolean {
        return false
    }
}