package com.darcy.kmpdemo.utils

object HexHelper {
    fun bytesToHexStr(bytes: ByteArray, uppercase: Boolean = false): String {
        if (bytes.isEmpty()) return ""
        return runCatching {
            val str = bytes.toHexString()
            if (uppercase) str.uppercase() else str.lowercase()
        }.onFailure {
            it.printStackTrace()
        }.getOrElse { "" }

    }

    fun hexStrToBytes(hex: String): ByteArray {
        if (hex.isEmpty()) return ByteArray(0)
        return runCatching {
            hex.hexToByteArray()
        }.onFailure {
            it.printStackTrace()
        }.getOrElse { ByteArray(0) }
    }
}