package com.darcy.kmpdemo.bean.http.request

import kotlinx.serialization.Serializable

@Serializable
data class X3DHBobKeysRequest(
    val identityKey: String = "",
    val signedPreKey: String = "",
    val oneTimePreKey: String = ""
)