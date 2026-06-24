package com.darcy.kmpdemo.platform

import com.darcy.kmpdemo.utils.EncryptHelper

actual object EncryptPlatform {
    actual fun encryptString(str: String?): String {
        return EncryptHelper.encryptString(str)
    }

    actual fun decryptString(str: String?): String {
        return EncryptHelper.decryptString(str)
    }
}