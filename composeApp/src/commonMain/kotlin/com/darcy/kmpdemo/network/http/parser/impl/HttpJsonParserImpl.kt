package com.darcy.kmpdemo.network.http.parser.impl

import com.darcy.kmpdemo.bean.http.base.BaseResult
import com.darcy.kmpdemo.bean.http.error.ErrorResponse
import com.darcy.kmpdemo.network.http.parser.IHttpJsonParser
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class HttpJsonParserImpl : IHttpJsonParser {
    override fun <T> toBean(
        json: String,
        kSerializer: KSerializer<T>,
        success: ((BaseResult<T>) -> Unit),
        successList: ((BaseResult<List<T>>) -> Unit),
        error: ((ErrorResponse) -> Unit)
    ) {
        // 解析时先转换为 JsonElement
        val jsonElement = kotlinxJson.parseToJsonElement(json)
        // 解析 error_code 字段
        val errorCode = jsonElement.jsonObject["error_code"]?.jsonPrimitive?.int ?: 0
        val resultElement = jsonElement.jsonObject["result"]
        // logD("resultElement=$resultElement")
        if (errorCode != 0) {
            if (resultElement != null) {
                val errorResponse = kotlinxJson.decodeFromJsonElement<ErrorResponse>(
                    ErrorResponse.serializer(), resultElement
                )
                error.invoke(errorResponse)
            } else {
                error.invoke(
                    ErrorResponse(
                        status = errorCode,
                        error = "未获取到错误信息"
                    )
                )
            }
            return
        }
        // 根据result字段判断是object还是array
        when (resultElement) {
            is JsonObject -> {
                kSerializer.let {
                    // 创建序列化器
                    // https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/serializers.md#builtin-primitive-serializers
                    val realSerializer = BaseResult.createSerializer(it)
                    success.invoke(
                        kotlinxJson.decodeFromString<BaseResult<T>>(realSerializer, json)
                    )
                }
            }

            is JsonArray -> {
                kSerializer.let {
                    val realSerializer = BaseResult.createSerializerList(it)
                    successList.invoke(
                        kotlinxJson.decodeFromString<BaseResult<List<T>>>(realSerializer, json)
                    )
                }
            }


            else -> {
                throw Exception("json result is not object or array")
            }
        }
    }
}