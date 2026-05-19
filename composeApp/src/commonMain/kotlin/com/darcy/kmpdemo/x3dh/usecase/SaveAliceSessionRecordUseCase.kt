package com.darcy.kmpdemo.x3dh.usecase

import com.darcy.kmpdemo.storage.database.daos.SessionRecordDao
import com.darcy.kmpdemo.storage.database.getDarcyIMDatabase
import com.darcy.kmpdemo.storage.database.tables.SessionRecordEntity
import com.darcy.kmpdemo.ui.base.IUseCase
import com.darcy.kmpdemo.utils.EncryptUtil
import com.darcy.kmpdemo.utils.hexStrToBytes

class SaveAliceSessionRecordUseCase : IUseCase<Boolean> {
    private val sessionRecordDao: SessionRecordDao = getDarcyIMDatabase().sessionRecordDao()
    override suspend fun invoke(params: Map<String, String>): Result<Boolean> {
        return runCatching {
            val aliceUserId = params["aliceUserId"]?.toLongOrNull() ?: return Result.failure(Exception("aliceUserId is null"))
            val bobUserId = params["bobUserId"]?.toLongOrNull() ?: return Result.failure(Exception("bobUserId is null"))
            val x3DHKeyStr = params["aliceX3DHKey"] ?: return Result.failure(Exception("x3DHKey is null"))
            val aliceEphemeralPrivateKey = params["aliceEphemeralPrivateKey"] ?: return Result.failure(Exception("aliceEphemeralPrivateKey is null"))
            val aliceEphemeralPublicKey = params["aliceEphemeralPublicKey"] ?: return Result.failure(Exception("aliceEphemeralPublicKey is null"))
            val bobSignedPreKey = params["bobSignedPreKey"] ?: return Result.failure(Exception("bobSignedPreKey is null"))
            val bobIdentityKey = params["bobIdentityKey"] ?: return Result.failure(Exception("bobIdentityKey is null"))
            val pairAlice = EncryptUtil.splitArray64(x3DHKeyStr.hexStrToBytes(), 32)
            // Alice Root密钥
            val K1 = pairAlice.first
            // Alice 接收链密钥
            val K2 = pairAlice.second
            sessionRecordDao.insert(
                SessionRecordEntity(
                    localUserId = aliceUserId,
                    remoteUserId = bobUserId,
                    remoteIdentityKey = bobIdentityKey,
                    remoteDHKey = bobSignedPreKey, // 第一个DH密钥 使用Bob的SignedPreKey
                    localEphemeralPrivateKey = aliceEphemeralPrivateKey,
                    localEphemeralPublicKey = aliceEphemeralPublicKey,
                    rootKey = K1.toHexString(),
                    receivingChainKey = K2.toHexString(),
                    sendingChainKey = "", // 第一次保存的时候 alice发送链密钥为空
                    sendingChainIndex = 0,
                    receivingChainIndex = 0,
                )
            )
            Result.success(true)
        }.onFailure {
            it.printStackTrace()
            Result.failure<Boolean>(it)
        }.getOrElse { Result.success(false) }
    }
}