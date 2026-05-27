package com.darcy.kmpdemo.x3dh.usecase

import com.darcy.kmpdemo.log.logD
import com.darcy.kmpdemo.platform.TimePlatform
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
        var newSendingChainIndex = sessionRecord.sendingChainIndex
        var newSendingChainMessageCount = sessionRecord.sendingChainMessageCount
        var newPreviousSendingChainLength = sessionRecord.previousSendingChainLength

        val needsDHStep = needDHStep(localUserId, remoteUserId)

        val newDHSharedSecret = if (needsDHStep) {
            newPreviousSendingChainLength = sessionRecord.sendingChainMessageCount

            val localEphemeralKey = ECCExchangeHelper.generateKeyPair()
            val localEphemeralPrivateKey = localEphemeralKey.privateKey
            val localEphemeralPublicKey = localEphemeralKey.publicKey
            localEphemeralPublicKeyBytes = localEphemeralPublicKey.toBytes()
            EncryptUtil.log("$localUserId 本地公钥:", localEphemeralPublicKey)
            EncryptUtil.log("$localUserId DH 私钥:", localEphemeralPrivateKey)
            EncryptUtil.log("$localUserId DH 公钥:", remoteDHKey)

            newSendingChainIndex = sessionRecord.sendingChainIndex + 1
            newSendingChainMessageCount = 0

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

            newSendingChainMessageCount = sessionRecord.sendingChainMessageCount + 1

            val result = ECCExchangeHelper.getSharedSecret(localEphemeralPrivateKey, remoteDHKey)
            EncryptUtil.log("$localUserId DH 棘轮无需步进:", result)
            result
        }

        val hkdf = HKDF1()
        val dhRatchetAlice =
            hkdf.deriveSecrets(newDHSharedSecret, lastRootKey, "DHInfo".encodeToByteArray(), 64)
        val pairAlice = EncryptUtil.splitArray64(dhRatchetAlice, 32)

        val K3 = pairAlice.first
        EncryptUtil.log("$localUserId 根密钥(新):", K3)

        val K4 = pairAlice.second
        EncryptUtil.log("$localUserId 发送链密钥:", K4)

        val N = newSendingChainMessageCount
        val PN = newPreviousSendingChainLength

        logD("$localUserId 发送链索引: $newSendingChainIndex, N=$N, PN=$PN")

        updateSessionRecordBySend(
            sessionRecord,
            K3, K4,
            newSendingChainIndex,
            newSendingChainMessageCount,
            newPreviousSendingChainLength
        )

        val senderChainAlice = ChainKey(hkdf, K4, N)
        val messageKeyTriple = senderChainAlice.getMessageKeyTriple()
        val messageKeyBytes = messageKeyTriple.first
        EncryptUtil.log("$localUserId 发送 $remoteUserId 的消息密钥:", messageKeyBytes)

        val messageKey = MessageKey(
            fromUserId = localUserId,
            dhPublicKey = localEphemeralPublicKeyBytes.toHexString(),
            sendingIndex = newSendingChainIndex,
            receivingIndex = sessionRecord.receivingChainIndex,
            messageKey = messageKeyBytes.toHexString(),
            N = N,
            PN = PN,
        )

        return Result.success(messageKey)
    }

    private suspend fun updateSessionRecordBySend(
        sessionRecord: SessionRecordEntity,
        newRootKey: ByteArray,
        newSendingChainKey: ByteArray,
        newSendingChainIndex: Long,
        newSendingChainMessageCount: Long,
        newPreviousSendingChainLength: Long
    ) {
        val newSessionRecord = sessionRecord.copy(
            rootKey = newRootKey.toHexString(),
            sendingChainKey = newSendingChainKey.toHexString(),
            sendingChainIndex = newSendingChainIndex,
            sendingChainMessageCount = newSendingChainMessageCount,
            previousSendingChainLength = newPreviousSendingChainLength,
            updatedTime = TimePlatform.getCurrentTimeStamp()
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
