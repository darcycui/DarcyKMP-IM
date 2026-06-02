package com.darcy.kmpdemo.crypto.file

import com.darcy.kmpdemo.log.logE
import com.darcy.kmpdemo.platform.KotlinCryptoPlatform
import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.DelicateCryptographyApi
import dev.whyoleg.cryptography.algorithms.AES
import kotlinx.io.RawSink
import kotlinx.io.RawSource
import kotlinx.io.buffered

object FileCipher {
    private const val TAG = "FileCipher"
    private const val BUFFER_SIZE = 8192
    private val provider: CryptographyProvider = KotlinCryptoPlatform.getCryptographyProvider()

    /**
     * AES-CTR 加密
     * @param content 明文内容
     * @param key 加密密钥（16/24/32字节对应 AES-128/192/256）
     * @param iv 初始化向量（16字节）
     * @return 密文（不包含IV）
     */
    @OptIn(DelicateCryptographyApi::class)
    suspend fun encrypt(content: ByteArray, key: ByteArray, iv: ByteArray): ByteArray {
        return runCatching {
            val aesCtr = provider.get(AES.CTR)
            val newKey = aesCtr.keyDecoder().decodeFromByteArray(AES.Key.Format.RAW, key)
            val cipher = newKey.cipher()
            val data = cipher.encryptWithIv(iv, content)
            data
        }.onFailure {
            logE("$TAG 加密失败: ${it::class.simpleName} ${it.message}")
            it.printStackTrace()
            throw RuntimeException("AES-CTR encryption failed: ${it.message}", it)
        }.getOrElse {
            byteArrayOf()
        }
    }

    /**
     * AES-CTR 解密
     * @param content 密文内容
     * @param key 解密密钥（16/24/32字节对应AES-128/192/256）
     * @param iv 初始化向量（16字节）
     * @return 明文
     */
    @OptIn(DelicateCryptographyApi::class)
    suspend fun decrypt(content: ByteArray, key: ByteArray, iv: ByteArray): ByteArray {
        return runCatching {
            val aesCtr = provider.get(AES.CTR)
            val newKey = aesCtr.keyDecoder().decodeFromByteArray(AES.Key.Format.RAW, key)
            val cipher = newKey.cipher()
            val data = cipher.decryptWithIv(iv, content)
            data
        }.onFailure {
            logE("$TAG 解密失败: ${it::class.simpleName} ${it.message}")
            it.printStackTrace()
            throw RuntimeException("AES-CTR decryption failed: ${it.message}", it)
        }.getOrElse {
            byteArrayOf()
        }
    }

    /**
     * 使用 Sink模式 流式加密文件 (立即执行)
     * @param source 明文数据源
     * @param sink 密文输出目标
     * @param key 加密密钥（16/24/32字节对应AES-128/192/256）
     * @param iv 初始化向量（16字节）
     */
    @OptIn(DelicateCryptographyApi::class)
    suspend fun encryptStream(
        source: RawSource,
        sink: RawSink,
        key: ByteArray,
        iv: ByteArray
    ): Result<Boolean> {
        return runCatching {
            val aesCtr = provider.get(AES.CTR)
            val newKey = aesCtr.keyDecoder().decodeFromByteArray(AES.Key.Format.RAW, key)
            val cipher = newKey.cipher()

            // 使用流式 API：创建加密 Sink，将明文源转换为密文写入目标
            val encryptedSink = cipher.encryptingSinkWithIv(iv, sink)

            // 从源读取数据并通过加密 Sink 写入
            source.buffered().use { src ->
                encryptedSink.buffered().use { encSink ->
                    val buffer = ByteArray(BUFFER_SIZE)
                    while (!src.exhausted()) {
                        val bytesRead = src.readAtMostTo(
                            buffer, 0, buffer.size
                        )
                        if (bytesRead > 0) {
                            encSink.write(buffer, 0, bytesRead)
                        }
                    }
                    encSink.flush()
                }
            }
            true
        }.onFailure {
            logE("$TAG 解密失败: ${it::class.simpleName} ${it.message}")
            it.printStackTrace()
        }
    }

    /**
     * 使用 Sink模式 流式解密文件 (立即执行)
     * @param source 密文数据源
     * @param sink 明文输出目标
     * @param key 解密密钥（16/24/32字节对应 AES-128/192/256）
     * @param iv 初始化向量（16字节）
     */
    @OptIn(DelicateCryptographyApi::class)
    suspend fun decryptStream(
        source: RawSource,
        sink: RawSink,
        key: ByteArray,
        iv: ByteArray
    ): Result<Boolean> {
        return runCatching {
            val aesCtr = provider.get(AES.CTR)
            val newKey = aesCtr.keyDecoder().decodeFromByteArray(AES.Key.Format.RAW, key)
            val cipher = newKey.cipher()

            // 使用流式 API：创建解密 Sink，将密文源转换为明文写入目标
            val decryptedSink = cipher.decryptingSinkWithIv(iv, sink)

            // 从源读取数据并通过解密 Sink 写入
            source.buffered().use { src ->
                val buffer = ByteArray(BUFFER_SIZE)
                decryptedSink.buffered().use { decSink ->
                    while (!src.exhausted()) {
                        val bytesRead = src.readAtMostTo(
                            buffer, 0, buffer.size
                        )
                        if (bytesRead > 0) {
                            decSink.write(buffer, 0, bytesRead)
                        }
                    }
                    decSink.flush()
                }
            }
            true
        }.onFailure {
            logE("$TAG 解密失败: ${it::class.simpleName} ${it.message}")
            it.printStackTrace()
        }
    }
}
