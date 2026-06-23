package com.darcy.kmpdemo.crypto

import com.darcy.kmpdemo.crypto.transport.TransportCipherAESGCM
import com.darcy.kmpdemo.crypto.transport.TransportCipherChaCha20
import com.darcy.kmpdemo.utils.bytesToHexStr
import io.ktor.utils.io.core.toByteArray
//import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals

class TransportCipherGCMTest {
    @Test
    fun `test-chacha20-cipher`() {
        runTest {
            val message = "hello world".toByteArray()
            println("message: ${message.bytesToHexStr()}")

            // ChaCha20 需要 32 字节密钥
            val key = "1234567890abcdef1234567890abcdef".toByteArray()
            println("key: ${key.bytesToHexStr()}")

            // Nonce 通常使用 12 字节
            val nonce = "1234567890ab".toByteArray()
            val aad = "additional data".toByteArray()

            val encrypted = TransportCipherChaCha20.encrypt(message, key, nonce, aad)
            println("encrypted: ${encrypted.bytesToHexStr()}")

            val decrypted = TransportCipherChaCha20.decrypt(encrypted, key, aad)
            println("decrypted: ${decrypted.bytesToHexStr()}")

            assertContentEquals(message, decrypted, "ChaCha20 加密解密失败")
        }
    }

    @Test
    fun `test-aes-gcm-cipher`() {
        runTest {
            val message = "hello world".toByteArray()
            println("message: ${message.bytesToHexStr()}")

            // AES-256-GCM 需要 32 字节密钥
            val key = "1234567890abcdef1234567890abcdef".toByteArray()
            println("key: ${key.bytesToHexStr()}")

            // Nonce 通常使用 12 字节
            val nonce = "1234567890ab".toByteArray()
            val aad = "additional data".toByteArray()

            val encrypted = TransportCipherAESGCM.encrypt(message, key, nonce, aad)
            println("encrypted: ${encrypted.bytesToHexStr()}")

            val decrypted = TransportCipherAESGCM.decrypt(encrypted, key, aad)
            println("decrypted: ${decrypted.bytesToHexStr()}")

            assertContentEquals(message, decrypted, "AES-256-GCM 加密解密失败")
        }
    }
}
