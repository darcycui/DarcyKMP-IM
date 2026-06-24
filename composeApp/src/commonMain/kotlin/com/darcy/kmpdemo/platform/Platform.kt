package com.darcy.kmpdemo.platform

interface IPlatform {
    val version: String
    val name: String
}

expect object Platform {

    fun getPlatform(): IPlatform

    fun isPhonePlatform(): Boolean

    fun isJvmPlatform(): Boolean

    fun isJsPlatform(): Boolean
}

