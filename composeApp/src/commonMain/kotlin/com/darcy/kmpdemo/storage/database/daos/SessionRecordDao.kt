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
    fun insert(item: SessionRecordEntity): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(itemList: List<SessionRecordEntity>): Int

    @Query("SELECT * FROM SessionRecordEntity WHERE id=:id")
    fun getById(id: Long): SessionRecordEntity?

    @Query("SELECT * FROM SessionRecordEntity WHERE aliceUserId=:aliceUserId AND bobUserId=:bobUserId")
    fun getByUserId(aliceUserId: Long, bobUserId: Long): SessionRecordEntity?

    @Delete
    fun delete(item: SessionRecordEntity): Int

    @Query("DELETE FROM SessionRecordEntity WHERE id=:id")
    fun deleteById(id: Long)

    @Update
    fun update(item: SessionRecordEntity): Int

}