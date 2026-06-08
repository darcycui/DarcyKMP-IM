package com.darcy.kmpdemo.crypto.transport

import com.darcy.kmpdemo.log.logD
import com.darcy.kmpdemo.log.logE
import com.darcy.kmpdemo.log.logW
import com.darcy.kmpdemo.platform.KotlinCryptoPlatform
import com.darcy.kmpdemo.storage.memory.TransportGlobalStorage
import com.darcy.kmpdemo.utils.bytesToHexStr
import com.darcy.kmpdemo.utils.hexStrToBytes
import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.DelicateCryptographyApi
import dev.whyoleg.cryptography.algorithms.ChaCha20Poly1305
import kotlin.math.log

object TransportCipher {
    private const val TAG = "TransportCipher"
    private val provider: CryptographyProvider = KotlinCryptoPlatform.getCryptographyProvider()
    private const val IV_LENGTH = 12

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
        key: ByteArray = TransportGlobalStorage.getServerSharedSecretKey().hexStrToBytes(),
        nonce: ByteArray = RandomHelper.secureRandomIV(12),
        aad: ByteArray
    ): ByteArray {
        return runCatching {
            val chacha20 = provider.get(ChaCha20Poly1305)
            logW("$TAG 加密...")
            logD("$TAG 明文:${content.decodeToString()}")
            logD("$TAG 加密key:${key.bytesToHexStr()}")
            logD("$TAG 加密aad:${aad.bytesToHexStr()}")
            logD("$TAG 加密nonce:${nonce.bytesToHexStr()}")
            val newKey =
                chacha20.keyDecoder().decodeFromByteArray(
                    ChaCha20Poly1305.Key.Format.RAW, key
                )
            val cipher = newKey.cipher()
            val data = cipher.encryptWithIv(nonce, content, aad)
            logD("$TAG 加密后data:${data.bytesToHexStr()}")
            nonce + data
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
        key: ByteArray = TransportGlobalStorage.getServerSharedSecretKey().hexStrToBytes(),
        aad: ByteArray
    ): ByteArray {
        return runCatching {
            logW("$TAG 解密...")
            logD("$TAG 密文:${content.bytesToHexStr()}")
            logD("$TAG 解密key:${key.bytesToHexStr()}")
            logD("$TAG 解密aad:${aad.bytesToHexStr()}")
            val chacha20 = provider.get(ChaCha20Poly1305)
            val newKey = chacha20.keyDecoder().decodeFromByteArray(
                ChaCha20Poly1305.Key.Format.RAW, key
            )
            val cipher = newKey.cipher()
            val nonce = content.copyOfRange(0, IV_LENGTH)
            logD("$TAG 解密nonce:${nonce.bytesToHexStr()}")
            val ciphertext = content.copyOfRange(IV_LENGTH, content.size)
            val data = cipher.decryptWithIv(nonce, ciphertext, aad)
            logD("$TAG 解密后data:${data.decodeToString()}")
            data
        }.onFailure {
            logE("$TAG 解密失败: ${it::class.simpleName} ${it.message}")
            it.printStackTrace()
        }.getOrElse { byteArrayOf() }
    }
}