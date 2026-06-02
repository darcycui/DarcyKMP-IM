package com.darcy.kmpdemo.crypto.message

import com.darcy.kmpdemo.log.logE
import com.darcy.kmpdemo.platform.KotlinCryptoPlatform
import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.DelicateCryptographyApi
import dev.whyoleg.cryptography.algorithms.AES

object MessageCipher {
    private const val TAG = "MessageCipher"
    private val provider: CryptographyProvider = KotlinCryptoPlatform.getCryptographyProvider()

    /**
     * AES-GCM 加密
     * @param content 明文内容
     * @param key 加密密钥（32字节用于AES-256-GCM）
     * @param iv 初始化向量（12字节推荐）
     * @param add 附加认证数据（AAD），可选
     * @return 密文（包含认证标签）
     */
    @OptIn(DelicateCryptographyApi::class)
    suspend fun encrypt(
        content: ByteArray,
        key: ByteArray,
        iv: ByteArray,
        add: ByteArray
    ): ByteArray {
        return runCatching {
            val aesGcm = provider.get(AES.GCM)
//                val newKey = aesGcm.keyGenerator().generateKey()
            // 使用 指定key 创建密钥
            val newKey = aesGcm.keyDecoder().decodeFromByteArray(
                AES.Key.Format.RAW, key
            )
            val cipher = newKey.cipher()
            val data = cipher.encryptWithIv(iv, content, add)
            data
        }.onFailure {
            logE("$TAG 加密失败: ${it::class.simpleName} ${it.message}")
        }.getOrElse { byteArrayOf() }
    }

    /**
     * AES-GCM 解密
     * @param content 密文内容
     * @param key 解密密钥（32字节用于AES-256-GCM）
     * @param iv 密文初始化向量（12字节推荐）
     * @param add 密文附加认证数据（AAD），可选
     * @return 明文
     */
    @OptIn(DelicateCryptographyApi::class)
    suspend fun decrypt(
        content: ByteArray,
        key: ByteArray,
        iv: ByteArray,
        add: ByteArray
    ): ByteArray {
        return runCatching {
            val aesGcm = provider.get(AES.GCM)
//                val newKey = aesGcm.keyGenerator().generateKey()
            // 使用 指定key 创建密钥
            val newKey = aesGcm.keyDecoder().decodeFromByteArray(
                AES.Key.Format.RAW, key
            )
            val cipher = newKey.cipher()
            val data = cipher.decryptWithIv(iv, content, add)
            data
        }.onFailure {
            logE("$TAG 解密失败: ${it::class.simpleName} ${it.message}")
        }.getOrElse { byteArrayOf() }
    }
}