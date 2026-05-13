package com.darcy.kmpdemo.storage.database.tables

import androidx.room3.Entity

// 交叉引用类，用于建立用户和好友关系之间的关联
@Entity(primaryKeys = ["friendshipId", "userId"])
data class FriendshipUserCrossRef(
    val friendshipId: Long,
    val userId: Long,
)
