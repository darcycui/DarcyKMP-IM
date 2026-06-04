package com.darcy.kmpdemo.bean.http.request

import kotlinx.serialization.Serializable


@Serializable
data class ConversationCreateRequestDTO(
    var userId: Long = 0,
    var targetId: Long = 0,
    var conversationType: Int = 0
) : IRequestDTO

@Serializable
data class ConversationQueryRequestDTO(
    var conversationId: Long = 0,
) : IRequestDTO