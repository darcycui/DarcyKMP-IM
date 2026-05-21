package com.darcy.kmpdemo

import com.darcy.kmpdemo.log.DarcyLogger
import com.darcy.kmpdemo.log.logI
import com.darcy.kmpdemo.utils.KeyValueHelper
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class KmpStorageLibraryTest {

    @BeforeTest
    fun setUp() {
        DarcyLogger.initLogger()
    }

    @Test
    fun `test-put-get-data-from-kmp-library-multiplatform-settings`() {

        logI("test start")
        val keyValueStorage = KeyValueHelper.getInstance("userInfo")
        val name = keyValueStorage.getString("name", "default-name")
        logI("name from storage: $name")
        keyValueStorage.putString("name", "darcy-name")
        val name2 = keyValueStorage.getString("name", "default-name")
        logI("name2 from storage: $name2")
        assertEquals("darcy-name", name2)
        logI("test end")
    }
}