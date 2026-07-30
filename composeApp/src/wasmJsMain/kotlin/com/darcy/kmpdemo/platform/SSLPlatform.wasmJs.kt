package com.darcy.kmpdemo.platform

actual object SSLPlatform {
    actual suspend fun sslCertsConfig(certsList: List<ByteArray>) {
    }

    actual fun configureEngineTLS(engineConfig: Any) {
        // 无需配置自定义证书
    }
}