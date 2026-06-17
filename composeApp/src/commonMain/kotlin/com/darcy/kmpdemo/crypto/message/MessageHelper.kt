package com.darcy.kmpdemo.crypto.message

import com.darcy.kmpdemo.utils.bytesToHexStr
import com.darcy.kmpdemo.utils.hexStrToBytes
import com.darcy.kmpdemo.x3dh.MessageKey
import io.ktor.utils.io.core.toByteArray

object MessageHelper {
    suspend fun encryptContent(
        content: String,
        msgId: String,
        messageKeyLocal: MessageKey,
    ): String {
        val aad = "/private/$msgId"
        return MessageCipher.encrypt(
            data = content.toByteArray(),
            key = messageKeyLocal.messageKey.hexStrToBytes(),
            nonce = messageKeyLocal.iv.hexStrToBytes(),
            aad = aad.toByteArray(),
            macKey = messageKeyLocal.macKey.hexStrToBytes(),
        ).bytesToHexStr()
    }

    suspend fun decryptContent(
        content: String,
        msgId: String,
        messageKeyLocal: MessageKey,
    ): String {
        val aad = "/private/$msgId"
        return MessageCipher.decrypt(
            data = content.hexStrToBytes(),
            key = messageKeyLocal.messageKey.hexStrToBytes(),
            nonce = messageKeyLocal.iv.hexStrToBytes(),
            aad = aad.toByteArray(),
            macKey = messageKeyLocal.macKey.hexStrToBytes(),
        ).decodeToString()
    }
}