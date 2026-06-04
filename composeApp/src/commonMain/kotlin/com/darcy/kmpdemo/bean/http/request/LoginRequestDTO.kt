package com.darcy.kmpdemo.bean.http.request

import kotlinx.serialization.Serializable


@Serializable
data class LoginRequestDTO(
    val phone: String = "",
    val password: String = ""
): IRequestDTO
