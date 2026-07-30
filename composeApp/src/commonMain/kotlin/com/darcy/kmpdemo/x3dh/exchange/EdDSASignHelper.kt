package com.darcy.kmpdemo.x3dh.exchange

import com.darcy.kmpdemo.platform.KotlinCryptoPlatform
import dev.whyoleg.cryptography.algorithms.EdDSA
//import kotlinx.coroutines.runBlocking
import org.kotlincrypto.error.InvalidKeyException
import org.kotlincrypto.error.SignatureException

object EdDSASignHelper {
    private const val ALGORITHM_EDDSA: String = "Ed25519" // 椭圆曲线算法 EdDSA
    private const val ALGORITHM_SIGN: String = "Ed25519" // 签名算法

    private val provider = KotlinCryptoPlatform.getCryptographyProvider()

    suspend fun generateKeyPairEdDSA(): EdDSA.KeyPair {

        val edDSA = provider.get(EdDSA)
        val curve = EdDSA.Curve.Ed25519
        return edDSA.keyPairGenerator(curve).generateKey()

    }

    suspend fun sign(data: ByteArray?, privateKey: EdDSA.PrivateKey?): ByteArray {
        if (data == null || privateKey == null) {
            throw IllegalArgumentException("data or privateKey is null")
        }

        try {
            return privateKey.signatureGenerator().generateSignature(data)
        } catch (e: InvalidKeyException) {
            e.printStackTrace()
            throw e
        } catch (e: SignatureException) {
            e.printStackTrace()
            throw e
        }
    }

    suspend fun verify(data: ByteArray?, sign: ByteArray?, publicKey: EdDSA.PublicKey?): Boolean {
        if (data == null || sign == null || publicKey == null) {
            return false
        }

        try {
            publicKey.signatureVerifier().verifySignature(data, sign)
            return true
        } catch (e: InvalidKeyException) {
            e.printStackTrace()

        } catch (e: SignatureException) {
            e.printStackTrace()
        }
        return false
    }

}