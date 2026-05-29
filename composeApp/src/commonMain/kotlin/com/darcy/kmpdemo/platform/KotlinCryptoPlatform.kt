package com.darcy.kmpdemo.platform

import dev.whyoleg.cryptography.CryptographyProvider

expect object KotlinCryptoPlatform {
    fun getCryptographyProvider(): CryptographyProvider
}