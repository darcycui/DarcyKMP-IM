package com.darcy.kmpdemo.storage.database.daos

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Update
import com.darcy.kmpdemo.storage.database.tables.MessageReadStatusEntity

@Dao
interface MessageReadStatusDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: MessageReadStatusEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(itemList: List<MessageReadStatusEntity>)

    @Query("SELECT * FROM MessageReadStatusEntity WHERE userId=:userId AND msgId=:msgId")
    suspend fun findByUserIdAndMessageId(userId: Long, msgId: String): MessageReadStatusEntity?

    @Query("UPDATE MessageReadStatusEntity SET isRead=true, readTime=:readTime WHERE userId=:userId AND msgId=:msgId")
    suspend fun markMessageAsRead(userId: Long, msgId: String, readTime: String): Int

    @Query("UPDATE MessageReadStatusEntity SET isRead=true, readTime=:readTime WHERE userId=:userId AND msgId IN (:msgIds)")
    suspend fun markMessageListAsRead(userId: Long, msgIds: List<String>, readTime: String): Int

    @Delete
    suspend fun delete(item: MessageReadStatusEntity)

    @Query("DELETE FROM MessageReadStatusEntity WHERE userId=:userId AND msgId=:msgId")
    suspend fun deleteByUserIdAndMessageId(userId: Long, msgId: String)

    @Update
    suspend fun update(item: MessageReadStatusEntity)

    @Query("SELECT * FROM MessageReadStatusEntity WHERE userId=:userId AND msgId IN (:msgIds)")
    suspend fun findByUserIdAndMessageIds(
        userId: Long,
        msgIds: List<String>
    ): List<MessageReadStatusEntity>
}