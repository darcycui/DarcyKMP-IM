package com.darcy.kmpdemo.crypto

import com.darcy.kmpdemo.crypto.transport.TransportCipher
import io.ktor.utils.io.core.toByteArray
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertContentEquals

class TransportCipherTest {
    @Test
    fun `test-chacha20-cipher`() {
        runBlocking {
            val message = "hello world".toByteArray()
            println("message: ${message.toHexString()}")

            // ChaCha20-Poly1305 需要 32 字节密钥
            val key = "1234567890abcdef1234567890abcdef".toByteArray()
            println("key: ${key.toHexString()}")

            // Nonce 通常使用 12 字节
            val nonce = "1234567890ab".toByteArray()
            val aad = "additional data".toByteArray()

            val encrypted = TransportCipher.encrypt(message, key, nonce, aad)
            println("encrypted: ${encrypted.toHexString()}")

            val decrypted = TransportCipher.decrypt(encrypted, key, nonce, aad)
            println("decrypted: ${decrypted.toHexString()}")

            assertContentEquals(message, decrypted, "ChaCha20-Poly1305 加密解密失败")
        }
    }
}
