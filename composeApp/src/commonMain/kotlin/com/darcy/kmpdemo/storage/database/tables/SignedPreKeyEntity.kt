package com.darcy.kmpdemo.storage.database.tables

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(
    indices = [
        Index(value = ["userId"], unique = true)
    ]
)
data class SignedPreKeyEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var keyId: String = "",
    var identityKeyId: String = "",
    var userId: Long = 0,
    var privateKey: String = "",
    var publicKey: String = "",
    var signature: String = "",
) {

}