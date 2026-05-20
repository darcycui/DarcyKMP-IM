package com.darcy.kmpdemo.x3dh.usecase

import com.darcy.kmpdemo.storage.database.getDarcyIMDatabase
import com.darcy.kmpdemo.storage.database.tables.SessionRecordEntity
import com.darcy.kmpdemo.ui.base.IUseCase
import com.darcy.kmpdemo.utils.EncryptUtil
import com.darcy.kmpdemo.utils.hexStrToBytes
import com.darcy.kmpdemo.utils.toBytes
import com.darcy.kmpdemo.utils.toPrivateKey
import com.darcy.kmpdemo.utils.toPublicKey
import com.darcy.kmpdemo.x3dh.MessageKey
import com.darcy.kmpdemo.x3dh.chain.ChainKey
import com.darcy.kmpdemo.x3dh.chain.HKDF1
import com.darcy.kmpdemo.x3dh.exchange.ECCExchangeHelper

class DoubleRatchetReceiveStepUseCase : IUseCase<MessageKey> {
    private val sessionRecordDao = getDarcyIMDatabase().sessionRecordDao()
    override suspend fun invoke(params: Map<String, String>): Result<MessageKey> {
        val localUserId = params["localUserId"]?.toLongOrNull()
            ?: return Result.failure(Exception("localUserId is null"))
        val remoteUserId = params["remoteUserId"]?.toLongOrNull()
            ?: return Result.failure(Exception("remoteUserId is null"))
        val remoteDHKey = params["remoteDHKey"]?.hexStrToBytes()?.toPublicKey() ?: return Result.failure(
            Exception("remoteDHKey is null")
        )
        val sessionRecord =
            sessionRecordDao.getByUserId(localUserId, remoteUserId) ?: return Result.failure(
                Exception("sessionRecord is null")
            )
        val lastRootKey = sessionRecord.rootKey.hexStrToBytes()
        EncryptUtil.log("$localUserId 根密钥(旧):", lastRootKey)

        // DH 棘轮同步
        val localEphemeralPrivateKey = sessionRecord.localEphemeralPrivateKey.hexStrToBytes().toPrivateKey()
        val localEphemeralPublicKey = sessionRecord.localEphemeralPublicKey.hexStrToBytes().toPublicKey()
        EncryptUtil.log("$localUserId 本地公钥:", localEphemeralPublicKey)
        EncryptUtil.log("$localUserId DH 私钥:", localEphemeralPrivateKey)
        EncryptUtil.log("$localUserId DH 公钥:", remoteDHKey)
        val newDH = ECCExchangeHelper.getSharedSecret(localEphemeralPrivateKey, remoteDHKey)
        EncryptUtil.log("$localUserId DH 棘轮同步:", newDH)
        // KDF 棘轮步进
        val hkdf = HKDF1()
        val dhRatchetAlice =
            hkdf.deriveSecrets(newDH, lastRootKey, "DHInfo".encodeToByteArray(), 64) // 盐:K1
        val pairAlice = EncryptUtil.splitArray64(dhRatchetAlice, 32)
        // 根密钥(新)
        val K3 = pairAlice.first
        EncryptUtil.log("$localUserId 根密钥(新):", K3)
        // 接收链密钥
        val K4 = pairAlice.second
        EncryptUtil.log("$localUserId 接收链密钥:", K4)
        // 更新 sessionRecord 数据库
        val newReceivingChainIndex = sessionRecord.receivingChainIndex + 1
        updateSessionRecordByReceive(sessionRecord, K3, K4, newReceivingChainIndex)
        // KDF 棘轮(发送链)步进一次
        val senderChainAlice = ChainKey(hkdf, K4, newReceivingChainIndex)
        val messageKeyAlice = senderChainAlice.getMessageKeys() // 计算消息密钥
        EncryptUtil.log("$localUserId 接收 $remoteUserId 的消息密钥:", messageKeyAlice)
        val messageKey = MessageKey(
            fromUserId = remoteUserId,
            dhPublicKey = localEphemeralPublicKey.toBytes().toHexString(),
            sendingIndex = sessionRecord.sendingChainIndex,
            receivingIndex = newReceivingChainIndex,
            messageKey = messageKeyAlice.toHexString()
        )
        return Result.success(messageKey)
    }

    private suspend fun updateSessionRecordByReceive(
        sessionRecord: SessionRecordEntity,
        K3: ByteArray,
        K4: ByteArray,
        newReceivingChainIndex: Long
    ) {
        val newSessionRecord = sessionRecord.copy(
            rootKey = K3.toHexString(),
            receivingChainIndex = newReceivingChainIndex,
            receivingChainKey = K4.toHexString()
        )
        sessionRecordDao.update(newSessionRecord)
    }
}