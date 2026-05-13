package com.darcy.kmpdemo.storage.database.queryentities

import androidx.room3.Embedded
import androidx.room3.Junction
import androidx.room3.Relation
import com.darcy.kmpdemo.storage.database.tables.ConversationEntity
import com.darcy.kmpdemo.storage.database.tables.ConversationUserCrossRef
import com.darcy.kmpdemo.storage.database.tables.UserEntity

data class UserConversations(
    @Embedded
    val user: UserEntity,

    @Relation(
        parentColumn = "userId",
        entityColumn = "conversationId",
        associateBy = Junction(ConversationUserCrossRef::class)
    )
    val conversations: List<ConversationEntity>
) {
    companion object {
        fun empty(): UserConversations {
            return UserConversations(
                user = UserEntity.empty(),
                conversations = emptyList()
            )
        }
    }
}
