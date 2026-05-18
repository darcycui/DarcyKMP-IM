package com.darcy.kmpdemo.storage.database.daos

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Update
import com.darcy.kmpdemo.storage.database.tables.OneTimePreKeyEntity

@Dao
interface OneTimePreKeyDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: OneTimePreKeyEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(itemList: List<OneTimePreKeyEntity>)

    @Query("SELECT * FROM OneTimePreKeyEntity WHERE id=:id")
    suspend fun getById(id: Long): OneTimePreKeyEntity?

    @Query("SELECT * FROM OneTimePreKeyEntity WHERE userId=:userId")
    suspend fun getAllByUserId(userId: Long): List<OneTimePreKeyEntity>

    @Delete
    suspend fun delete(item: OneTimePreKeyEntity)

    @Query("DELETE FROM OneTimePreKeyEntity WHERE id=:id")
    suspend fun deleteById(id: Long)

    @Update
    suspend fun update(item: OneTimePreKeyEntity)
}