package com.darcy.kmpdemo.storage.database.tables

import androidx.room3.Entity

/**
 * 聊天会话和用户的关系表 多对多
 */
@Entity(
    primaryKeys = ["conversationId", "userId"],
)
data class ConversationUserCrossRef(
    val conversationId: Long,
    val userId: Long,
    val unreadCount: Long = 0,
    val lastReadMessageId: Long = 0,
)
