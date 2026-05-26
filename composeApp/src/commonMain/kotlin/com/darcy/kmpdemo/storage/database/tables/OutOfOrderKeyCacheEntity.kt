package com.darcy.kmpdemo.storage.database.tables

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(
    indices = [
        Index(value = ["userId", "targetId", "msgId"], unique = true)
    ]
)
data class OutOfOrderKeyCacheEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long = 0,
    val targetId: Long = 0,
    val msgId: String = "",
    val sendingIndex: Int = 0,
    val receivingIndex: Int = 0,
    val createdTime: String = "",
    val updatedTime: String = ""
) {

}