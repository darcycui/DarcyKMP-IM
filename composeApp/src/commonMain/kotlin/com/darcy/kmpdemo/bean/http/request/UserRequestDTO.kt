package com.darcy.kmpdemo.bean.http.request

import kotlinx.serialization.Serializable


@Serializable
data class UserUpdateRequestDTO(
    var userId: Long = 0,
    val username: String,
    val password: String,
    val nickname: String = "",
    val avatar: String = "",
    val phone: String = "",
    val email: String = "",
    val gender: String = "",
    val roles: String = "",
    val signature: String = "",
    val settings: String = "",
) : IRequestDTO

@Serializable
data class UserQueryIdRequestDTO(
    var userId: Long = 0
) : IRequestDTO

@Serializable
data class UserQueryPhoneRequestDTO(
    var phone: String = ""
) : IRequestDTO

@Serializable
data class UserQueryEmailRequestDTO(
    var email: String = ""
) : IRequestDTO

@Serializable
data class UserQueryNameRequestDTO(
    var username: String = ""
) : IRequestDTO