package com.darcy.kmpdemo.utils

import kotlinx.serialization.json.Json

object JsonHelper {
    val json = Json

    inline fun <reified T> toJson(data: T): String {
        return json.encodeToString(data)
    }

    inline fun <reified T> fromJson(jsonStr: String): T {
        return jsonStr.let { json.decodeFromString(it) }
    }

}