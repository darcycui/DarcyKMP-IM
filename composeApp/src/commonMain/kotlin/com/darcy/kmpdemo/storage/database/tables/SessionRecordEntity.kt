package com.darcy.kmpdemo.storage.database.tables

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(
    indices = [
        Index(value = ["localUserId", "remoteUserId"], unique = true)
    ]
)
data class SessionRecordEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var localUserId: Long = 0,
    var remoteUserId: Long = 0,

    var remoteIdentityKey: String = "",
    var remoteDHKey: String = "",
    var localEphemeralPrivateKey: String = "",
    var localEphemeralPublicKey: String = "",

    var rootKey: String = "",
    var sendingChainKey: String = "",
    var receivingChainKey: String = "",

    var receivingChainIndex: Long = 0,

    var N: Long = 0, // N sendingChainMessageIndex
    var PN: Long = 0,// PN previousSendingChainLength

    var createdTime: String = "",
    var updatedTime: String = ""
)