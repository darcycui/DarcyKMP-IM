package com.darcy.kmpdemo.storage.database.daos

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Update
import com.darcy.kmpdemo.storage.database.tables.IdentityKeyEntity

@Dao
interface IdentityKeyDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: IdentityKeyEntity): Int

    @Query("SELECT * FROM IdentityKey WHERE id=:id LIMIT 1")
    suspend fun getById(id: Long): IdentityKeyEntity?

    @Query("SELECT * FROM IdentityKey WHERE userId=:userId LIMIT 1")
    suspend fun getByUserId(userId: Long): IdentityKeyEntity?

    @Delete
    suspend fun delete(item: IdentityKeyEntity): Int

    @Query("DELETE FROM IdentityKey WHERE id=:id")
    suspend fun deleteById(id: Long)

    @Update
    suspend fun update(item: IdentityKeyEntity): Int
}