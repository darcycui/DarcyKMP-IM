package com.darcy.kmpdemo.platform

expect object EncryptPlatform {
    fun encryptString(str: String?): String
    
    fun decryptString(str: String?): String
}