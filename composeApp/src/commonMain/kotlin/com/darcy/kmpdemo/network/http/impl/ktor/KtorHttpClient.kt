package com.darcy.kmpdemo.network.http.impl.ktor

import com.darcy.kmpdemo.bean.http.base.BaseResult
import com.darcy.kmpdemo.bean.http.error.ErrorResponse
import com.darcy.kmpdemo.crypto.JsonCryptoHelper
import com.darcy.kmpdemo.log.logD
import com.darcy.kmpdemo.network.http.IHttp
import com.darcy.kmpdemo.network.http.parser.impl.HttpJsonParserImpl
import com.darcy.kmpdemo.utils.JsonHelper
import com.darcy.kmpdemo.utils.toFormDataContent
import com.darcy.kmpdemo.utils.toUrlEncodedString
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.KSerializer

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
                errors.invoke(ErrorResponse.create(throwable = it))
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
                errors.invoke(ErrorResponse.create(throwable = it))
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
                val decryptedJson = JsonCryptoHelper.decryptHttpJson(json, url)
                jsonParser.toBean(decryptedJson, serializerT, success, successList, errors)
            }.onFailure {
                it.printStackTrace()
                errors.invoke(ErrorResponse.create(throwable = it))
            }
        }
    }
}