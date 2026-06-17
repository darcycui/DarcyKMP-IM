package com.darcy.kmpdemo.crypto

import com.darcy.kmpdemo.crypto.message.MessageCipher
import com.darcy.kmpdemo.log.DarcyLogger
import io.ktor.utils.io.core.toByteArray
import kotlinx.coroutines.runBlocking
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals

class MessageCipherTest {
    @BeforeTest
    fun setUp() {
        println("setUp")
        DarcyLogger.initLogger()
    }

    @Test
    fun `test-aes-gcm-cipher`() {
        runBlocking {
            val message = "hello world".toByteArray()
            println("明文: ${message.toHexString()}")
            val key = "1234567890abcdef1234567890abcdef".toByteArray()
            println("密钥: ${key.toHexString()}")
            val iv = "1234567890ab".toByteArray()
            val add = "additional data".toByteArray()
            val macKey = "abcdef1234567890abcdef1234567890".toByteArray()
            val messageCipher = MessageCipher
            val encrypted = messageCipher.encrypt(message, key, iv, add, macKey)
            println("encrypted: ${encrypted.toHexString()}")
            val decrypted = messageCipher.decrypt(encrypted, key, iv, add, macKey)
            println("decrypted: ${decrypted.toHexString()}")
            assertContentEquals(message, decrypted, "AES-GCM 加密解密失败")
        }
    }
}