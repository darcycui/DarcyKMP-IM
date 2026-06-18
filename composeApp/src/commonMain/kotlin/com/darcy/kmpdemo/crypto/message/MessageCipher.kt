package com.darcy.kmpdemo.crypto.message

import com.darcy.kmpdemo.crypto.hmac.HMAC1
import com.darcy.kmpdemo.log.logD
import com.darcy.kmpdemo.log.logE
import com.darcy.kmpdemo.log.logI
import com.darcy.kmpdemo.log.logV
import com.darcy.kmpdemo.log.logW
import com.darcy.kmpdemo.platform.KotlinCryptoPlatform
import com.darcy.kmpdemo.utils.bytesToHexStr
import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.DelicateCryptographyApi
import dev.whyoleg.cryptography.algorithms.AES

object MessageCipher {
    private const val TAG = "MessageCipher"
    private val provider: CryptographyProvider = KotlinCryptoPlatform.getCryptographyProvider()
    private const val IV_LENGTH = 12
    private const val HMAC_LENGTH = 32

    /**
     * AES-GCM 加密
     * @param data 明文内容
     * @param key 加密密钥（32字节用于AES-256-GCM）
     * @param nonce 初始化向量（12字节推荐）
     * @param aad 附加认证数据（AAD），可选
     * @return 密文（hmac + content）
     */
    @OptIn(DelicateCryptographyApi::class)
    suspend fun encrypt(
        data: ByteArray,
        key: ByteArray,
        nonce: ByteArray,
        aad: ByteArray,
        macKey: ByteArray,
    ): ByteArray {
        return runCatching {
            logW("$TAG 加密...")
            logI("$TAG 明文:${data.decodeToString()} 长度=${data.size}")
            logD("$TAG 加密key:${key.bytesToHexStr()}")
            logD("$TAG 加密nonce:${nonce.bytesToHexStr()}")
            logD("$TAG 加密aad:${aad.bytesToHexStr()}")
            logD("$TAG 加密macKey:${macKey.bytesToHexStr()}")
            val aesGcm = provider.get(AES.GCM)
//                val newKey = aesGcm.keyGenerator().generateKey()
            // 使用 指定key 创建密钥
            val newKey = aesGcm.keyDecoder().decodeFromByteArray(
                AES.Key.Format.RAW, key
            )
            val cipher = newKey.cipher()
            val ciphertext = cipher.encryptWithIv(nonce, data, aad)
            logV("$TAG 加密后content:${ciphertext.bytesToHexStr()}")
            val hmac = HMAC1.hmacSignature(macKey, nonce + ciphertext)
            logV("$TAG 加密时hmac:${hmac.bytesToHexStr()}")
            hmac + ciphertext

        }.onFailure {
            logE("$TAG 加密失败: ${it::class.simpleName} ${it.message}")
        }.getOrElse { byteArrayOf() }
    }

    /**
     * AES-GCM 解密
     * @param data 密文（hmac + content）
     * @param key 解密密钥（32字节用于AES-256-GCM）
     * @param nonce 密文初始化向量（12字节推荐）
     * @param aad 密文附加认证数据（AAD），可选
     * @return 明文
     */
    @OptIn(DelicateCryptographyApi::class)
    suspend fun decrypt(
        data: ByteArray,
        key: ByteArray,
        nonce: ByteArray,
        aad: ByteArray,
        macKey: ByteArray
    ): ByteArray {
        return runCatching {
            logW("$TAG 解密...")
            logV("$TAG 密文:${data.bytesToHexStr()}")
            logD("$TAG 解密key:${key.bytesToHexStr()}")
            logD("$TAG 解密nonce:${nonce.bytesToHexStr()}")
            logD("$TAG 解密aad:${aad.bytesToHexStr()}")
            logD("$TAG 解密macKey:${macKey.bytesToHexStr()}")
            val aesGcm = provider.get(AES.GCM)
//                val newKey = aesGcm.keyGenerator().generateKey()
            // 使用 指定key 创建密钥
            val newKey = aesGcm.keyDecoder().decodeFromByteArray(
                AES.Key.Format.RAW, key
            )
            val cipher = newKey.cipher()
            val expectedHmac = data.copyOfRange(0, HMAC_LENGTH)
            logD("$TAG 解密时expectedHmac:${expectedHmac.bytesToHexStr()}")
            val ciphertext = data.copyOfRange(HMAC_LENGTH, data.size)
            if (HMAC1.hmacSignatureVerify(macKey, nonce + ciphertext, expectedHmac)) {
                logD("$TAG HMAC 校验成功")
                val plaintext = cipher.decryptWithIv(nonce, ciphertext, aad)
                logI("$TAG 解密后content:${plaintext.decodeToString()} 长度=${plaintext.size}")
                plaintext
            } else {
                logE("$TAG HMAC 校验失败")
                byteArrayOf()
            }
        }.onFailure {
            logE("$TAG 解密失败: ${it::class.simpleName} ${it.message}")
        }.getOrElse { byteArrayOf() }
    }
}