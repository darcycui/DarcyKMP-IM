package com.darcy.kmpdemo.bean.http.response

import kotlinx.serialization.Serializable

@Serializable
data class MessageReadStatusResponse(
    val id: Long = 0,
    val msgIds: List<String> = listOf(),
    val userId: Long = 0,
    val conversationType: Int = 1,
    val targetId: Long = 0L,
    val isRead: Boolean = false,
    val readTime: String = "",
    val clientType: String = "",
    val deviceId: String = "",
)