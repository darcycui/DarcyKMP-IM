package com.darcy.kmpdemo.crypto.transport

import com.darcy.kmpdemo.log.logE
import com.darcy.kmpdemo.platform.KotlinCryptoPlatform
import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.DelicateCryptographyApi
import dev.whyoleg.cryptography.algorithms.ChaCha20Poly1305

object TransportCipher {
    private const val TAG = "TransportCipher"
    private val provider: CryptographyProvider = KotlinCryptoPlatform.getCryptographyProvider()

    /**
     * ChaCha20-Poly1305 加密
     * @param content 明文内容
     * @param key 加密密钥（32字节）
     * @param nonce Nonce/IV（12字节推荐）
     * @param aad 附加认证数据（AAD），可选
     * @return 密文（包含认证标签，不包含nonce）
     */
    @OptIn(DelicateCryptographyApi::class)
    suspend fun encrypt(
        content: ByteArray,
        key: ByteArray,
        nonce: ByteArray,
        aad: ByteArray
    ): ByteArray {
        return runCatching {
            val chacha20 = provider.get(ChaCha20Poly1305)
            val newKey =
                chacha20.keyDecoder().decodeFromByteArray(
                    ChaCha20Poly1305.Key.Format.RAW, key
                )
            val cipher = newKey.cipher()
            val data = cipher.encryptWithIv(nonce, content, aad ?: byteArrayOf())
            data
        }.onFailure {
            logE("$TAG 加密失败: ${it::class.simpleName} ${it.message}")
        }.getOrElse { byteArrayOf() }
    }

    /**
     * ChaCha20-Poly1305 解密
     * @param content 密文内容（包含认证标签）
     * @param key 解密密钥（32字节）
     * @param nonce Nonce/IV（12字节）
     * @param aad 附加认证数据（AAD），可选
     * @return 明文
     */
    @OptIn(DelicateCryptographyApi::class)
    suspend fun decrypt(
        content: ByteArray,
        key: ByteArray,
        nonce: ByteArray,
        aad: ByteArray
    ): ByteArray {
        return runCatching {
            val chacha20 = provider.get(ChaCha20Poly1305)
            val newKey = chacha20.keyDecoder().decodeFromByteArray(
                ChaCha20Poly1305.Key.Format.RAW, key
            )
            val cipher = newKey.cipher()
            val data = cipher.decryptWithIv(nonce, content, aad ?: byteArrayOf())
            data
        }.onFailure {
            logE("$TAG 解密失败: ${it::class.simpleName} ${it.message}")
        }.getOrElse { byteArrayOf() }
    }
}