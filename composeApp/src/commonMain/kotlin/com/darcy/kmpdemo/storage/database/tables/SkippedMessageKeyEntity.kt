package com.darcy.kmpdemo.storage.database.tables

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey


@Entity(
    indices = [
        Index(value = ["userId", "targetId", "dhPublicKey", "chainIndex"], unique = true)
    ]
)
data class SkippedMessageKeyEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long = 0,
    val targetId: Long = 0,
    val msgId: String = "",

    val dhPublicKey: String = "",
    val chainIndex: Long = 0,

    val messageKey: String = "",
    val macKey: String = "",
    val iv: String = "",

    val createdTime: String = ""
)
