package com.darcy.kmpdemo.platform

import com.darcy.kmpdemo.log.ColorfulAntilog
import io.github.aakira.napier.Antilog

actual object AntilogPlatform {
    actual fun createPlatformAntilog(): Antilog {
        return ColorfulAntilog()
    }
}