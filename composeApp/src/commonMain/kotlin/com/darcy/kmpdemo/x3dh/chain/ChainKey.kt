package com.darcy.kmpdemo.x3dh.chain

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

    fun getKey(): ByteArray {
        return key
    }

    fun getIndex(): Long {
        return index
    }

    fun getNextChainKey(): ChainKey {
        val nextKey = HMAC1.getBaseMaterial(key, CHAIN_KEY_SEED)
        return ChainKey(kdf, nextKey, index + 1)
    }

    fun getMessageKeys(): ByteArray {
        val triple: Triple<ByteArray, ByteArray, ByteArray> = getMessageKeyTriple()
        val messageKey: ByteArray = triple.first
        val macKey: ByteArray = triple.second
        val iv: ByteArray = triple.third
        return messageKey
    }

    fun getMessageKeyTriple(): Triple<ByteArray, ByteArray, ByteArray> {
        val inputKeyMaterial = HMAC1.getBaseMaterial(key, MESSAGE_KEY_SEED)
        val keyMaterialBytes: ByteArray =
            kdf.deriveSecrets(
                inputKeyMaterial,
                ByteArray(32),
                "WhisperMessageKeys".encodeToByteArray(),
                80
            )
        val triple: Triple<ByteArray, ByteArray, ByteArray> =
            EncryptUtil.splitArray80(keyMaterialBytes, 32, 32, 16)
        return triple
    }

}