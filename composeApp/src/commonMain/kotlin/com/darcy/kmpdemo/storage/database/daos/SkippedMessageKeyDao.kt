package com.darcy.kmpdemo.storage.database.daos

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import com.darcy.kmpdemo.storage.database.tables.SkippedMessageKeyEntity

@Dao
interface SkippedMessageKeyDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: SkippedMessageKeyEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(itemList: List<SkippedMessageKeyEntity>)

    @Query("SELECT * FROM SkippedMessageKeyEntity WHERE userId=:userId AND targetId=:targetId AND dhPublicKey=:dhPublicKey AND chainIndex=:chainIndex LIMIT 1")
    suspend fun findByIndexAndDHKey(
        userId: Long,
        targetId: Long,
        chainIndex: Long,
        dhPublicKey: String
    ): SkippedMessageKeyEntity?

    @Query("SELECT * FROM SkippedMessageKeyEntity WHERE userId=:userId AND targetId=:targetId ORDER BY chainIndex ASC")
    suspend fun queryByUserIdAndTargetId(
        userId: Long,
        targetId: Long
    ): List<SkippedMessageKeyEntity>

    @Delete
    suspend fun delete(item: SkippedMessageKeyEntity): Int

    @Query("DELETE FROM SkippedMessageKeyEntity WHERE userId=:userId AND targetId=:targetId AND dhPublicKey=:dhPublicKey AND chainIndex=:chainIndex")
    suspend fun deleteByKey(
        userId: Long,
        targetId: Long,
        dhPublicKey: String,
        chainIndex: Long
    ): Int

    @Query("DELETE FROM SkippedMessageKeyEntity WHERE userId=:userId AND targetId=:targetId AND chainIndex < :threshold")
    suspend fun deleteOlderThan(userId: Long, targetId: Long, threshold: Long): Int

    @Query("SELECT COUNT(*) FROM SkippedMessageKeyEntity WHERE userId=:userId AND targetId=:targetId")
    suspend fun countByUserIdAndTargetId(userId: Long, targetId: Long): Int

    @Query("DELETE FROM SkippedMessageKeyEntity WHERE userId=:userId AND targetId=:targetId")
    suspend fun clearByUserIdAndTargetId(userId: Long, targetId: Long): Int
}
