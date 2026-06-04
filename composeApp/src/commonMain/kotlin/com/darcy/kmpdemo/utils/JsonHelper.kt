package com.darcy.kmpdemo.utils

import com.darcy.kmpdemo.log.logE
import com.darcy.kmpdemo.network.http.parser.impl.kotlinxJson
import kotlinx.serialization.KSerializer

object JsonHelper {

    fun <T> toJson(serializer: KSerializer<T>, data: T?): String {
        if (data == null) return "{}"
        return runCatching {
            kotlinxJson.encodeToString(serializer, data)
        }.onFailure {
            logE("toJson error: ${it.message}")
            it.printStackTrace()
        }.getOrElse { "{}" }
    }

    inline fun <reified T> toJson(data: T?): String {
        if (data == null) return "{}"
        return runCatching {
            val serializer = kotlinx.serialization.serializer<T>()
            kotlinxJson.encodeToString(serializer, data)
        }.onFailure {
            logE("toJson error: ${it.message}")
            it.printStackTrace()
        }.getOrElse { "{}" }
    }

    inline fun <reified T> fromJson(jsonStr: String): T? {
        return runCatching {
            kotlinxJson.decodeFromString<T>(jsonStr)
        }.onFailure {
            it.printStackTrace()
        }.getOrElse {
            null
        }
    }

}