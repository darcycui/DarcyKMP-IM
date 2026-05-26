package com.darcy.kmpdemo.storage.database.daos

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import com.darcy.kmpdemo.storage.database.tables.OutOfOrderKeyCacheEntity

@Dao
interface OutOfOrderKeyCacheDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: OutOfOrderKeyCacheEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(itemList: List<OutOfOrderKeyCacheEntity>)

    @Query(
        "SELECT * FROM OutOfOrderKeyCacheEntity " +
                "WHERE userId=:userId " +
                "AND targetId=:targetId" +
                "AND msgId=:msgId "
    )
    suspend fun findByUserIdAndMessageId(
        userId: Long,
        targetId: Long,
        msgId: String
    ): OutOfOrderKeyCacheEntity

    @Delete
    suspend fun delete(item: OutOfOrderKeyCacheEntity)

    @Query("DELETE FROM OutOfOrderKeyCacheEntity WHERE userId=:userId AND targetId=:targetId AND msgId=:msgId")
    suspend fun deleteByUserIdAndMessageId(userId: Long, targetId: Long, msgId: String)
}