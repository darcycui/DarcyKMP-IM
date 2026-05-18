package com.darcy.kmpdemo.bean.http.response

import kotlinx.serialization.Serializable

@Serializable
data class X3DHKeysPullResponse(
    val userId: Long = 0,
    val status: Int = 0,
    val identityKey: String = "",
    val signedPreKey: String = "",
    val oneTimePreKey: String = ""
) {
}