package com.darcy.kmpdemo.storage.database.daos

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Update
import com.darcy.kmpdemo.storage.database.tables.SignedPreKeyEntity

@Dao
interface SignedPreKeyDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: SignedPreKeyEntity)

    @Query("SELECT * FROM SignedPreKeyEntity WHERE id=:id")
    suspend fun getById(id: Long): SignedPreKeyEntity?

    @Query("SELECT * FROM SignedPreKeyEntity WHERE userId=:userId")
    suspend fun getByUserId(userId: Long): SignedPreKeyEntity?

    @Delete
    suspend fun delete(item: SignedPreKeyEntity)

    @Query("DELETE FROM SignedPreKeyEntity WHERE id=:id")
    suspend fun deleteById(id: Long)

    @Update
    suspend fun update(item: SignedPreKeyEntity)
}