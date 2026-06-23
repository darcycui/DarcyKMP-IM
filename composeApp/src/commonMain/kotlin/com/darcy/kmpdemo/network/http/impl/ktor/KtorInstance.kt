package com.darcy.kmpdemo.network.http.impl.ktor

import com.darcy.kmpdemo.log.logE
import io.ktor.client.HttpClient
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.addDefaultResponseValidation
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.request.header
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.json.Json

const val KTOR_TAG = "KtorClient:"
const val KTOR_FILE_TAG = KTOR_TAG + "File Download:"

val ktorExceptionHandler = CoroutineExceptionHandler { _, throwable ->
    logE("$KTOR_TAG exceptionHandler: ${throwable.message}")
    throwable.printStackTrace()
}
val ktorScope: CoroutineScope =
    CoroutineScope(Dispatchers.Default + SupervisorJob() + ktorExceptionHandler)

val ktorClient: HttpClient
    get() = HttpClient(createKtorEngine()) {
        // 超时时间
        install(HttpTimeout) {
            socketTimeoutMillis = 30_000
            requestTimeoutMillis = 30_000
        }
        // 日志
        install(Logging) {
            level = LogLevel.ALL
            logger = KtorLogger()
        }
        // 重试
        install(HttpRequestRetry) {
            noRetry()
        }
//        install(HttpRequestRetry) {
//            maxRetries = 1
//            exponentialDelay() // 指数增长延迟
//            retryIf { request, response -> response.status.isSuccess().not() }
//            retryOnExceptionIf { request, cause ->
//                cause is ConnectTimeoutException
//            }
//            delayMillis { retry -> retry * 3000L }
//        }
        // intercept: monitor and retry request
        install(HttpSend) {
            maxSendCount = 20
        }

        // 默认请求设置
        defaultRequest {
            header("Content-Type-default", "application/json")
            url ("https://darcycui.com.cn/api")
        }
        // 自定义插件 header
        install(CustomHeaderPlugin)
        // 自定义插件 加密
        install(EncryptRequestJsonBodyPlugin)
        // 自定义插件 解密 todo: client.post().bodyAsText() 方式不触发该插件?
        install(DecryptResponseJsonBodyPlugin)
        // 序列化
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
                explicitNulls = false
            })
        }
        // 开启 websocket
        install(WebSockets) {
            pingIntervalMillis = 20_000
            maxFrameSize = 8 * 1024
            contentConverter = null
        }
        // 遇到异常 停止请求
        expectSuccess = true
        // 添加默认的异常处理
        addDefaultResponseValidation()
    }
