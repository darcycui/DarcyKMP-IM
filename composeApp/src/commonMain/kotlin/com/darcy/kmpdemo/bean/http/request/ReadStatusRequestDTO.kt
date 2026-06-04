package com.darcy.kmpdemo.bean.http.request

import kotlinx.serialization.Serializable

@Serializable
data class ReceiverMessageReadStatusMarkRequestDTO(
    val userId: Long = 0L,
    val fromUserName: String = "",
    val targetId: Long = 0L,
    val targetName: String = "",
    val msgIds: List<String> = listOf(),
    val conversationId: Long = 0L,
    val conversationType: Int = 1,
    val clientType: String = "",
    val deviceId: String = ""
)

/**
 * 接收方离线消息同步请求
 */
@Serializable
data class ReceiverOfflineMessageSyncRequestDTO(
    val userId: Long = 0L,                    // 接收方用户ID
    val targetId: Long = 0L,                  // 发送方用户ID（会话目标）
    val conversationId: Long = 1,             // 会话ID
    val conversationType: Int = 1,            // 会话类型：1-私聊，2-群聊
    val lastMsgId: String? = null,            // 客户端最后一条消息ID（游标）
    val lastSyncTime: String? = null,         // 客户端最后同步时间（备选游标）
    val page: Int? = null,                    // 页码（从0开始）
    val size: Int? = null,                    // 每页大小
    val limit: Int = 50,                      // 单次拉取数量限制（兼容旧版本）
    val deviceId: String = "",                // 设备ID（多端同步）
    val clientType: String = ""               // 客户端类型：WEB/IOS/ANDROID
) : IRequestDTO

/**
 * 接收方离线消息同步请求
 */
@Serializable
data class SenderOfflineMessageReadSyncRequestDTO(
    val userId: Long = 0L,                    // 接收方用户ID
    val targetId: Long = 0L,                  // 发送方用户ID（会话目标）
    val conversationId: Long = 1,             // 会话ID
    val conversationType: Int = 1,            // 会话类型：1-私聊，2-群聊
    val deviceId: String = "",                // 设备ID（多端同步）
    val clientType: String = "",               // 客户端类型：WEB/IOS/ANDROID
    val since: String = "",                    //  开始时间
    val until: String = ""                     //  结束时间
) : IRequestDTO