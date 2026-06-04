package com.darcy.kmpdemo.bean.http.request

import kotlinx.serialization.Serializable


@Serializable
data class DHExchangeRequestDTO(
    var userId: Long = 0,
    var publicKey: String = ""
) : IRequestDTO
