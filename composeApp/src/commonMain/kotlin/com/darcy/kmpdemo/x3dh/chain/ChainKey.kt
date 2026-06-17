package com.darcy.kmpdemo.x3dh.chain

import com.darcy.kmpdemo.crypto.hmac.HMAC1
import com.darcy.kmpdemo.log.logD
import com.darcy.kmpdemo.utils.EncryptUtil

class ChainKey(
    private var kdf: HKDF1,
    private var key: ByteArray,
    private var index: Long,
) {
    companion object {
        private val MESSAGE_KEY_SEED: ByteArray = byteArrayOf(0x01)
        private val CHAIN_KEY_SEED: ByteArray = byteArrayOf(0x02)
    }

    init {
        logD("构造ChainKey: key=${key.toHexString()} index=$index")
    }

    fun getKey(): ByteArray {
        return key
    }

    fun getIndex(): Long {
        return index
    }

    suspend fun getNextChainKey(): ChainKey {
        val nextKey = HMAC1.hmacSignature(key, CHAIN_KEY_SEED)
        return ChainKey(kdf, nextKey, index + 1)
    }

    suspend fun getMessageKey(): ByteArray {
        val triple: Triple<ByteArray, ByteArray, ByteArray> = getMessageKeyTriple()
        val messageKey: ByteArray = triple.first
        val macKey: ByteArray = triple.second
        val iv: ByteArray = triple.third
        return messageKey
    }

    /**
     * 获取消息密钥
     * @return Triple<ByteArray, ByteArray, ByteArray>
     *     messageKey: ByteArray 消息密钥 32字节
     *     macKey: ByteArray 消息密钥的MAC密钥 32字节
     *     iv: ByteArray 密钥的初始向量 16字节
     */
    suspend fun getMessageKeyTriple(): Triple<ByteArray, ByteArray, ByteArray> {
        val inputKeyMaterial = HMAC1.hmacSignature(key, MESSAGE_KEY_SEED)
        val keyMaterialBytes: ByteArray =
            kdf.deriveSecrets(
                inputKeyMaterial,
                ByteArray(32),
                "WhisperMessageKeys".encodeToByteArray(),
                76
            )
        val triple: Triple<ByteArray, ByteArray, ByteArray> =
            EncryptUtil.splitArray76(keyMaterialBytes, 32, 32, 12)
        return triple
    }

}