package com.darcy.kmpdemo.crypto

import com.darcy.kmpdemo.utils.bytesToHexStr
import com.darcy.kmpdemo.utils.hexStrToBytes
import com.darcy.kmpdemo.crypto.hmac.HMAC1
import io.ktor.utils.io.core.toByteArray
import kotlinx.coroutines.test.runTest
//import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class HMACTest {

    @Test
    fun `test-hmac-sha256-signature`() {
        runTest {

            val originalText = "Hello, World!"
            val data = originalText.encodeToByteArray()
            val key = "secret-key".toByteArray()
            val hmac = HMAC1.calculateHmacSHA256Manually(key, data)
            println("HMAC-SHA256-1: ${hmac.bytesToHexStr()}")

            val hmac2 = HMAC1.hmacSignature(key, data)
            println("HMAC-SHA256-2: ${hmac2.bytesToHexStr()}")
            assertContentEquals(hmac, hmac2, "HMAC计算错误")
        }
    }

    @Test
    fun `test-hmac-sha256-signature-verify`() {
        runTest {
            val originalText = "Hello, World!"
            val data = originalText.encodeToByteArray()
            val key = "secret-key".toByteArray()
            val expectSignature = "16ee525f6c944ff49a368cd593eb7b72883b14456c7b583bba4ff973ff4b30f9".hexStrToBytes()

            val verifier = HMAC1.hmacSignatureVerify(key, data, expectSignature)
            println("verifier=$verifier")
            assertEquals(true, verifier, "HMAC验证失败")
        }
    }
}