package com.darcy.kmpdemo.crypto

import com.darcy.kmpdemo.crypto.file.FileCipher
import com.darcy.kmpdemo.utils.FileHelper
import io.ktor.utils.io.core.toByteArray
import kotlinx.coroutines.test.runTest
//import kotlinx.coroutines.runBlocking
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.time.Clock

class FileCipherTest {

    @Test
    fun `test-aes-ctr-256`() {
        runTest {
            val message = "hello world".toByteArray()
            println("message: ${message.toHexString()}")
            val key = "1234567890abcdef1234567890abcdef".toByteArray() // 32字节 = AES-256
            val iv = "1234567890abcdef".toByteArray() // 16字节

            val encrypted = FileCipher.encrypt(message, key, iv)
            println("encrypted: ${encrypted.toHexString()}")

            val decrypted = FileCipher.decrypt(encrypted, key, iv)
            println("decrypted: ${decrypted.toHexString()}")

            assertContentEquals(message, decrypted, "AES-256-CTR 加密解密失败")
        }
    }

    @Test
    fun `test-aes-ctr-stream-encrypt-decrypt-large-file`() {
        runTest {

            val testDir = Path("file/test_stream_large")
            FileHelper.createDirectories(testDir)

            val plainPath = Path(testDir, "large_plain.bin")
            val encryptedPath = Path(testDir, "large_encrypted.bin")
            val decryptedPath = Path(testDir, "large_decrypted.bin")

            // 生成较大的测试数据（1MB）
            val largeData = ByteArray(1024 * 1024) { i -> (i % 256).toByte() }
//            val largeData = ByteArray(256) { i -> (i % 256).toByte() }
            FileHelper.writeFileByArrayBuffered(plainPath, largeData)
            println("原始文件大小: ${largeData.size / 1024} KB")

            val key = "1234567890abcdef1234567890abcdef".toByteArray()
            val iv = "1234567890abcdef".toByteArray()

            // 流式加密大文件
            val encryptStart = Clock.System.now().toEpochMilliseconds()
            SystemFileSystem.source(plainPath).buffered().use { source ->
                SystemFileSystem.sink(encryptedPath).buffered().use { sink ->
                    FileCipher.encryptStream(source, sink, key, iv)
                }
            }
            val encryptTime = Clock.System.now().toEpochMilliseconds() - encryptStart
            println("大文件加密耗时: ${encryptTime}ms")

            // 流式解密大文件
            val decryptStart = Clock.System.now().toEpochMilliseconds()
            SystemFileSystem.source(encryptedPath).buffered().use { source ->
                SystemFileSystem.sink(decryptedPath).buffered().use { sink ->
                    FileCipher.decryptStream(source, sink, key, iv)
                }
            }
            val decryptTime = Clock.System.now().toEpochMilliseconds() - decryptStart
            println("大文件解密耗时: ${decryptTime}ms")

            // 验证
            var decryptedBytes: ByteArray = ByteArray(0)
            FileHelper.readFileBuffered(decryptedPath) {
                decryptedBytes += it
            }

            assertContentEquals(largeData, decryptedBytes, "大文件流式加密解密失败")
            println("大文件流式加密解密测试成功! 总耗时: ${encryptTime + decryptTime}ms")

            // 清理
            runCatching {
                SystemFileSystem.delete(plainPath)
                SystemFileSystem.delete(encryptedPath)
                SystemFileSystem.delete(decryptedPath)
                SystemFileSystem.delete(testDir)
            }
        }
    }

}
