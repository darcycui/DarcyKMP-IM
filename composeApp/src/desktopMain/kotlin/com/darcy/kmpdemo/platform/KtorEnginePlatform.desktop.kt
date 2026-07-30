package com.darcy.kmpdemo.platform

import com.darcy.kmpdemo.log.logE
import com.darcy.kmpdemo.log.logV
import com.darcy.kmpdemo.ssl.SslSettings
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO
import kotlinx.coroutines.Dispatchers

actual fun createKtorEngine(): HttpClientEngine = CIO.create {
    dispatcher = Dispatchers.Default
    // 在引擎创建时直接配置 SSL 证书（engine {} 块时可能已太晚）
    try {
        SslSettings.getTrustManager().let { tm ->
            https {
                trustManager = tm
            }
            logV("createKtorEngine: SSL trustManager 设置成功, issuers=${tm?.acceptedIssuers?.size}")
        }
    } catch (e: Exception) {
        logE("createKtorEngine: SSL 证书尚未就绪: ${e.message}")
    }
}
