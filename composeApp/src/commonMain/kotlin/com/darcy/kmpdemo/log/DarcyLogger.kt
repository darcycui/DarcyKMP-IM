package com.darcy.kmpdemo.log

import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import com.darcy.kmpdemo.platform.getPlatform
import com.darcy.kmpdemo.platform.isJvmPlatform

const val DARCY_TAG = "DarcyLog"

object DarcyLogger {
    val isJVMPlatform = isJvmPlatform()
    fun initLogger() {
        if (isJVMPlatform) {
            Napier.base(DesktopAntilog())
        } else {
            Napier.base(DebugAntilog())
        }
//    Napier.base(ReleaseAntiLog())
        val platform = getPlatform()
        logD("initLogger $platform")
    }
}

fun logD(msg: String, tag: String = DARCY_TAG, throwable: Throwable? = null) {
    Napier.d(message = msg, tag = tag, throwable = throwable)
}

fun logI(msg: String, tag: String = DARCY_TAG, throwable: Throwable? = null) {
    Napier.i(message = msg, tag = tag, throwable = throwable)
}

fun logV(msg: String, tag: String = DARCY_TAG, throwable: Throwable? = null) {
    Napier.v(message = msg, tag = tag, throwable = throwable)
}

fun logW(msg: String, tag: String = DARCY_TAG, throwable: Throwable? = null) {
    Napier.w(message = msg, tag = tag, throwable = throwable)
}

fun logE(msg: String, tag: String = DARCY_TAG, throwable: Throwable? = null) {
    val throwableMsg = throwable?.let { " :${it::class} ${it.message}" } ?: ""
    Napier.e(message = msg + throwableMsg, tag = tag, throwable = throwable)
}