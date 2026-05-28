package com.darcy.kmpdemo.x3dh.usecase

import com.darcy.kmpdemo.storage.database.daos.SessionRecordDao
import com.darcy.kmpdemo.storage.database.getDarcyIMDatabase
import com.darcy.kmpdemo.storage.database.tables.SessionRecordEntity
import com.darcy.kmpdemo.ui.base.IUseCase
import com.darcy.kmpdemo.utils.EncryptUtil
import com.darcy.kmpdemo.utils.hexStrToBytes

class SaveBobSessionRecordUseCase : IUseCase<Unit, Boolean> {
    private val identityKeyDao = getDarcyIMDatabase().identityKeyDao()
    private val signedPreKeyDao = getDarcyIMDatabase().signedPreKeyDao()
    private val sessionRecordDao: SessionRecordDao = getDarcyIMDatabase().sessionRecordDao()
    override suspend fun invoke(params: Map<String, String>, bean: Unit): Result<Boolean> {
        return runCatching {
            val localUserId = params["localUserId"]?.toLongOrNull() ?: return Result.failure(Exception("localUserId is null"))
            val remoteUserId = params["remoteUserId"]?.toLongOrNull() ?: return Result.failure(Exception("remoteUserId is null"))
            val x3DHKeyStr = params["bobX3DHKey"] ?: return Result.failure(Exception("x3DHKey is null"))
            val aliceIdentityKey = params["aliceIdentityKey"] ?: return Result.failure(Exception("aliceIdentityKey is null"))
            val aliceEphemeralKey = params["aliceEphemeralKey"] ?: return Result.failure(Exception("aliceEphemeralKey is null"))
            val bobIdentityKey = identityKeyDao.getByUserId(localUserId) ?: return Result.failure(Exception("bobIdentityKey is null"))
            val bobSignedPreKey = signedPreKeyDao.getByUserId(localUserId) ?: return Result.failure(Exception("bobSignedPreKey is null"))
            val pairBob = EncryptUtil.splitArray64(x3DHKeyStr.hexStrToBytes(), 32)
            val K1 = pairBob.first
            val K2 = pairBob.second
            sessionRecordDao.getByUserId(localUserId, remoteUserId)?.apply {
                sessionRecordDao.delete(this)
            }
            sessionRecordDao.insert(SessionRecordEntity(
                localUserId = localUserId,
                remoteUserId = remoteUserId,
                remoteIdentityKey = aliceIdentityKey,
                remoteDHKey = aliceEphemeralKey,
                localEphemeralPrivateKey = bobSignedPreKey.privateKey,
                localEphemeralPublicKey = bobSignedPreKey.publicKey,
                rootKey = K1.toHexString(),
                receivingChainKey = "", // 第一次保存的时候 bob接收链密钥为空
                sendingChainKey = K2.toHexString(),
                receivingChainIndex = -1,
                sendingChainMessageIndex = 0,
                previousSendingChainLength = 0
            ))

            Result.success(true)
        }.onFailure {
            it.printStackTrace()
        }.getOrElse {
            Result.success(false)
        }
    }
}