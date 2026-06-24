package com.darcy.kmpdemo.platform

import dev.whyoleg.cryptography.CryptographyProvider

actual object KotlinCryptoPlatform {
    actual fun getCryptographyProvider(): CryptographyProvider {
        return CryptographyProvider.Default
    }
}