package com.darcy.kmpdemo.network.http.impl.ktor

import com.darcy.kmpdemo.crypto.transport.TransportCipher
import com.darcy.kmpdemo.log.logD
import com.darcy.kmpdemo.log.logW
import com.darcy.kmpdemo.storage.memory.TransportGlobalStorage
import com.darcy.kmpdemo.utils.getAAD
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.request.forms.FormDataContent
import io.ktor.http.HttpMethod
import io.ktor.http.Parameters
import io.ktor.http.encodedPath
import io.ktor.utils.io.charsets.Charsets
import io.ktor.utils.io.core.toByteArray


object EncryptFormBodyConfig {
    private var enabled: Boolean = false

    fun isEnabled(): Boolean {
        return TransportGlobalStorage.getServerDhKey().isNotEmpty()
    }
}

/**
 * FormDataContent 拦截器
 * 拦截 FormDataContent 对需要加密的参数进行加密
 */
val EncryptFormBodyPlugin = createClientPlugin(
    "EncryptFormBodyPlugin"
) {

    transformRequestBody { request, body, typeInfo ->
        val method = request.method.value
        val path = request.url.encodedPath
        logD("EncryptFormBodyPlugin: $method $path")
        // 不是POST请求 直接放过
        if (method != "POST" || path.isEmpty()) {
            return@transformRequestBody null
        }
        // 不是FormDataContent 直接放过
        if (body !is FormDataContent) {
            return@transformRequestBody null
        }
        val formBody: FormDataContent = body
        val original: Parameters = formBody.formData
        val needEncrypt: Set<String> = setOf("password", "token", "secret")

        val rebuilt = Parameters.build {
            for (name in original.names()) {
                val values = original.getAll(name) ?: continue
                for (value in values) {
                    val newValue = if (EncryptFormBodyConfig.isEnabled()) {
                        logW("EncryptFormBodyPlugin: 加密 $name")
                        TransportCipher.encrypt(
                            content = value.toByteArray(Charsets.UTF_8),
                            aad = request.getAAD()
                        ).toHexString()
                    } else {
                        logW("EncryptFormBodyPlugin: 无需加密 $name")
                        value
                    }
                    append(name, newValue)
                }
            }
        }

        // 返回新的 body，替换原来的 FormDataContent
        FormDataContent(rebuilt)
    }
}