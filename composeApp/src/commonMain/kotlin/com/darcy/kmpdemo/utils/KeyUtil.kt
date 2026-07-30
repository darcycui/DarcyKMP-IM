package com.darcy.kmpdemo.utils

import com.darcy.kmpdemo.log.logD
import com.darcy.kmpdemo.platform.KotlinCryptoPlatform
import dev.whyoleg.cryptography.algorithms.XDH

//import kotlinx.coroutines.runBlocking

object KeyUtil {
    private val provider = KotlinCryptoPlatform.getCryptographyProvider()
    private val xdh = provider.get(XDH)
    suspend fun bytesToPrivateKey(privateKeyBytes: ByteArray): XDH.PrivateKey {
        logD("privateKeyBytes长度: ${privateKeyBytes.size}")
        val curve = XDH.Curve.X25519
        // 使用 RAW 格式解码私钥
        return xdh.privateKeyDecoder(curve)
            .decodeFromByteArray(XDH.PrivateKey.Format.RAW, privateKeyBytes)
    }

    suspend fun bytesToPublicKey(publicKeyBytes: ByteArray): XDH.PublicKey {
        logD("publicKeyBytes长度: ${publicKeyBytes.size}")
        val curve = XDH.Curve.X25519
        // 使用 RAW 格式解码公钥
        return xdh.publicKeyDecoder(curve)
            .decodeFromByteArray(XDH.PublicKey.Format.RAW, publicKeyBytes)
    }

    /**
     * 将私钥转换为字节数组（RAW 格式）
     */
    suspend fun privateKeyToBytes(privateKey: XDH.PrivateKey): ByteArray {
        return privateKey.encodeToByteArray(XDH.PrivateKey.Format.RAW)
    }

    /**
     * 将公钥转换为字节数组（RAW 格式）
     */
    suspend fun publicKeyToBytes(publicKey: XDH.PublicKey): ByteArray {
        return publicKey.encodeToByteArray(XDH.PublicKey.Format.RAW)
    }
}

suspend fun ByteArray.toPrivateKey(): XDH.PrivateKey {
    return KeyUtil.bytesToPrivateKey(this)
}

suspend fun ByteArray.toPublicKey(): XDH.PublicKey {
    return KeyUtil.bytesToPublicKey(this)
}

suspend fun XDH.PrivateKey.toBytes(): ByteArray {
    return KeyUtil.privateKeyToBytes(this)
}

suspend fun XDH.PublicKey.toBytes(): ByteArray {
    return KeyUtil.publicKeyToBytes(this)
}
