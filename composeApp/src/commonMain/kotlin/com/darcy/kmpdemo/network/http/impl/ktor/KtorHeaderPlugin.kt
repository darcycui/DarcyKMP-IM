package com.darcy.kmpdemo.network.http.impl.ktor

import com.darcy.kmpdemo.log.logD
import com.darcy.kmpdemo.log.logE
import com.darcy.kmpdemo.log.logI
import com.darcy.kmpdemo.network.http.token.TokenManager
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.request.header
import io.ktor.http.HttpStatusCode

class PluginConfiguration {
    var headerName: String = "Authorization"
    var headerValue: String = ""
        get() = TokenManager.getToken()
        set(value) {
            field = "Bearer $value"
        }
}

val CustomHeaderPlugin = createClientPlugin("CustomHeaderPlugin") {
    val config = PluginConfiguration()
    onRequest { request, content ->
        logD("CustomHeaderPlugin Request: ${request.url}")
        request.header(config.headerName, config.headerValue)
    }
    onResponse { response ->
        if (response.status == HttpStatusCode.Unauthorized
            || response.status == HttpStatusCode.Forbidden) {
//        if (response.status == HttpStatusCode.OK) {
            logE("CustomHeaderPlugin Response: 未授权 需要重新登录！")
        } else {
            logI("CustomHeaderPlugin Response: 已授权 ${response.status}")
        }
    }
}