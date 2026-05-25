package com.darcy.kmpdemo.storage.database.daos

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Update
import com.darcy.kmpdemo.storage.database.tables.PrivateMessageEntity

@Dao
interface PrivateMessageDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: PrivateMessageEntity)

    @Update
    suspend fun update(item: PrivateMessageEntity)

    @Delete
    suspend fun delete(item: PrivateMessageEntity)

    @Query("SELECT * FROM PrivateMessageEntity WHERE id=:id LIMIT 1")
    suspend fun getById(id: Long): PrivateMessageEntity?

    @Query("SELECT * FROM PrivateMessageEntity WHERE msgId=:msgId")
    suspend fun getByMsgId(msgId: String): PrivateMessageEntity?
}