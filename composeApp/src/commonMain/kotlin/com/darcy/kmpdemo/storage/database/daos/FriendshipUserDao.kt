package com.darcy.kmpdemo.storage.database.daos

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Update
import com.darcy.kmpdemo.storage.database.tables.FriendshipEntity

@Dao
interface FriendshipUserDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: FriendshipEntity)

    @Update
    suspend fun update(item: FriendshipEntity)

    @Delete
    suspend fun delete(item: FriendshipEntity)

    @Query("SELECT * FROM FriendshipEntity WHERE userIdFrom = :userIdFrom AND userIdTo = :userIdTo")
    suspend fun getByUserId(userIdFrom: Long, userIdTo: Long): FriendshipEntity

}