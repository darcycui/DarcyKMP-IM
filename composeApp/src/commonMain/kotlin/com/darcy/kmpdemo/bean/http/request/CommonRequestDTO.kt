package com.darcy.kmpdemo.bean.http.request

import kotlinx.serialization.Serializable

@Serializable
data class CommonRequestDTO(
    var userId: Long = 0
) : IRequestDTO
