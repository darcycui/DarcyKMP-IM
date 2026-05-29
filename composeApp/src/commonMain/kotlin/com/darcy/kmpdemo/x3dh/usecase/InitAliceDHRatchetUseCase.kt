package com.darcy.kmpdemo.x3dh.usecase

import com.darcy.kmpdemo.log.logD
import com.darcy.kmpdemo.log.logW
import com.darcy.kmpdemo.storage.database.daos.SessionRecordDao
import com.darcy.kmpdemo.storage.database.getDarcyIMDatabase
import com.darcy.kmpdemo.ui.base.IUseCase
import com.darcy.kmpdemo.utils.EncryptUtil
import com.darcy.kmpdemo.utils.hexStrToBytes
import com.darcy.kmpdemo.utils.toBytes
import com.darcy.kmpdemo.utils.toPublicKey
import com.darcy.kmpdemo.x3dh.chain.ChainKey
import com.darcy.kmpdemo.x3dh.chain.HKDF1
import com.darcy.kmpdemo.x3dh.exchange.ECCExchangeHelper

class InitAliceDHRatchetUseCase: IUseCase<Unit, Boolean> {
    private val sessionRecordDao: SessionRecordDao = getDarcyIMDatabase().sessionRecordDao()
    override suspend fun invoke(
        params: Map<String, String>,
        bean: Unit
    ): Result<Boolean> {
        val localUserId = params["aliceUserId"]?.toLongOrNull()
            ?: return Result.failure(Exception("aliceUserId is null"))
        val remoteUserId = params["bobUserId"]?.toLongOrNull()
            ?: return Result.failure(Exception("bobUserId is null"))
        val bobSignedPreKey = params["bobSignedPreKey"]
            ?: return Result.failure(Exception("bobSignedPreKey is null"))
        val sessionRecord = sessionRecordDao.getByUserId(localUserId, remoteUserId)
            ?: return Result.failure(Exception("sessionRecord is null"))
        val K1 = sessionRecord.rootKey
        // alice 初始化DH棘轮
        logW("$localUserId DH 棘轮步进开始")
        val localEphemeralKey = ECCExchangeHelper.generateKeyPair()
        val newLocalEphemeralPrivateKey = localEphemeralKey.privateKey
        val newLocalEphemeralPublicKey = localEphemeralKey.publicKey
        EncryptUtil.log("$localUserId 本地新公钥:", newLocalEphemeralPublicKey)
        EncryptUtil.log("$localUserId DH 新私钥:", newLocalEphemeralPrivateKey)
        logD("$localUserId DH 公钥: $bobSignedPreKey")
        val sendingNewDHSharedSecret = ECCExchangeHelper.getSharedSecret(
            newLocalEphemeralPrivateKey,
            bobSignedPreKey.hexStrToBytes().toPublicKey()
        )
        logW("$localUserId DH 棘轮步进完成 ${sendingNewDHSharedSecret.toHexString()}")
        val sendingDHRatchetResult = HKDF1().deriveSecrets(
            sendingNewDHSharedSecret,
            K1.hexStrToBytes(),
            "DHInfo".encodeToByteArray(),
            64
        )
        val sendingPair = EncryptUtil.splitArray64(sendingDHRatchetResult, 32)
        val sendingNewRootKey = sendingPair.first
        EncryptUtil.log("$localUserId 根密钥(新):", sendingNewRootKey)
        val sendingNewChainKey = sendingPair.second
        EncryptUtil.log("$localUserId 发送链初始化密钥:", sendingNewChainKey)
        val sendingChain = ChainKey(HKDF1(), sendingNewChainKey, 0)
        EncryptUtil.log("$localUserId 发送链密钥:", sendingChain.getKey ())

        val updatedCount = sessionRecordDao.update(sessionRecord.copy(
            rootKey = sendingNewRootKey.toHexString(),
            localEphemeralPrivateKey = newLocalEphemeralPrivateKey.toBytes().toHexString(),
            localEphemeralPublicKey = newLocalEphemeralPublicKey.toBytes().toHexString(),
            sendingChainKey = sendingChain.getKey().toHexString(),
        ))
        return if (updatedCount > 0) {
            Result.success(true)
        } else {
            Result.failure(Exception("update sessionRecord failed"))
        }
    }
}