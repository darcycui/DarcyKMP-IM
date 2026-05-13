package com.darcy.kmpdemo.storage.database.queryentities

import androidx.room3.Embedded
import androidx.room3.Junction
import androidx.room3.Relation
import com.darcy.kmpdemo.storage.database.tables.FriendshipEntity
import com.darcy.kmpdemo.storage.database.tables.FriendshipUserCrossRef
import com.darcy.kmpdemo.storage.database.tables.UserEntity

data class UserFriends(
    @Embedded
    val user: UserEntity,
    @Relation(
        parentColumn = "userId",
        entityColumn = "friendshipId",
        associateBy = Junction(FriendshipUserCrossRef::class)
    )
    val friends: List<FriendshipEntity>
) {
    companion object {
        fun empty(): UserFriends {
            return UserFriends(
                user = UserEntity.empty(),
                friends = emptyList()
            )
        }
    }
}