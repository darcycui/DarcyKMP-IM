package com.darcy.kmpdemo.bean.http.request

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequestDTO(
    val username: String,
    val password: String,
    val nickname: String = "",
    val avatar: String = "",
    val phone: String = "",
    val email: String="",
    val gender: String = "",
    val roles: String = "",

    val signature: String = "",
    val settings: String = "",
) : IRequestDTO
