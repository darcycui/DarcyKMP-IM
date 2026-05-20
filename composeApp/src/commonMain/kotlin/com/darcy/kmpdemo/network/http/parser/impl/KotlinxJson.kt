package com.darcy.kmpdemo.network.http.parser.impl

import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule

val kotlinxJson = Json {
    prettyPrint = true
    isLenient = true
    ignoreUnknownKeys = true
    coerceInputValues = true
    serializersModule = SerializersModule {
        // 注册BaseResult的上下文序列化器 KSerializer
    }
}