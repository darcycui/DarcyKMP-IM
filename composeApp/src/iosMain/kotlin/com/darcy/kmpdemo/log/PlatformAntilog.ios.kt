package com.darcy.kmpdemo.log

import io.github.aakira.napier.Antilog
import io.github.aakira.napier.DebugAntilog

actual fun createPlatformAntilog(): Antilog = DebugAntilog()
