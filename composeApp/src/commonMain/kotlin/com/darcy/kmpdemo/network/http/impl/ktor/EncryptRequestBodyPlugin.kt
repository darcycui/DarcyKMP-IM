package com.darcy.kmpdemo.network.http.impl.ktor

import com.darcy.kmpdemo.crypto.transport.TransportCipher
import com.darcy.kmpdemo.log.logD
import com.darcy.kmpdemo.log.logI
import com.darcy.kmpdemo.log.logV
import com.darcy.kmpdemo.log.logW
import com.darcy.kmpdemo.storage.memory.TransportGlobalStorage
import com.darcy.kmpdemo.utils.bytesToHexStr
import com.darcy.kmpdemo.utils.getAAD
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.content.TextContent
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.encodedPath
import io.ktor.utils.io.charsets.Charsets
import io.ktor.utils.io.core.toByteArray

object EncryptBodyConfig {

    fun isEnabled(): Boolean {
        val key = TransportGlobalStorage.getServerSharedSecretKey()
        val enabled = key.isNotEmpty() && key.isNotBlank()
        logD("$E_TAG: enabled: $enabled")
        return enabled
    }
}

/**
 * Post请求 拦截器
 * 拦截 Json请求体 进行加密
 */
private const val E_TAG = "EncryptRequestJsonBodyPlugin"
val EncryptRequestJsonBodyPlugin = createClientPlugin(E_TAG) {

    transformRequestBody { request, content, typeInfo ->
        val method = request.method.value
        val path = request.url.encodedPath
        logD("$E_TAG: $method $path content类型: ${content::class}")
        // 只拦截POST请求
        if (method != HttpMethod.Post.value || path.isEmpty()) {
            return@transformRequestBody null
        }
        // 不是 String 直接放过
        if (content !is String) {
            return@transformRequestBody null
        }
        val originalText: String = content
        logI("$E_TAG: originalText: $originalText")
        if (EncryptBodyConfig.isEnabled()) {
            logW("$E_TAG: 加密")
            val encryptText = TransportCipher.encrypt(
                content = originalText.toByteArray(Charsets.UTF_8),
                aad = request.getAAD().also {
                    logD("$E_TAG: AAD: $it")
                }.toByteArray()
            )
            logV("$E_TAG: encryptText: ${encryptText.bytesToHexStr()}")
            // 返回新的 body
            TextContent(
                text = encryptText.toHexString(),
                contentType = ContentType.Application.Json,
            )
        } else {
            logW("$E_TAG: 无需加密")
            // 返回新的 body
            TextContent(
                text = originalText,
                contentType = ContentType.Application.Json,
            )
        }
    }
}