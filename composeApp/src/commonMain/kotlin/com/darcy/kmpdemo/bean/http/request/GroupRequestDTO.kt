package com.darcy.kmpdemo.bean.http.request

import kotlinx.serialization.Serializable


@Serializable
data class GroupCreateRequestDTO(
    var ownerId: Long = 0,
    val groupName: String = ""
) : IRequestDTO

@Serializable
data class GroupQueryRequestDTO(
    var groupId: Long = 0,
) : IRequestDTO

@Serializable
data class GroupUpdateRequestDTO(
    var groupId: Long = 0,
    val groupName: String = ""
) : IRequestDTO