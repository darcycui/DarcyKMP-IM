package com.darcy.kmpdemo.log

import com.darcy.kmpdemo.platform.TimePlatform
import io.github.aakira.napier.Antilog
import io.github.aakira.napier.LogLevel

/**
 * 彩色日志打印
 * 用于 desktop、web
 */
class ColorfulAntilog : Antilog() {

    companion object {
        private const val ANSI_RESET = "\u001B[0m"
        private const val ANSI_BLUE = "\u001B[34m"      // 蓝色 DEBUG
        private const val ANSI_GREEN = "\u001B[32m"     // 绿色 INFO
        private const val ANSI_YELLOW = "\u001B[33m"    // 黄色 WARN
        private const val ANSI_RED = "\u001B[31m"       // 红色 ERROR
        private const val ANSI_WHITE = "\u001B[38;5;250m" // 白色 VERBOSE
    }

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
