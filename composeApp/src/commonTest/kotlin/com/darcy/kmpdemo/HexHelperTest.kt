package com.darcy.kmpdemo

import com.darcy.kmpdemo.utils.HexHelper
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class HexHelperTest {

    @Test
    fun `test-bytes-to-string`() {
        val bytes = "hello world".encodeToByteArray()
        val hex1 = bytes.toHexString()
        println("hex1=$hex1")
        val hex2 = HexHelper.bytesToHexStr(bytes)
        println("hex2=$hex2")

        assertEquals("68656c6c6f20776f726c64", hex1, "hex1 should be the expect value")
        assertEquals(hex1, hex2, "hex1 and hex2 should be equal")
    }

    @Test
    fun `test-string-to-bytes`() {
        val hex = "68656c6c6f20776f726c64"
        val bytes1 = hex.hexToByteArray()
        println("bytes1=${bytes1.contentToString()}")
        val bytes2 = HexHelper.hexStrToBytes(hex)
        println("bytes2=${bytes2.contentToString()}")
        // 断言两个对象的内容是否相等 使用 assertContentEquals
        assertContentEquals(bytes1, bytes2, "bytes1 and bytes2 should be equal")
        // 或者转为字符串后比较 使用 assertEquals
        assertEquals(bytes1.toList(), bytes2.toList(), "bytes1 and bytes2 should be equal")
    }
}