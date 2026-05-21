package com.darcy.kmpdemo.storage.database.daos

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Update
import com.darcy.kmpdemo.storage.database.tables.MessageReadStatus

@Dao
interface MessageReadStatusDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: MessageReadStatus)

    @Query("SELECT * FROM MessageReadStatus WHERE userId=:userId AND msgId=:msgId")
    suspend fun findByUserIdAndMessageId(userId: Long, msgId: String): MessageReadStatus?

    @Query("UPDATE MessageReadStatus SET isRead=true, readTime=:readTime WHERE userId=:userId AND msgId IN (:msgIds)")
    suspend fun markMessagesAsRead(userId: Long, msgIds: List<String>, readTime: String): Int

    @Delete
    suspend fun delete(item: MessageReadStatus)

    @Query("DELETE FROM MessageReadStatus WHERE userId=:userId AND msgId=:msgId")
    suspend fun deleteByUserIdAndMessageId(userId: Long, msgId: String)

    @Update
    suspend fun update(item: MessageReadStatus)

    @Query("SELECT * FROM MessageReadStatus WHERE userId=:userId AND msgId IN (:msgIds)")
    suspend fun findByUserIdAndMessageIds(
        userId: Long,
        msgIds: List<String>
    ): List<MessageReadStatus>
}