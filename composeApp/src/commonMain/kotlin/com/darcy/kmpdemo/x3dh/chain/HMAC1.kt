package com.darcy.kmpdemo.x3dh.chain

import com.darcy.kmpdemo.platform.KotlinCryptoPlatform
import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.algorithms.SHA256
import kotlinx.coroutines.runBlocking
import kotlin.experimental.xor

object HMAC1 {
    private val provider = KotlinCryptoPlatform.getCryptographyProvider()

    fun getBaseMaterial(key: ByteArray, seed: ByteArray): ByteArray {
        return runBlocking {
            // 使用手动计算的HMAC
            computeHmacSHA256(key, seed)
        }
    }

    /**
     * cryptography-kotlin 库不支持从指定密钥 key生成 HMAC，这里需要手动计算 HMAC-SHA256
     * 基于 RFC 2104 标准实现
     *
     * @param key 密钥
     * @param data 数据
     * @return HMAC-SHA256 结果（32字节）
     */
    private suspend fun computeHmacSHA256(key: ByteArray, data: ByteArray): ByteArray {
        val blockSize = 64 // SHA-256 的块大小为 64 字节

        // 步骤 1: 如果密钥长度大于块大小，先对密钥进行哈希
        val actualKey = if (key.size > blockSize) {
            val sha256 = provider.get(SHA256).hasher()
            sha256.hash(key)
        } else {
            key
        }

        // 步骤 2: 将密钥填充到块大小
        val paddedKey = ByteArray(blockSize)
        actualKey.copyInto(paddedKey)

        // 步骤 3: 创建 ipad (inner padding) 和 opad (outer padding)
        val ipad = ByteArray(blockSize) { i -> paddedKey[i] xor 0x36.toByte() }
        val opad = ByteArray(blockSize) { i -> paddedKey[i] xor 0x5c.toByte() }

        // 步骤 4: 内部哈希: H((K ^ ipad) || data)
        val sha256 = provider.get(SHA256).hasher()
        val innerData = ipad + data
        val innerHash = sha256.hash(innerData)

        // 步骤 5: 外部哈希: H((K ^ opad) || innerHash)
        val outerData = opad + innerHash
        val finalHash = sha256.hash(outerData)

        return finalHash
    }

}