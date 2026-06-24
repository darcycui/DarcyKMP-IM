package com.darcy.kmpdemo.platform

import com.darcy.kmpdemo.log.logE
import com.darcy.kmpdemo.log.logV
import com.darcy.kmpdemo.ssl.SslSettings
import io.ktor.client.engine.cio.CIOEngineConfig

actual object SSLPlatform {
    actual fun configureEngineTLS(engineConfig: Any) {
        if (engineConfig is CIOEngineConfig) {
            engineConfig.https {
                trustManager = SslSettings.getTrustManager()
            }
        } else {
            logE("configureEngineTLS 错误: engineConfig is not CIOEngineConfig")
        }
    }


    actual suspend fun sslCertsConfig(certsList: List<ByteArray>) {
        logV("sslCertsConfig: init ssl certs")
        SslSettings.initCertBytes(certsList)
    }
}