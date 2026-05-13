package com.darcy.kmpdemo.storage.database.daos

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Update
import com.darcy.kmpdemo.storage.database.queryentities.UserFriends
import com.darcy.kmpdemo.storage.database.tables.FriendshipUserCrossRef

@Dao
interface FriendshipUserCrossRefDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: FriendshipUserCrossRef)

    @Update
    suspend fun update(item: FriendshipUserCrossRef)

    @Delete
    suspend fun delete(item: FriendshipUserCrossRef)


    /**
     * 查询用户所有好友
     */
    @Transaction
    @Query("SELECT * FROM UserEntity  WHERE userId = :userId LIMIT 1")
    suspend fun getUserFriends(userId: Long): UserFriends?

}