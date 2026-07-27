package com.darcy.kmpdemo.crypto

import com.darcy.kmpdemo.crypto.transport.TransportCipherAESGCM
import com.darcy.kmpdemo.log.logW
import com.darcy.kmpdemo.network.http.impl.ktor.EncryptBodyConfig
import com.darcy.kmpdemo.utils.bytesToHexStr
import com.darcy.kmpdemo.utils.hexStrToBytes
import io.ktor.utils.io.charsets.Charsets
import io.ktor.utils.io.core.toByteArray

object JsonCryptoHelper {
    private const val TAG = "JsonCryptoHelper"

    /**
     * 去除 URL 的 scheme（http://、https://、ws://、wss://），使 AAD 不受协议变更影响
     */
    private fun stripScheme(url: String): String {
        val schemeEnd = url.indexOf("://")
        return if (schemeEnd != -1) url.substring(schemeEnd + 3) else url
    }

    suspend fun encryptHttpJson(originalJson: String, url: String): String {
        val encryptText = TransportCipherAESGCM.encrypt(
            content = originalJson.toByteArray(Charsets.UTF_8),
            aad = "POST:${stripScheme(url)}".toByteArray()
        )
        return encryptText.bytesToHexStr()
    }

    suspend fun decryptHttpJson(originalJson: String, url: String): String {
        val json = originalJson.removeSurroundingQuotes()
        if (EncryptBodyConfig.isEnabled()) {
            logW("$TAG 解密json")
            // 解密 result 字段
            val decryptedResult = TransportCipherAESGCM.decrypt(
                content = json.hexStrToBytes(),
                aad = "POST:${stripScheme(url)}".toByteArray()
            )
            return decryptedResult.decodeToString()
        } else {
            logW("$TAG 无需解密json")
            return json
        }
    }

    suspend fun encryptWebsocketJson(message: String, url: String): String {
        val encryptedMessage = TransportCipherAESGCM.encrypt(
            content = message.toByteArray(),
            aad = "WS:${stripScheme(url)}".toByteArray()
        )
        return encryptedMessage.toHexString().also {
            logW("$TAG 加密后长度:${it.length}")
        }
    }

    suspend fun decryptWebsocketJson(message: String, url: String): String {
        val decryptedMessage = TransportCipherAESGCM.decrypt(
            content = message.hexStrToBytes(),
            aad = "WS:${stripScheme(url)}".toByteArray()
        )
        return decryptedMessage.decodeToString()
    }

}

/**
 * 去除字符串开头和结尾的双引号 (")
 * 安全处理 null、空字符串、长度不够以及内部包含引号的情况
 */
fun String?.removeSurroundingQuotes(): String {
    // 1. 处理 null 或空字符串
    if (this.isNullOrEmpty()) return ""

    // 2. 长度不够 2 的情况（单个字符或空串，不可能形成一对引号）
    if (this.length < 2) return this

    // 3. 判断首尾是否同时为双引号
    if (this.first() == '"' && this.last() == '"') {
        // substring 会安全处理索引，因为前面已经保证了 length >= 2
        return this.substring(1, this.length - 1)
    }

    // 4. 首尾不匹配双引号，原样返回
    return this
}