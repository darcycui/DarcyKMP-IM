package com.darcy.kmpdemo

import com.darcy.kmpdemo.utils.HashHelper
import kotlin.test.Test
import kotlin.test.assertEquals

class HashHelperTest {
    @Test
    fun `test-sha256-hash`() {
        val str = "hello world"
        val hash = HashHelper.sha256Str(str)
        println("hash=$hash")
        assertEquals(
            "b94d27b9934d3e08a52e52d7da7dabfac484efe37a5380ee9088f7ace2efcde9",
            hash,
            "hash should be the expect value"
        )
    }
}