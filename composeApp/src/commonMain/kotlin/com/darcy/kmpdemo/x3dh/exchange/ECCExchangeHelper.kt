package com.darcy.kmpdemo.x3dh.exchange

import com.darcy.kmpdemo.log.logV
import com.darcy.kmpdemo.platform.KotlinCryptoPlatform
import com.darcy.kmpdemo.utils.bytesToHexStr
import com.darcy.kmpdemo.utils.toBytes
import dev.whyoleg.cryptography.algorithms.XDH
import kotlinx.coroutines.runBlocking


object ECCExchangeHelper {
    // 初始化 指定使用 X25519 曲线
    const val ALGORITHM: String = "X25519"
    private val provider = KotlinCryptoPlatform.getCryptographyProvider()
    fun generateKeyPair(): XDH.KeyPair {
        return runBlocking {
            val xdh = provider.get(XDH)
            val curve = XDH.Curve.X25519
            xdh.keyPairGenerator(curve).generateKey()
        }
    }

    fun getSharedSecret(privateKey: XDH.PrivateKey?, publicKey: XDH.PublicKey?): ByteArray {
        if (privateKey == null || publicKey == null) {
            throw IllegalArgumentException("privateKey or publicKey is null")
        }
        return runBlocking {
            val sharedKey = privateKey.sharedSecretGenerator().generateSharedSecret(publicKey).toByteArray()
//            logV("getSharedSecret: privateKey=${privateKey.toBytes().bytesToHexStr()}")
//            logV("getSharedSecret: publicKey=${publicKey.toBytes().bytesToHexStr()}")
//            logV("getSharedSecret: sharedKey=${sharedKey.toHexString()}")
            sharedKey
        }
    }

}