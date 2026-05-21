package com.darcy.kmpdemo.storage.database.tables

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(
    indices = [
        Index(value = ["msgId", "userId", "targetId"], unique = true)
    ]
)
data class MessageReadStatus(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val msgId: String = "",
    val userId: Long = 0,
    val targetId: Long = 0,
    val isRead: Boolean = false,
    val messageType: Int = 1,
    val readTime: String = "",
    val createdTime: String = "",
    val updatedTime: String = ""
) {
}