package com.darcy.kmpdemo.x3dh.chain

import dev.whyoleg.cryptography.BinarySize.Companion.bytes
import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.algorithms.HKDF
import dev.whyoleg.cryptography.algorithms.SHA256
import kotlinx.coroutines.runBlocking

class HKDF1 {
    companion object {
        private const val HASH_OUTPUT_SIZE: Int = 32
    }

    private val provider = CryptographyProvider.Default

    fun deriveSecrets(
        inputKeyMaterial: ByteArray,
        salt: ByteArray,
        info: ByteArray?,
        outputLength: Int
    ): ByteArray {
        return runBlocking {
            val hkdf = provider.get(HKDF)
            val derivation = hkdf.secretDerivation(
                digest = SHA256,
                outputSize = outputLength.bytes,
                salt = salt,
                info = info ?: ByteArray(0),
            )
            derivation.deriveSecret(inputKeyMaterial).toByteArray()
        }
    }
}