package com.darcy.kmpdemo.utils

import kotlinx.serialization.json.Json

object JsonHelper {
    val json = Json

    inline fun <reified T> toJson(data: T?): String {
        if (data == null) return "{}"
        return runCatching {
            json.encodeToString(data)
        }.onFailure {
            it.printStackTrace()
        }.getOrElse { "{}" }
    }

    inline fun <reified T> fromJson(jsonStr: String): T? {
        return runCatching {
            json.decodeFromString<T>(jsonStr)
        }.onFailure {
            it.printStackTrace()
        }.getOrElse {
            null
        }
    }

}