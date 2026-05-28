package com.darcy.kmpdemo.x3dh.usecase

import com.darcy.kmpdemo.log.logD
import com.darcy.kmpdemo.storage.database.daos.SessionRecordDao
import com.darcy.kmpdemo.storage.database.getDarcyIMDatabase
import com.darcy.kmpdemo.storage.database.tables.SessionRecordEntity
import com.darcy.kmpdemo.ui.base.IUseCase
import com.darcy.kmpdemo.utils.EncryptUtil
import com.darcy.kmpdemo.utils.hexStrToBytes
import com.darcy.kmpdemo.utils.toBytes
import com.darcy.kmpdemo.utils.toPrivateKey
import com.darcy.kmpdemo.utils.toPublicKey
import com.darcy.kmpdemo.x3dh.chain.HKDF1
import com.darcy.kmpdemo.x3dh.exchange.ECCExchangeHelper

class SaveAliceSessionRecordUseCase : IUseCase<Unit, Boolean> {
    private val sessionRecordDao: SessionRecordDao = getDarcyIMDatabase().sessionRecordDao()
    override suspend fun invoke(params: Map<String, String>, bean: Unit): Result<Boolean> {
        return runCatching {
            val localUserId = params["aliceUserId"]?.toLongOrNull()
                ?: return Result.failure(Exception("aliceUserId is null"))
            val remoteUserId = params["bobUserId"]?.toLongOrNull()
                ?: return Result.failure(Exception("bobUserId is null"))
            val x3DHKeyStr = params["aliceX3DHKey"]
                ?: return Result.failure(Exception("x3DHKey is null"))
            val aliceEphemeralPrivateKey = params["aliceEphemeralPrivateKey"]
                ?: return Result.failure(Exception("aliceEphemeralPrivateKey is null"))
            val aliceEphemeralPublicKey = params["aliceEphemeralPublicKey"]
                ?: return Result.failure(Exception("aliceEphemeralPublicKey is null"))
            val bobSignedPreKey = params["bobSignedPreKey"]
                ?: return Result.failure(Exception("bobSignedPreKey is null"))
            val bobIdentityKey = params["bobIdentityKey"]
                ?: return Result.failure(Exception("bobIdentityKey is null"))



            val pairAlice = EncryptUtil.splitArray64(x3DHKeyStr.hexStrToBytes(), 32)
            // Alice 根密钥
            val K1 = pairAlice.first
            // Alice 接收链密钥
            val K2 = pairAlice.second

            // alice 初始化DH棘轮
            val localEphemeralKey = ECCExchangeHelper.generateKeyPair()
            val newLocalEphemeralPrivateKey = localEphemeralKey.privateKey
            val newLocalEphemeralPublicKey = localEphemeralKey.publicKey
            val dhSharedSecret = ECCExchangeHelper.getSharedSecret(
                newLocalEphemeralPrivateKey,
                bobSignedPreKey.hexStrToBytes().toPublicKey())
            EncryptUtil.log("$localUserId 本地新公钥:", newLocalEphemeralPublicKey)
            EncryptUtil.log("$localUserId DH 新私钥:", newLocalEphemeralPrivateKey)
            logD("$localUserId DH 公钥:", bobSignedPreKey)
            val sendingNewDHSharedSecret =
                ECCExchangeHelper.getSharedSecret(newLocalEphemeralPrivateKey, bobSignedPreKey.hexStrToBytes().toPublicKey())
            logD("$localUserId DH 棘轮步进完成")
            val sendingDHRatchetResult = HKDF1().deriveSecrets(
                sendingNewDHSharedSecret,
                K1,
                "DHInfo".encodeToByteArray(),
                64
            )
            val sendingPair = EncryptUtil.splitArray64(sendingDHRatchetResult, 32)
            val sendingNewRootKey = sendingPair.first
            EncryptUtil.log("$localUserId 根密钥(新):", sendingNewRootKey)
            val sendingNewChainKey = sendingPair.second
            EncryptUtil.log("$localUserId 发送链密钥:", sendingNewChainKey)

            sessionRecordDao.getByUserId(localUserId, remoteUserId)?.apply {
                sessionRecordDao.delete(this)
            }
            sessionRecordDao.insert(
                SessionRecordEntity(
                    localUserId = localUserId,
                    remoteUserId = remoteUserId,
                    remoteIdentityKey = bobIdentityKey,
                    remoteDHKey = bobSignedPreKey, // 第一个DH密钥 使用Bob的SignedPreKey
                    localEphemeralPrivateKey = newLocalEphemeralPrivateKey.toBytes().toHexString(),
                    localEphemeralPublicKey = newLocalEphemeralPublicKey.toBytes().toHexString(),
                    rootKey = sendingNewRootKey.toHexString(),
                    receivingChainKey = K2.toHexString(),
                    sendingChainKey = sendingNewChainKey.toHexString(), // 第一次保存的时候 alice发送链密钥为空
                    receivingChainIndex = 0,
                    N = 0,
                    PN = 0
                )
            )
            Result.success(true)
        }.onFailure {
            it.printStackTrace()
            Result.failure<Boolean>(it)
        }.getOrElse { Result.success(false) }
    }
}