package com.darcy.kmpdemo.platform

import io.github.aakira.napier.Antilog

expect object AntilogPlatform {
    fun createPlatformAntilog(): Antilog
}
