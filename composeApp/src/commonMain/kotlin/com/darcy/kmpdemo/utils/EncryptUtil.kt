package com.darcy.kmpdemo.utils

import com.darcy.kmpdemo.log.logD
import com.darcy.kmpdemo.log.logI
import dev.whyoleg.cryptography.algorithms.XDH

//import kotlinx.coroutines.runBlocking

object EncryptUtil {

    fun appendArrays(vararg arrays: ByteArray): ByteArray {
        var length = 0
        for (array in arrays) {
            length += array.size
        }
        val result = ByteArray(length)
        var pos = 0
        for (array in arrays) {
            array.copyInto(result, pos, 0, array.size)
            pos += array.size
        }
        return result
    }

    fun splitArray64(array: ByteArray, subArrayLength: Int): Pair<ByteArray, ByteArray> {
        val result = Pair(ByteArray(subArrayLength), ByteArray(subArrayLength))
        array.copyInto(result.first, 0, 0, subArrayLength)
        array.copyInto(result.second, 0, subArrayLength, subArrayLength + subArrayLength)
        return result
    }

    fun splitArray76(
        array: ByteArray,
        keyLength: Int,
        macLength: Int,
        ivLength: Int
    ): Triple<ByteArray, ByteArray, ByteArray> {
        val key = ByteArray(keyLength)
        val mac = ByteArray(macLength)
        val iv = ByteArray(ivLength)
        array.copyInto(key, 0, 0, keyLength)
        array.copyInto(mac, 0, keyLength, keyLength + macLength)
        array.copyInto(iv, 0, keyLength + macLength, keyLength + macLength + ivLength)
        return Triple(key, mac, iv)
    }

    suspend fun log(info: String, key: XDH.PrivateKey) {
        val hexString: String =
            HexUtil.bytesToHexStr(key.encodeToByteArray(XDH.PrivateKey.Format.RAW))
        logD("$info: $hexString")
    }

    suspend fun log(info: String, key: XDH.PublicKey) {
        val hexString: String = HexUtil.bytesToHexStr(key.toBytes())
        logD("$info: $hexString")
    }

    fun log(info: String, bytes: ByteArray?) {
        val hexString: String = HexUtil.bytesToHexStr(bytes)
        logD("$info: $hexString")
    }

    fun logI(info: String, bytes: ByteArray?) {
        val hexString: String = HexUtil.bytesToHexStr(bytes)
        logI("$info: $hexString")
    }

    fun toNormalString(bytes: ByteArray?): String {
        return bytes?.decodeToString() ?: ""
    }
}