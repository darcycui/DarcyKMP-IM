package com.darcy.kmpdemo.storage.database.tables

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity
data class FriendshipEntity(
    @PrimaryKey(autoGenerate = true)
    val friendshipId: Long? = null,
    val userIdFrom: Long,
    val userIdTo: Long,
    val markNameOfTo: String,
    val markNameOfFrom: String,
    val createdTime: Long = 0,
    val updatedTime: Long = 0,
    val deletedTime: Long = 0,
    val name: String = "$userIdFrom-$userIdTo",
) {
    companion object {
        fun empty(): FriendshipEntity {
            return FriendshipEntity(
                userIdFrom = 0,
                userIdTo = 0,
                markNameOfTo = "",
                markNameOfFrom = ""
            )
        }
    }
}
