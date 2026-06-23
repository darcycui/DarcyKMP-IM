package com.darcy.kmpdemo.log

import io.github.aakira.napier.Antilog

actual fun createPlatformAntilog(): Antilog = ColorfulAntilog()
