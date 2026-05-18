package com.darcy.kmpdemo.bean.http.response

import kotlinx.serialization.Serializable

@Serializable
data class X3DHKeysPushResponse(
    val userId: Long = 0,
    val status: Int = 0,
    val message: String = "",
) {
}