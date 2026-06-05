package com.darcy.kmpdemo.network.http.impl.ktor

import com.darcy.kmpdemo.crypto.transport.TransportCipher
import com.darcy.kmpdemo.log.logD
import com.darcy.kmpdemo.log.logE
import com.darcy.kmpdemo.log.logI
import com.darcy.kmpdemo.log.logW
import com.darcy.kmpdemo.utils.getAAD
import com.darcy.kmpdemo.utils.hexStrToBytes
import com.darcy.kmpdemo.utils.isHexString
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.statement.request
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.content.TextContent
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.core.toByteArray
import io.ktor.utils.io.readRemaining
import kotlinx.io.readByteArray

/**
 * 解密服务端响应 JSON 的解密拦截器（修正版）
 */
private const val D_TAG = "DecryptResponseJsonBodyPlugin"
val DecryptResponseJsonBodyPlugin = createClientPlugin(D_TAG) {

    transformResponseBody { response, content, requestedType ->
        val method = response.request.method.value
        val path = response.request.url.encodedPath
        val contentType = response.headers[HttpHeaders.ContentType]
        logD("$D_TAG: $method $path contentType=${contentType} status=${response.status}")
        // 检查内容类型
        if (contentType?.contains("application/json") != true) {
            logE("$D_TAG: contentType is not application/json, skip decrypt")
            return@transformResponseBody null
        }

        // 从 ByteReadChannel 读取所有剩余字节
        val rawBytes: ByteArray = try {
            content.readRemaining().readByteArray()
        } catch (e: Exception) {
            logE("$D_TAG: read body failed", throwable = e)
            e.printStackTrace()
            return@transformResponseBody null
        }

        // 将字节转为字符串（假设响应体是十六进制字符串的 UTF-8 编码）
        val hexString = rawBytes.decodeToString()
        logI("$D_TAG: encryptText: $hexString")

        // 验证是否为合法的十六进制字符串
        if (hexString.isHexString().not()) {
            logW("$D_TAG: body is not hex string, skip decrypt")
            return@transformResponseBody null
        }

        val decryptedBytes = try {
            if (EncryptBodyConfig.isEnabled())  {
                logW("$D_TAG: 解密")
                TransportCipher.decrypt(
                    content = hexString.hexStrToBytes(),
                    aad = response.request.getAAD().toByteArray()
                )
            } else {
                logW("$D_TAG: 无需解密")
                hexString.hexToByteArray()
            }
        } catch (e: Exception) {
            logE("$D_TAG: decrypt failed", throwable = e)
            return@transformResponseBody null
        }

        val decryptText = decryptedBytes.decodeToString()
        logI("$D_TAG: decryptedText: $decryptText")

        // 返回 TextContent，Ktor 会将此作为新的响应体传递给下游
        ByteReadChannel (decryptText)
    }
}