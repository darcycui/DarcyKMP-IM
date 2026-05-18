package com.darcy.kmpdemo.bean.http.response

import kotlinx.serialization.Serializable

@Serializable
data class X3DHAliceHelloPushResponse(
    val userId: Long = 0,
    val status: Int = 0,
    val message: String = "",
) {
}