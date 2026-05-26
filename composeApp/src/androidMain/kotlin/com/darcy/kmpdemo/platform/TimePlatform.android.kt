package com.darcy.kmpdemo.platform

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

actual object TimePlatform {
    private const val TIME_FORMATTER: String = "yyyy-MM-dd HH:mm:ss.SSS"

    @RequiresApi(Build.VERSION_CODES.O)
    actual fun getCurrentTimeStamp(): String {
        val formatter = DateTimeFormatter.ofPattern(TIME_FORMATTER)
        return LocalDateTime.now().format(formatter)
    }
}