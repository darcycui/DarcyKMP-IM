package com.darcy.kmpdemo.network.http.impl.ktor

import com.darcy.kmpdemo.log.logD
import com.darcy.kmpdemo.log.logE
import com.darcy.kmpdemo.log.logI
import com.darcy.kmpdemo.network.http.token.TokenManager
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.request.header
import io.ktor.http.HttpStatusCode


val CustomHeaderPlugin = createClientPlugin("CustomHeaderPlugin") {
    onRequest { request, content ->
        logD("CustomHeaderPlugin Request: ${request.url}")
        request.header("Authorization", TokenManager.getToken())
    }
    onResponse { response ->
        val message = "${response.status} ${response.call.request.url}"
        if (response.status == HttpStatusCode.Unauthorized
            || response.status == HttpStatusCode.Forbidden) {
//        if (response.status == HttpStatusCode.OK) {
            logE("CustomHeaderPlugin Response: 未授权 需要重新登录！$message")
        } else {
            logI("CustomHeaderPlugin Response: 已授权 $message")
        }
    }
}