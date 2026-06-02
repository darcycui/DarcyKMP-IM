package com.darcy.kmpdemo.crypto

import com.darcy.kmpdemo.crypto.message.MessageCipher
import io.ktor.utils.io.core.toByteArray
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertContentEquals

class MessageCipherTest {
    @Test
    fun `test-aes-gcm-cipher`() {
        runBlocking {
            val message = "hello world".toByteArray()
            println("message: ${message.toHexString()}")
            val key = "1234567890abcdef1234567890abcdef".toByteArray()
            println("key: ${key.toHexString()}")
            val iv = "1234567890ab".toByteArray()
            val add = "additional data".toByteArray()
            val messageCipher = MessageCipher
            val encrypted = messageCipher.encrypt(message, key, iv, add)
            println("encrypted: ${encrypted.toHexString()}")
            val decrypted = messageCipher.decrypt(encrypted, key, iv, add)
            println("decrypted: ${decrypted.toHexString()}")
            assertContentEquals(message, decrypted, "GCM 加密解密失败")
        }
    }
}