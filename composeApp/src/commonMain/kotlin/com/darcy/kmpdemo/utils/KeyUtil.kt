package com.darcy.kmpdemo.utils

import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.algorithms.XDH
import kotlinx.coroutines.runBlocking

object KeyUtil {
    private val provider = CryptographyProvider.Default
    private val xdh = provider.get(XDH)
    fun bytesToPrivateKey(privateKeyBytes: ByteArray): XDH.PrivateKey {
        println("privateKeyBytes长度: ${privateKeyBytes.size}")
        return runBlocking {
            val curve = XDH.Curve.X25519
            // 使用 RAW 格式解码私钥
            xdh.privateKeyDecoder(curve)
                .decodeFromByteArray(XDH.PrivateKey.Format.RAW, privateKeyBytes)
        }
    }

    fun bytesToPublicKey(publicKeyBytes: ByteArray): XDH.PublicKey {
        println("publicKeyBytes长度: ${publicKeyBytes.size}")
        return runBlocking {
            val curve = XDH.Curve.X25519
            // 使用 RAW 格式解码公钥
            xdh.publicKeyDecoder(curve)
                .decodeFromByteArray(XDH.PublicKey.Format.RAW, publicKeyBytes)
        }
    }

    /**
     * 将私钥转换为字节数组（RAW 格式）
     */
    fun privateKeyToBytes(privateKey: XDH.PrivateKey): ByteArray {
        return runBlocking {
            privateKey.encodeToByteArray(XDH.PrivateKey.Format.RAW)
        }
    }

    /**
     * 将公钥转换为字节数组（RAW 格式）
     */
    fun publicKeyToBytes(publicKey: XDH.PublicKey): ByteArray {
        return runBlocking {
            publicKey.encodeToByteArray(XDH.PublicKey.Format.RAW)
        }
    }
}

fun ByteArray.toPrivateKey(): XDH.PrivateKey {
    return KeyUtil.bytesToPrivateKey(this)
}

fun ByteArray.toPublicKey(): XDH.PublicKey {
    return KeyUtil.bytesToPublicKey(this)
}

fun XDH.PrivateKey.toBytes(): ByteArray {
    return KeyUtil.privateKeyToBytes(this)
}

fun XDH.PublicKey.toBytes(): ByteArray {
    return KeyUtil.publicKeyToBytes(this)
}
