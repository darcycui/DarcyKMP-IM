package com.darcy.kmpdemo.utils

import com.darcy.kmpdemo.log.logD
import org.kotlincrypto.hash.sha2.SHA256

object HashHelper {
    fun sha256Str(original: String): String {
        val digest = sha256ByteArray(original.encodeToByteArray())
        return HexHelper.bytesToHexStr(digest)
    }

    fun sha256ByteArray(byteArray: ByteArray): ByteArray {
        println("original bytearray: ${byteArray.contentToString()}")
        if (byteArray.isEmpty()) return ByteArray(0)
        val sha256 = SHA256()
        sha256.update(byteArray)
        return sha256.digest()
    }
}