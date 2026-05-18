package com.darcy.kmpdemo.bean.http.request

import kotlinx.serialization.Serializable

@Serializable
data class X3DHAliceHelloRequest(
    val userId: Long = 0,
    val identityKey: String = "",
    val ephemeralKey: String = "",
)