package com.darcy.kmpdemo.bean.http.request

import kotlinx.serialization.Serializable

@Serializable
data class MessageReadStatusInputDTO(
    val userId: Long = 0L,
    val targetId: Long = 0L,
    val targetName: String = "",
    val msgIds: List<String> = listOf(),
    val conversationType: Int = 1,
    val clientType: String = "",
    val deviceId: String = ""
)
