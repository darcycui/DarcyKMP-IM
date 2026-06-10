package com.darcy.kmpdemo.network.http.impl.ktor

import com.darcy.kmpdemo.bean.http.base.BaseResult
import com.darcy.kmpdemo.crypto.JsonCryptoHelper
import com.darcy.kmpdemo.crypto.removeSurroundingQuotes
import com.darcy.kmpdemo.log.logE
import com.darcy.kmpdemo.log.logI
import com.darcy.kmpdemo.log.logW
import com.darcy.kmpdemo.utils.JsonHelper
import com.darcy.kmpdemo.utils.isHexString
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.request
import io.ktor.http.HttpHeaders
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readRemaining
import kotlinx.io.readByteArray
import kotlinx.serialization.SerializationException

/**
 * 解密服务端响应 JSON 的解密拦截器
 * 注意:client.post().bodyAsText() 不触发解密拦截器 --> 改为 client.post().bosy<String>()
 */
private const val D_TAG = "DecryptResponseJsonBodyPlugin"
val DecryptResponseJsonBodyPlugin = createClientPlugin(D_TAG) {
    logW("$D_TAG: 解密插件 初始化")

    transformResponseBody { response, content, requestedType ->
        val method = response.request.method.value
        val path = response.request.url.encodedPath
        val contentType = response.headers[HttpHeaders.ContentType]
        logW("$D_TAG 解密插件 拦截响应 $method $path contentType=${contentType} status=${response.status}")
        // 检查内容类型
        if (contentType?.contains("application/json") != true) {
            logE("$D_TAG: contentType is not application/json, skip decrypt")
            return@transformResponseBody null
        }

        val decryptText = doDecrypt(content, response)
        // 2. 将 JSON 字符串反序列化为目标类型
        try {
            // 根据你的序列化库选择实现
            when (requestedType.type) {
                String::class -> decryptText
                else -> {
                    // 反序列化为对象
                    JsonHelper.fromJson<BaseResult<*>>(decryptText)
                }
            }
        } catch (e: SerializationException) {
            logE("$D_TAG: 反序列化失败：", throwable = e)
            e.printStackTrace()
            // 解码失败时返回 null，让 ktor 继续执行其他转换
            null
        }
    }
}

suspend fun doDecrypt(content: ByteReadChannel, response: HttpResponse): String {
    // 从 ByteReadChannel 读取所有剩余字节
    val rawBytes: ByteArray = try {
        content.readRemaining().readByteArray()
    } catch (e: Exception) {
        logE("$D_TAG: read body failed", throwable = e)
        e.printStackTrace()
        return ""
    }
    // 将字节转为字符串 并移除前后引号
    val originalStr = rawBytes.decodeToString().removeSurroundingQuotes()
    // logI("$D_TAG: encryptText: $originalStr")
    val decryptText = try {
        if (EncryptBodyConfig.isEnabled()) {
            logW("$D_TAG: 解密")
            if (originalStr.isHexString().not()) {
                logW("$D_TAG: body is not hex string, skip decrypt")
                return originalStr
            }
            JsonCryptoHelper.decryptHttpJson(originalStr, response.request.url.toString())
        } else {
            logW("$D_TAG: 无需解密")
            // 验证是否为合法的十六进制字符串
            return originalStr
        }
    } catch (e: Exception) {
        logE("$D_TAG: decrypt failed", throwable = e)
        e.printStackTrace()
        return ""
    }
    // logI("$D_TAG: decryptedText: $decryptText")
    return decryptText
}
