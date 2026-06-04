package com.darcy.kmpdemo.bean.http.request

import kotlinx.serialization.Serializable


@Serializable
data class GroupInviteCreateRequestDTO(
    var groupId: Long = 0,
    var inviterId: Long = 0,
    var inviteeId: Long = 0
) : IRequestDTO

@Serializable
data class GroupInviteQueryToRequestDTO(
    var toUserId: Long = 0,
) : IRequestDTO

@Serializable
data class GroupInviteQueryFromRequestDTO(
    var fromUserId: Long = 0,
) : IRequestDTO