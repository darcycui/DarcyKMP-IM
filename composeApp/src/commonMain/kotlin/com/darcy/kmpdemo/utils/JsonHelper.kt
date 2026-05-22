package com.darcy.kmpdemo.utils

import com.darcy.kmpdemo.network.http.parser.impl.kotlinxJson

object JsonHelper {

    inline fun <reified T> toJson(data: T?): String {
        if (data == null) return "{}"
        return runCatching {
            kotlinxJson.encodeToString(data)
        }.onFailure {
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