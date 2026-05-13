package com.darcy.kmpdemo.storage.database.tables

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity
data class UserEntity (
    @PrimaryKey(autoGenerate = true)
    val userId: Long = 0,
    val name: String = "",
    val nickName: String = "",
    val age: Int = 0,
    val sex: String = "",
    val avatar: String = "",
    val createdTime: Long = 0,
    val updatedTime: Long = 0,
    val deletedTime: Long = 0,
){
    companion object {
        fun empty(): UserEntity {
            return UserEntity()
        }
    }
}
