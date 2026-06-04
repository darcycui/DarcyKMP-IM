package com.darcy.kmpdemo.bean.http.request

import kotlinx.serialization.Serializable


@Serializable
data class FriendshipDeleteRequestDTO(
    var userId: Long = 0,
    var friendId: Long = 0
) : IRequestDTO

@Serializable
data class FriendshipUpdateRequestDTO(
    var friendshipId: Long = 0,
) : IRequestDTO
