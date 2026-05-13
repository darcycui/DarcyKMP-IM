package com.darcy.kmpdemo.utils

object HexUtil {
    @OptIn(ExperimentalStdlibApi::class)
    fun bytesToHexStr(bytes: ByteArray?, uppercase: Boolean = false): String {
        if (bytes == null || bytes.isEmpty()) return ""
        return runCatching {
            val str = bytes.toHexString()
            if (uppercase) str.uppercase() else str.lowercase()
        }.onFailure {
            it.printStackTrace()
        }.getOrElse { "" }

    }

    @OptIn(ExperimentalStdlibApi::class)
    fun hexStrToBytes(hex: String?): ByteArray {
        if (hex.isNullOrEmpty()) return ByteArray(0)
        return runCatching {
            hex.hexToByteArray()
        }.onFailure {
            it.printStackTrace()
        }.getOrElse { ByteArray(0) }
    }
}

fun String.hexStrToBytes(): ByteArray {
    return HexUtil.hexStrToBytes(this)
}

fun ByteArray.bytesToHexStr(uppercase: Boolean = false): String {
    return HexUtil.bytesToHexStr(this, uppercase)
}