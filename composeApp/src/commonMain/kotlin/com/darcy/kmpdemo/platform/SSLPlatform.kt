package com.darcy.kmpdemo.platform

expect object SSLPlatform {
    // SSL证书初始化
    suspend fun sslCertsConfig(certsList: List<ByteArray>)

    // 给Ktor配置SSL证书 不同平台提供不同实现
    fun configureEngineTLS(engineConfig: Any): Unit
}