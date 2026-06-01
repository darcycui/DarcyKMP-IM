package com.darcy.kmpdemo.crypto.message

import com.darcy.kmpdemo.platform.KotlinCryptoPlatform
import com.darcy.kmpdemo.utils.bytesToHexStr
import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.DelicateCryptographyApi
import dev.whyoleg.cryptography.algorithms.AES
import kotlinx.coroutines.runBlocking

object MessageCipher {
    private val provider: CryptographyProvider = KotlinCryptoPlatform.getCryptographyProvider()

    /**
     * AES-GCM 加密
     * @param content 明文内容
     * @param key 加密密钥（32字节用于AES-256-GCM）
     * @param iv 初始化向量（12字节推荐）
     * @param add 附加认证数据（AAD），可选
     * @return Base64编码的密文（包含认证标签）
     */
    @OptIn(DelicateCryptographyApi::class)
    fun encrypt(content: ByteArray, key: ByteArray, iv: ByteArray, add: ByteArray): String {
        return runBlocking {
            try {
                val aesGcm = provider.get(AES.GCM)
                val newKey = aesGcm.keyGenerator().generateKey() // todo 使用 key
                val cipher = newKey.cipher()
                val data = cipher.encryptWithIv(iv, content, add)
                data.bytesToHexStr()
            } catch (e: Exception) {
                e.printStackTrace()
                throw RuntimeException("GCM encryption failed: ${e.message}", e)
            }
        }
    }

    @OptIn(DelicateCryptographyApi::class)
    fun decrypt(content: ByteArray, key: ByteArray, iv: ByteArray, add: ByteArray): String {
        return runBlocking {
            try {
                val aesGcm = provider.get(AES.GCM)
                val newKey = aesGcm.keyGenerator().generateKey()
                val cipher = newKey.cipher()
                val data = cipher.decryptWithIv(iv, content, add)
                data.bytesToHexStr()
            } catch (e: Exception) {
                e.printStackTrace()
                throw RuntimeException("GCM decryption failed: ${e.message}", e)
            }
        }
    }
}