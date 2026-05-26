package com.darcy.kmpdemo.x3dh.usecase

import com.darcy.kmpdemo.log.logD
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

class SendDoubleRatchetStepUseCase : IUseCase<Unit, MessageKey> {
    private val sessionRecordDao = getDarcyIMDatabase().sessionRecordDao()
    private val messageReadStatusDao = getDarcyIMDatabase().messageReadStatusDao()
    override suspend fun invoke(params: Map<String, String>, bean: Unit): Result<MessageKey> {
        val localUserId = params["localUserId"]?.toLongOrNull()
            ?: return Result.failure(Exception("localUserId is null"))
        val remoteUserId = params["remoteUserId"]?.toLongOrNull()
            ?: return Result.failure(Exception("remoteUserId is null"))
        val sessionRecord =
            sessionRecordDao.getByUserId(localUserId, remoteUserId) ?: return Result.failure(
                Exception("sessionRecord is null")
            )
        val lastRootKey = sessionRecord.rootKey.hexStrToBytes()
        EncryptUtil.log("$localUserId 根密钥(旧):", lastRootKey)
        val remoteDHKey = sessionRecord.remoteDHKey.hexStrToBytes().toPublicKey()

        var localEphemeralPublicKeyBytes: ByteArray = ByteArray(0)
        // DH 棘轮步进
        val newDH = if (needDHStep(localUserId, remoteUserId)) {
            val localEphemeralKey = ECCExchangeHelper.generateKeyPair()
            val localEphemeralPrivateKey = localEphemeralKey.privateKey
            val localEphemeralPublicKey = localEphemeralKey.publicKey
            localEphemeralPublicKeyBytes = localEphemeralPublicKey.toBytes()
            EncryptUtil.log("$localUserId 本地公钥:", localEphemeralPublicKey)
            EncryptUtil.log("$localUserId DH 私钥:", localEphemeralPrivateKey)
            EncryptUtil.log("$localUserId DH 公钥:", remoteDHKey)
            val result = ECCExchangeHelper.getSharedSecret(localEphemeralPrivateKey, remoteDHKey)
            EncryptUtil.log("$localUserId DH 棘轮步进:", result)
            result
        } else {
            val localEphemeralPrivateKey =
                sessionRecord.localEphemeralPrivateKey.hexStrToBytes().toPrivateKey()
            val localEphemeralPublicKey =
                sessionRecord.localEphemeralPublicKey.hexStrToBytes().toPublicKey()
            localEphemeralPublicKeyBytes = localEphemeralPublicKey.toBytes()
            EncryptUtil.log("$localUserId 本地公钥:", localEphemeralPublicKey)
            EncryptUtil.log("$localUserId DH 私钥:", localEphemeralPrivateKey)
            EncryptUtil.log("$localUserId DH 公钥:", remoteDHKey)
            val result = ECCExchangeHelper.getSharedSecret(localEphemeralPrivateKey, remoteDHKey)
            EncryptUtil.log("$localUserId DH 棘轮无需步进:", result)
            result
        }

        // KDF 棘轮步进
        val hkdf = HKDF1()
        val dhRatchetAlice =
            hkdf.deriveSecrets(newDH, lastRootKey, "DHInfo".encodeToByteArray(), 64) // 盐:K1
        val pairAlice = EncryptUtil.splitArray64(dhRatchetAlice, 32)
        // 根密钥(新)
        val K3 = pairAlice.first
        EncryptUtil.log("$localUserId 根密钥(新):", K3)
        // 发送链密钥
        val K4 = pairAlice.second
        EncryptUtil.log("$localUserId 发送链密钥:", K4)
        // 更新 sessionRecord 数据库
        val newSendingChainIndex = sessionRecord.sendingChainIndex + 1
        logD("$localUserId 发送链索引: $newSendingChainIndex")
        updateSessionRecordBySend(sessionRecord, K3, K4, newSendingChainIndex)
        // KDF 棘轮(发送链)步进一次
        val senderChainAlice = ChainKey(hkdf, K4, newSendingChainIndex)
        val messageKeyAlice = senderChainAlice.getMessageKeys() // 计算消息密钥
        EncryptUtil.log("$localUserId 发送 $remoteUserId 的消息密钥:", messageKeyAlice)
        val messageKey = MessageKey(
            fromUserId = localUserId,
            dhPublicKey = localEphemeralPublicKeyBytes.toHexString(),
            sendingIndex = newSendingChainIndex,
            receivingIndex = sessionRecord.receivingChainIndex,
            messageKey = messageKeyAlice.toHexString(),
        )
        return Result.success(messageKey)
    }

    private suspend fun updateSessionRecordBySend(
        sessionRecord: SessionRecordEntity,
        K3: ByteArray,
        K4: ByteArray,
        newSendingChainIndex: Long
    ) {
        val newSessionRecord = sessionRecord.copy(
            rootKey = K3.toHexString(),
            sendingChainIndex = newSendingChainIndex,
            sendingChainKey = K4.toHexString()
        )
        sessionRecordDao.update(newSessionRecord)
    }

    private suspend fun needDHStep(localUserId: Long, remoteUserId: Long): Boolean {
        val lastMessageReadStatus = messageReadStatusDao.findByUserIdAndMessageId(
            localUserId, remoteUserId.toString()
        )
        if (lastMessageReadStatus == null) {
            return true
        }
        return lastMessageReadStatus.isRead
    }
}