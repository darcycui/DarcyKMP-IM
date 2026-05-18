package com.darcy.kmpdemo.storage.database.daos

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Update
import com.darcy.kmpdemo.storage.database.tables.SessionRecordEntity

@Dao
interface SessionRecordDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: SessionRecordEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(itemList: List<SessionRecordEntity>)

    @Query("SELECT * FROM SessionRecordEntity WHERE id=:id")
    suspend fun getById(id: Long): SessionRecordEntity?

    @Query("SELECT * FROM SessionRecordEntity WHERE aliceUserId=:aliceUserId AND bobUserId=:bobUserId")
    suspend fun getByUserId(aliceUserId: Long, bobUserId: Long): SessionRecordEntity?

    @Delete
    suspend fun delete(item: SessionRecordEntity)

    @Query("DELETE FROM SessionRecordEntity WHERE id=:id")
    suspend fun deleteById(id: Long)

    @Update
    suspend fun update(item: SessionRecordEntity)

}