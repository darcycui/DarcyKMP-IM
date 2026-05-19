package com.darcy.kmpdemo.bean.http.response

import kotlinx.serialization.Serializable

@Serializable
data class X3DHAliceHelloPullResponse(
    val id: Long = 0,
    val fromUserId: Long = 0,
    val toUserId: Long = 0,
    val aliceIdentityKey: String = "",
    val aliceEphemeralKey: String = "",
    val bobOneTimePreKeyId: String = "",
) {
}