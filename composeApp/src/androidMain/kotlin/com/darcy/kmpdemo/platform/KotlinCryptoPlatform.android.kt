package com.darcy.kmpdemo.platform

import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.providers.jdk.JDK
import org.bouncycastle.jce.provider.BouncyCastleProvider

actual object KotlinCryptoPlatform {
    actual fun getCryptographyProvider(): CryptographyProvider {
        return CryptographyProvider.JDK(BouncyCastleProvider())
    }
}