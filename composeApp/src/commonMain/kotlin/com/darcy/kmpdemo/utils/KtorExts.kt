package com.darcy.kmpdemo.utils

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.forms.FormDataContent
import io.ktor.http.Parameters
import io.ktor.http.encodedPath
import io.ktor.utils.io.core.toByteArray
import kotlin.collections.forEach

fun Map<String, String>.toFormDataContent(): FormDataContent {
    val map = this
    // create parameters builder
    val parameters = Parameters.Companion.build {
        map.forEach {
            append(it.key, it.value)
        }
    }
    return FormDataContent(parameters)
}

fun HttpRequestBuilder.getAAD(): ByteArray {
    return "${this.method.value}:${this.url.encodedPath}".toByteArray()
}