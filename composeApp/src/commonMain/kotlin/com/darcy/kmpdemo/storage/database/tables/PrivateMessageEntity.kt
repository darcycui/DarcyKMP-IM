package com.darcy.kmpdemo.storage.database.tables

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey
import com.darcy.kmpdemo.storage.memory.IMGlobalStorage

@Entity(
    indices = [
        Index(value = ["msgId", "userId"], unique = true)
    ]
)
data class PrivateMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val msgId: String = "",
    val userId: Long = 0,
    val targetId: Long = 0,
    val content: String = "",
    val messageType: Int = 1,
    val createdTime: String = "",
    val updatedTime: String = "",
    // 添加三个字段
    val dhPublicKey: String = "",
    val nKey: Long = 0L,
    val pnKey: Long = 0L
) {
    fun isSelfSend(): Boolean {
        return userId != 0L && userId == IMGlobalStorage.getCurrentUserId()
    }
}
