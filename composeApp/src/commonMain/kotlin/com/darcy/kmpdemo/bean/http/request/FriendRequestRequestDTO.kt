package com.darcy.kmpdemo.bean.http.request

import kotlinx.serialization.Serializable


@Serializable
data class FriendRequestActionRequestDTO(
    var friendRequestId: Long = 0,
) : IRequestDTO

@Serializable
data class FriendRequestCreateRequestDTO(
    var fromUserId: Long = 0,
    var toUserId: Long = 0,
) : IRequestDTO

@Serializable
data class FriendRequestQueryToRequestDTO(
    var toUserId: Long = 0,
) : IRequestDTO

@Serializable
data class FriendRequestQueryFromRequestDTO(
    var fromUserId: Long = 0,
) : IRequestDTO