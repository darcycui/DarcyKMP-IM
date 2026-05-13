package com.darcy.kmpdemo.storage.database.tables

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity
data class ConversationEntity(
    @PrimaryKey(autoGenerate = true)
    val conversationId: Long? = null,
    val userIdFrom: Long,
    val userIdTo: Long,
    val type: Int = 0,
    val name: String = "$userIdFrom-$userIdTo",
    val createdTime: Long = 0,
    val updatedTime: Long = 0,
    val deletedTime: Long = 0
) {
    companion object {
        fun empty(): ConversationEntity {
            return ConversationEntity(
                userIdFrom = 0,
                userIdTo = 0
            )
        }
    }
}
