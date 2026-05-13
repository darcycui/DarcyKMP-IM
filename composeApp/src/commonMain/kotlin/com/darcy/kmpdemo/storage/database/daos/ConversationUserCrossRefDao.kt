package com.darcy.kmpdemo.storage.database.daos

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Update
import com.darcy.kmpdemo.storage.database.queryentities.ConversationUsers
import com.darcy.kmpdemo.storage.database.queryentities.UserConversations
import com.darcy.kmpdemo.storage.database.tables.ConversationUserCrossRef

@Dao
interface ConversationUserCrossRefDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: ConversationUserCrossRef)

    @Update
    suspend fun update(item: ConversationUserCrossRef)

    @Delete
    suspend fun delete(item: ConversationUserCrossRef)


    @Transaction
    @Query("SELECT * FROM ConversationEntity WHERE conversationId = :conversationId")
    suspend fun getUsersByConversationId(conversationId: Long): ConversationUsers?

    @Transaction
    @Query("SELECT * FROM UserEntity WHERE userId = :userId")
    suspend fun getConversationsByUserId(userId: Long): UserConversations?
}