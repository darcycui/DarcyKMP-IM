package com.darcy.kmpdemo.platform

interface Platform {
    val version: String
    val name: String
}

expect fun getPlatform(): Platform

expect fun isPhonePlatform(): Boolean

expect fun isJvmPlatform(): Boolean