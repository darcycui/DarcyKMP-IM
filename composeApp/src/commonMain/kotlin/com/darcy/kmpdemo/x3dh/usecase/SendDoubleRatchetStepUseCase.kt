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
        var newSendingChainMessageIndex = sessionRecord.sendingChainMessageIndex
        var newPreviousSendingChainLength = sessionRecord.previousSendingChainLength

        val needsDHStep = needDHStep(localUserId, remoteUserId)

        val newDHSharedSecret = if (needsDHStep) {
            newPreviousSendingChainLength = sessionRecord.sendingChainMessageIndex

            val localEphemeralKey = ECCExchangeHelper.generateKeyPair()
            val localEphemeralPrivateKey = localEphemeralKey.privateKey
            val localEphemeralPublicKey = localEphemeralKey.publicKey
            localEphemeralPublicKeyBytes = localEphemeralPublicKey.toBytes()
            EncryptUtil.log("$localUserId 本地公钥:", localEphemeralPublicKey)
            EncryptUtil.log("$localUserId DH 私钥:", localEphemeralPrivateKey)
            EncryptUtil.log("$localUserId DH 公钥:", remoteDHKey)

            newSendingChainMessageIndex = 0

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

            newSendingChainMessageIndex = sessionRecord.sendingChainMessageIndex + 1

            val result = ECCExchangeHelper.getSharedSecret(localEphemeralPrivateKey, remoteDHKey)
            EncryptUtil.log("$localUserId DH 棘轮无需步进:", result)
            result
        }

        val hkdf = HKDF1()
        val dhRatchetAlice =
            hkdf.deriveSecrets(newDHSharedSecret, lastRootKey, "DHInfo".encodeToByteArray(), 64)
        val pairAlice = EncryptUtil.splitArray64(dhRatchetAlice, 32)

        val newRootKey = pairAlice.first
        EncryptUtil.log("$localUserId 根密钥(新):", newRootKey)

        val newSendingChainKey = pairAlice.second
        EncryptUtil.log("$localUserId 发送链密钥:", newSendingChainKey)

        val N = newSendingChainMessageIndex
        val PN = newPreviousSendingChainLength

        logD("$localUserId 发送链索引: N=$N, PN=$PN")

        updateSessionRecordBySend(
            sessionRecord,
            newRootKey,
            newSendingChainKey,
            newSendingChainMessageIndex,
            newPreviousSendingChainLength
        )

        val senderChainAlice = ChainKey(hkdf, newSendingChainKey, N)
        val messageKeyTriple = senderChainAlice.getMessageKeyTriple()
        val messageKeyBytes = messageKeyTriple.first
        EncryptUtil.log("$localUserId 发送 $remoteUserId 的消息密钥:", messageKeyBytes)

        val messageKey = MessageKey(
            fromUserId = localUserId,
            dhPublicKey = localEphemeralPublicKeyBytes.toHexString(),
            messageKey = messageKeyBytes.toHexString(),
            nKey = N,
            pnKey = PN,
        )
        return Result.success(messageKey)
    }

    private suspend fun updateSessionRecordBySend(
        sessionRecord: SessionRecordEntity,
        newRootKey: ByteArray,
        newSendingChainKey: ByteArray,
        newSendingChainMessageIndex: Long,
        newPreviousSendingChainLength: Long
    ) {
        val newSessionRecord = sessionRecord.copy(
            rootKey = newRootKey.toHexString(),
            sendingChainKey = newSendingChainKey.toHexString(),
            sendingChainMessageIndex = newSendingChainMessageIndex,
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
