package com.darcy.kmpdemo.storage.database.tables

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity
data class OneTimePreKeyEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var userId: Long = 0,
    var privateKey: String = "",
    var publicKey: String = "",
) {

}