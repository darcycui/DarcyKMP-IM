package com.darcy.kmpdemo.bean.http.response

import kotlinx.serialization.Serializable

@Serializable
data class DHExchangeResponse(
    val id: Long = 0,
    val userId: Long = 0,
    val sessionId: String = "",
    val publicKey: String = "",
    val keySize: Int = 2048,
    val algorithm: String = "",
    val isCompleted: Boolean = false,
    val isExpired: Boolean = false,
    val expiresAt: String = "",
    val completedAt: String = "",
    val createdAt: String = "",
    val updatedAt: String = "",
) {
}