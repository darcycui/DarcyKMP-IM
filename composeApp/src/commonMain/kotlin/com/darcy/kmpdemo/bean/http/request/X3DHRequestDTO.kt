package com.darcy.kmpdemo.bean.http.request

import kotlinx.serialization.Serializable


@Serializable
data class X3DHPushKeysRequestDTO(
    var userId: Long = 0,
    val identityKey: String = "",
    val signedPreKey: String = "",
    val oneTimePreKeys: List<OneTimePreKeyInputDTO> = listOf()
)

@Serializable
data class X3DHPullKeysRequestDTO(
    var aliceUserId: Long = 0,
    var bobUserId: Long = 0
) : IRequestDTO

@Serializable
data class X3DHPushHelloRequestDTO(
    var aliceUserId: Long = 0,
    var bobUserId: Long = 0,
    val aliceIdentityKey: String = "",
    val aliceEphemeralKey: String = "",
    val bobOneTimePreKeyId: String = ""
) : IRequestDTO


@Serializable
data class X3DHPullHelloRequestDTO(
    var aliceUserId: Long = 0,
    var bobUserId: Long = 0,
) : IRequestDTO

@Serializable
data class OneTimePreKeyInputDTO(
    var id: Long = 0,
    var keyId: String = "",
    var userId: Long = 0,
    var publicKey: String = "",
) : IRequestDTO {
}