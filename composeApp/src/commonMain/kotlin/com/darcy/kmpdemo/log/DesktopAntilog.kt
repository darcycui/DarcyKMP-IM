package com.darcy.kmpdemo.log

import com.darcy.kmpdemo.platform.TimePlatform
import io.github.aakira.napier.Antilog
import io.github.aakira.napier.LogLevel

class DesktopAntilog : Antilog() {

    private val ANSI_RESET = "\u001B[0m"
    private val ANSI_BLUE = "\u001B[34m"      // DEBUG
    private val ANSI_GREEN = "\u001B[32m"     // INFO
    private val ANSI_YELLOW = "\u001B[33m"    // WARN
    private val ANSI_RED = "\u001B[31m"       // ERROR
    private val ANSI_WHITE = "\u001B[38;5;250m"      // VERBOSE

    override fun performLog(
        priority: LogLevel,
        tag: String?,
        throwable: Throwable?,
        message: String?
    ) {
        val timestamp = TimePlatform.getCurrentTimeStamp()

        val color = when (priority) {
            LogLevel.VERBOSE -> ANSI_WHITE
            LogLevel.DEBUG -> ANSI_BLUE
            LogLevel.INFO -> ANSI_GREEN
            LogLevel.WARNING -> ANSI_YELLOW
            LogLevel.ERROR -> ANSI_RED
            LogLevel.ASSERT -> ANSI_RED
        }

        val levelStr = when (priority) {
            LogLevel.VERBOSE -> "VERBOSE"
            LogLevel.DEBUG -> "DEBUG"
            LogLevel.INFO -> "INFO"
            LogLevel.WARNING -> "WARN"
            LogLevel.ERROR -> "ERROR"
            LogLevel.ASSERT -> "ASSERT"
        }

        val tagStr = tag ?: "DarcyLog"
        val msg = message ?: ""

        println("$timestamp [$levelStr] $color$tagStr - $msg${ANSI_RESET}")

        throwable?.printStackTrace()
    }
}
