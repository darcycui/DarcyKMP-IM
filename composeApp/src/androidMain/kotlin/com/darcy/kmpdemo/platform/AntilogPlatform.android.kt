package com.darcy.kmpdemo.platform

import io.github.aakira.napier.Antilog
import io.github.aakira.napier.DebugAntilog

actual object AntilogPlatform {
    actual fun createPlatformAntilog(): Antilog = DebugAntilog()
}