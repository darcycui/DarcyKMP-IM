package com.darcy.kmpdemo.platform

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

actual object TimePlatform {
    private const val TIME_FORMATTER: String = "yyyy-MM-dd HH:mm:ss.SSS"
    actual fun getCurrentTimeStamp(): String {
        val formatter = DateTimeFormatter.ofPattern(TIME_FORMATTER)
        return LocalDateTime.now().format(formatter)
    }
}