package com.darcy.kmpdemo.storage.database.queryentities

import androidx.room3.Embedded
import androidx.room3.Junction
import androidx.room3.Relation
import com.darcy.kmpdemo.storage.database.tables.ConversationEntity
import com.darcy.kmpdemo.storage.database.tables.ConversationUserCrossRef
import com.darcy.kmpdemo.storage.database.tables.UserEntity

data class ConversationUsers(
    @Embedded
    val conversation: ConversationEntity,

    @Relation(
        parentColumn = "conversationId",
        entityColumn = "userId",
        associateBy = Junction(ConversationUserCrossRef::class)
    )
    val users: List<UserEntity>
) {
    companion object {
        fun empty(): ConversationUsers {
            return ConversationUsers(
                conversation = ConversationEntity.empty(),
                users = emptyList()
            )
        }
    }
}
