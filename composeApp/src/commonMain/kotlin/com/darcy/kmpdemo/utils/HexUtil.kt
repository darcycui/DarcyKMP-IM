package com.darcy.kmpdemo.utils

import com.darcy.kmpdemo.log.logE

object HexUtil {
    private val TAG = HexUtil::class.simpleName

    @OptIn(ExperimentalStdlibApi::class)
    fun bytesToHexStr(bytes: ByteArray?, uppercase: Boolean = false): String {
        if (bytes == null || bytes.isEmpty()) return ""
        return runCatching {
            val str = bytes.toHexString()
            if (uppercase) str.uppercase() else str.lowercase()
        }.onFailure {
            logE("$TAG bytesToHexStr error: ${it::class.simpleName} ${it.message}")
            it.printStackTrace()
        }.getOrElse { "" }

    }

    @OptIn(ExperimentalStdlibApi::class)
    fun hexStrToBytes(hex: String?): ByteArray {
        if (hex.isNullOrEmpty()) return ByteArray(0)
        return runCatching {
            hex.hexToByteArray()
        }.onFailure {
            logE("$TAG hexStrToBytes error: ${it::class.simpleName} ${it.message}")
            it.printStackTrace()
        }.getOrElse { ByteArray(0) }
    }

    fun isHexString(hex: String?): Boolean {
        if (hex.isNullOrEmpty()) return false
        return hex.matches(Regex("^[0-9a-fA-F]+$"))
    }
}

fun String.hexStrToBytes(): ByteArray {
    return HexUtil.hexStrToBytes(this)
}

fun ByteArray.bytesToHexStr(uppercase: Boolean = false): String {
    return HexUtil.bytesToHexStr(this, uppercase)
}

fun String?.isHexString(): Boolean {
    return HexUtil.isHexString(this)
}