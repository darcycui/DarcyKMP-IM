package com.darcy.kmpdemo.platform

import com.darcy.kmpdemo.log.logE
import com.darcy.kmpdemo.log.logV
import com.darcy.kmpdemo.ssl.SslSettings
import io.ktor.client.engine.cio.CIOEngineConfig

actual object SSLPlatform {
    actual fun configureEngineTLS(engineConfig: Any) {
        if (engineConfig is CIOEngineConfig) {
            val tm = try {
                SslSettings.getTrustManager().also {
                    logV("configureEngineTLS: got CompositeX509TrustManager, issuers=${it?.acceptedIssuers?.size}")
                }
            } catch (e: Exception) {
                logE("configureEngineTLS getTrustManager failed: ${e.message}")
                null
            }
            if (tm != null) {
                engineConfig.https {
                    trustManager = tm
                }
                logV("configureEngineTLS: trustManager set successfully")
            } else {
                logE("configureEngineTLS: trustManager is null, using system default")
            }
        } else {
            logE("configureEngineTLS 错误: engineConfig is not CIOEngineConfig")
        }
    }


    actual suspend fun sslCertsConfig(certsList: List<ByteArray>) {
        logV("sslCertsConfig: init ssl certs, count=${certsList.size}")
        // 验证证书内容非空
        certsList.forEachIndexed { index, bytes ->
            logV("sslCertsConfig: cert[$index] size=${bytes.size}")
        }
        SslSettings.initCertBytes(certsList)
    }
}