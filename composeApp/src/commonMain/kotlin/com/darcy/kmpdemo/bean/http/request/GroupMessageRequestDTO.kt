package com.darcy.kmpdemo.bean.http.request

import kotlinx.serialization.Serializable


@Serializable
data class GroupMessageQueryRequestDTO(
    var groupId: Long = 0,
    val userId: Long = 0,
    val conversationId: Long = 0,
    var page: Int = 0,
    var size: Int = 0,
) : IRequestDTO {
}

@Serializable
data class GroupMessageSendRequestDTO(
    var senderId: Long = 0,
    var receiverId: Long = 0,
    var conversationId: Long = 0,
    var content: String = "",
    var msgId: String = "",
) : IRequestDTO {
}