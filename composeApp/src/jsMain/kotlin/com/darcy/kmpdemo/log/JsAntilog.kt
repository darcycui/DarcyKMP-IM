package com.darcy.kmpdemo.log

import io.github.aakira.napier.Antilog
import io.github.aakira.napier.LogLevel

private external val console: Console

private external class Console {
    fun log(vararg messages: Any?)
    fun info(vararg messages: Any?)
    fun warn(vararg messages: Any?)
    fun error(vararg messages: Any?)
}

class JsAntilog : Antilog() {
    override fun performLog(priority: LogLevel, tag: String?, throwable: Throwable?, message: String?) {
        val tagStr = tag ?: "DarcyLog"
        val msg = message ?: ""
        val formattedMsg = "[${priority.name}][$tagStr] $msg"
        when (priority) {
            LogLevel.WARNING -> console.warn(formattedMsg)
            LogLevel.ERROR, LogLevel.ASSERT -> console.error(formattedMsg)
            LogLevel.INFO -> console.info(formattedMsg)
            else -> console.log(formattedMsg)
        }
        throwable?.let { console.log(it) }
    }
}

actual fun createPlatformAntilog(): Antilog = JsAntilog()
