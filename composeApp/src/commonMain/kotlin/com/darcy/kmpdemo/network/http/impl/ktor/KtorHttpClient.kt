package com.darcy.kmpdemo.network.http.impl.ktor

import com.darcy.kmpdemo.bean.http.base.BaseResult
import com.darcy.kmpdemo.bean.http.error.ErrorResponse
import com.darcy.kmpdemo.crypto.transport.TransportCipher
import com.darcy.kmpdemo.log.logD
import com.darcy.kmpdemo.log.logW
import com.darcy.kmpdemo.network.http.IHttp
import com.darcy.kmpdemo.network.http.parser.impl.HttpJsonParserImpl
import com.darcy.kmpdemo.network.http.parser.impl.kotlinxJson
import com.darcy.kmpdemo.storage.memory.TransportGlobalStorage
import com.darcy.kmpdemo.utils.JsonHelper
import com.darcy.kmpdemo.utils.hexStrToBytes
import com.darcy.kmpdemo.utils.toFormDataContent
import com.darcy.kmpdemo.utils.toUrlEncodedString
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.utils.io.charsets.Charsets
import io.ktor.utils.io.core.toByteArray
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class KtorHttpClient : IHttp {
    companion object {
        private val TAG = KtorHttpClient::class.simpleName
    }

    private val exceptionHandler: CoroutineExceptionHandler =
        CoroutineExceptionHandler { _, throwable ->
            logD("$TAG exceptionHandler: ${throwable.message}")
            throwable.printStackTrace()
        }
    private val scope: CoroutineScope =
        CoroutineScope(Dispatchers.Default + SupervisorJob() + exceptionHandler)
    private val jsonParser by lazy {
        HttpJsonParserImpl()
    }

    override fun <T> doGetRequest(
        serializer: KSerializer<T>,
        url: String,
        params: Map<String, String>,
        needRetry: Boolean,
        needCache: Boolean,
        success: ((BaseResult<T>) -> Unit),
        successList: ((BaseResult<List<T>>) -> Unit),
        errors: ((ErrorResponse) -> Unit)
    ) {
        scope.launch {
            // dealRetry(needRetry)
            // dealCache(needCache)
            runCatching {
                val realUrl = url + "?" + params.toUrlEncodedString()
                val json = ktorClient.get(realUrl) {
                    this.header("User-Agent", "KMP Client by Ktor Get")
                    contentType(ContentType.Application.Json)
                }.bodyAsText()
                jsonParser.toBean(json, serializer, success, successList, errors)
            }.onFailure {
                it.printStackTrace()
                errors.invoke(ErrorResponse.create(message = it.message ?: "请求失败$url"))
                // 触发协程的 exceptionHandler
                // error("请求失败:${it.message}")
            }
        }
    }

    override fun <T> doPostFormRequest(
        serializer: KSerializer<T>,
        url: String,
        params: Map<String, String>,
        needRetry: Boolean,
        needCache: Boolean,
        success: (BaseResult<T>) -> Unit,
        successList: (BaseResult<List<T>>) -> Unit,
        errors: (ErrorResponse) -> Unit,
        encrypt: Boolean
    ) {
        scope.launch {
            runCatching {
                val formDataContent = params.toFormDataContent()
                val json = ktorClient.post(url) {
                    this.header("User-Agent", "KMP Client by Ktor Post Form")
                    contentType(ContentType.Application.FormUrlEncoded)
                    setBody(formDataContent)
                }.bodyAsText()
                jsonParser.toBean(json, serializer, success, successList, errors)
            }.onFailure {
                it.printStackTrace()
                errors.invoke(ErrorResponse.create(message = it.message ?: "请求失败:$url"))
                // 触发协程的 exceptionHandler
                // error("请求失败:${it.message}")
            }
        }
    }

    override fun <R, T> doPostJsonRequest(
        serializerR: KSerializer<R>,
        serializerT: KSerializer<T>,
        url: String,
        params: R,
        needRetry: Boolean,
        needCache: Boolean,
        success: (BaseResult<T>) -> Unit,
        successList: (BaseResult<List<T>>) -> Unit,
        errors: (ErrorResponse) -> Unit,
        encrypt: Boolean
    ) {
        scope.launch {
            runCatching {
                val json = ktorClient.post(url) {
                    this.header("User-Agent", "KMP Client by Ktor Post Json")
                    contentType(ContentType.Application.Json)
                    setBody(JsonHelper.toJson(serializerR, params))
                }.bodyAsText()
                val decryptedJson = decryptJson(json, url)
                jsonParser.toBean(decryptedJson, serializerT, success, successList, errors)
            }.onFailure {
                it.printStackTrace()
                errors.invoke(ErrorResponse.create(message = it.message ?: "请求失败:$url"))
            }
        }
    }

    suspend fun decryptJson(originalJson: String, url: String): String {
        val json = originalJson.removeSurroundingQuotes()
//        // 解析时先转换为 JsonElement
//        val jsonElement = kotlinxJson.parseToJsonElement(json)
//        // 解析 error_code 字段
//        val errorCode = jsonElement.jsonObject["error_code"]?.jsonPrimitive?.int ?: 0
//        val resultElement = jsonElement.jsonObject["result"]
        if (EncryptBodyConfig.isEnabled()) {
            logW("$TAG 解密json json=$json")
            // 解密 result 字段
            val decryptedResult = TransportCipher.decrypt(
                content = json.hexStrToBytes(),
                aad = "POST:$url".toByteArray()
            )
            return decryptedResult.decodeToString()
        } else {
            logW("$TAG 无需解密json")
            return json
        }
    }
    /**
     * 去除字符串开头和结尾的双引号 (")
     * 安全处理 null、空字符串、长度不够以及内部包含引号的情况
     */
    fun String?.removeSurroundingQuotes(): String {
        // 1. 处理 null 或空字符串
        if (this.isNullOrEmpty()) return ""

        // 2. 长度不够 2 的情况（单个字符或空串，不可能形成一对引号）
        if (this.length < 2) return this

        // 3. 判断首尾是否同时为双引号
        if (this.first() == '"' && this.last() == '"') {
            // substring 会安全处理索引，因为前面已经保证了 length >= 2
            return this.substring(1, this.length - 1)
        }

        // 4. 首尾不匹配双引号，原样返回
        return this
    }

}