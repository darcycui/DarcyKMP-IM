package com.darcy.kmpdemo.x3dh.usecase

import com.darcy.kmpdemo.storage.database.getDarcyIMDatabase
import com.darcy.kmpdemo.storage.database.tables.SessionRecordEntity
import com.darcy.kmpdemo.ui.base.IUseCase
import com.darcy.kmpdemo.utils.EncryptUtil
import com.darcy.kmpdemo.utils.hexStrToBytes
import com.darcy.kmpdemo.utils.toPublicKey
import com.darcy.kmpdemo.x3dh.chain.ChainKey
import com.darcy.kmpdemo.x3dh.chain.HKDF1
import com.darcy.kmpdemo.x3dh.exchange.ECCExchangeHelper
import dev.whyoleg.cryptography.algorithms.XDH

class DoubleRatchetReceiveStepUseCase : IUseCase<Pair<XDH.PublicKey, ByteArray>> {
    private val sessionRecordDao = getDarcyIMDatabase().sessionRecordDao()
    override suspend fun invoke(params: Map<String, String>): Result<Pair<XDH.PublicKey, ByteArray>> {
        val localUserId = params["localUserId"]?.toLong()
            ?: return Result.failure(Exception("localUserId is null"))
        val remoteUserId = params["remoteUserId"]?.toLong()
            ?: return Result.failure(Exception("remoteUserId is null"))
        val sessionRecord =
            sessionRecordDao.getByUserId(localUserId, remoteUserId) ?: return Result.failure(
                Exception("sessionRecord is null")
            )
        val lastRootKey = sessionRecord.rootKey.hexStrToBytes()
        val remoteDHKey = sessionRecord.remoteDHKey.hexStrToBytes().toPublicKey()

        // DH 棘轮步进
        val localEphemeralKey = ECCExchangeHelper.generateKeyPair()
        val localEphemeralPrivateKey = localEphemeralKey.privateKey
        val localEphemeralPublicKey = localEphemeralKey.publicKey
        val newDH = ECCExchangeHelper.getSharedSecret(localEphemeralPrivateKey, remoteDHKey)
        // KDF 棘轮步进
        val hkdf = HKDF1()
        val dhRatchetAlice =
            hkdf.deriveSecrets(newDH, lastRootKey, "DHInfo".encodeToByteArray(), 64) // 盐:K1
        val pairAlice = EncryptUtil.splitArray64(dhRatchetAlice, 32)
        // Root密钥(新)
        val K3 = pairAlice.first
        // 接收链密钥
        val K4 = pairAlice.second
        // 更新 sessionRecord 数据库
        updateSessionRecordByReceive(sessionRecord, K3, K4)
        // KDF 棘轮(发送链)步进一次
        val senderChainAlice = ChainKey(hkdf, K4, sessionRecord.receivingChainIndex)
        val messageKeyAlice = senderChainAlice.getMessageKeys() // 计算消息密钥
        EncryptUtil.log("$localUserId 接收 $remoteUserId 的消息密钥:", messageKeyAlice)
        return Result.success(Pair(localEphemeralPublicKey, messageKeyAlice))
    }

    private suspend fun updateSessionRecordByReceive(
        sessionRecord: SessionRecordEntity,
        K3: ByteArray,
        K4: ByteArray
    ) {
        val newSessionRecord = sessionRecord.copy(
            rootKey = K3.toHexString(),
            receivingChainIndex = sessionRecord.sendingChainIndex + 1,
            receivingChainKey = K4.toHexString()
        )
        sessionRecordDao.update(newSessionRecord)
    }
}