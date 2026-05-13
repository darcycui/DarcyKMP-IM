package com.darcy.kmpdemo.storage.database.daos

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Update
import com.darcy.kmpdemo.storage.database.tables.ConversationEntity

@Dao
interface ConversationDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: ConversationEntity)

    @Query("SELECT * FROM ConversationEntity WHERE conversationId=:conversationId LIMIT 1")
    suspend fun getConversationById(conversationId: Long): ConversationEntity?

    @Query("SELECT * FROM ConversationEntity WHERE name=:conversationName LIMIT 1")
    suspend fun getConversationByName(conversationName: String): ConversationEntity?

    @Query("SELECT * FROM ConversationEntity")
    suspend fun getAllConversations(): List<ConversationEntity>

    @Update
    suspend fun update(item: ConversationEntity)

    @Delete
    suspend fun delete(item: ConversationEntity)
}