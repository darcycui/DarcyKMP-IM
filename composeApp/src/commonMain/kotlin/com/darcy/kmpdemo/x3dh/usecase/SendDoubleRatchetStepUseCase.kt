package com.darcy.kmpdemo.x3dh.usecase

import com.darcy.kmpdemo.log.logD
import com.darcy.kmpdemo.log.logW
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

        var localEphemeralPublicKeyBytes: ByteArray
        var newN: Long
        val newPN: Long

        if (needDHStep(sessionRecord)) {
            val localEphemeralKey = ECCExchangeHelper.generateKeyPair()
            val localEphemeralPrivateKey = localEphemeralKey.privateKey
            val localEphemeralPublicKey = localEphemeralKey.publicKey
            localEphemeralPublicKeyBytes = localEphemeralPublicKey.toBytes()
            EncryptUtil.log("$localUserId 本地公钥:", localEphemeralPublicKey)
            EncryptUtil.log("$localUserId DH 私钥:", localEphemeralPrivateKey)
            EncryptUtil.log("$localUserId DH 公钥:", remoteDHKey)
            val newDHSharedSecret =
                ECCExchangeHelper.getSharedSecret(localEphemeralPrivateKey, remoteDHKey)
            newN = 1
            newPN = sessionRecord.N
            EncryptUtil.log("$localUserId DH 棘轮步进:", newDHSharedSecret)
            val dhRatchetAlice =
                HKDF1().deriveSecrets(
                    newDHSharedSecret,
                    lastRootKey,
                    "DHInfo".encodeToByteArray(),
                    64
                )
            val pairAlice = EncryptUtil.splitArray64(dhRatchetAlice, 32)
            val newRootKey = pairAlice.first
            EncryptUtil.log("$localUserId 根密钥(新):", newRootKey)
            val newSendingChainKey = pairAlice.second
            EncryptUtil.log("$localUserId 发送链密钥:", newSendingChainKey)
            logD("$localUserId 发送链索引: N=$newN, PN=$newPN")
            updateSessionRecordBySend(
                sessionRecord, newRootKey, newSendingChainKey, newN, newPN
            )
            val senderChainAlice = ChainKey(HKDF1(), newSendingChainKey, newN)
            val messageKeyTriple = senderChainAlice.getMessageKeyTriple()
            val messageKeyBytes = messageKeyTriple.first
            EncryptUtil.log("$localUserId 发送 $remoteUserId 的消息密钥:", messageKeyBytes)
            val messageKey = MessageKey(
                fromUserId = localUserId,
                dhPublicKey = localEphemeralPublicKeyBytes.toHexString(),
                messageKey = messageKeyBytes.toHexString(),
                nKey = newN,
                pnKey = newPN,
            )
            return Result.success(messageKey)

        } else {
            val localEphemeralPrivateKey =
                sessionRecord.localEphemeralPrivateKey.hexStrToBytes().toPrivateKey()
            val localEphemeralPublicKey =
                sessionRecord.localEphemeralPublicKey.hexStrToBytes().toPublicKey()
            localEphemeralPublicKeyBytes = localEphemeralPublicKey.toBytes()
            EncryptUtil.log("$localUserId 本地公钥:", localEphemeralPublicKey)
            EncryptUtil.log("$localUserId DH 私钥:", localEphemeralPrivateKey)
            EncryptUtil.log("$localUserId DH 公钥:", remoteDHKey)
            newN = sessionRecord.N + 1
            newPN = sessionRecord.PN
            logW("$localUserId DH 棘轮无需步进:")
            logD("$localUserId 发送链索引: N=$newN, PN=$newPN")
            val newSendingChainKey = ChainKey(
                HKDF1(),
                sessionRecord.sendingChainKey.hexStrToBytes(),
                sessionRecord.N
            ).getNextChainKey()
            updateSessionRecordBySend(
                sessionRecord,
                sessionRecord.rootKey.hexStrToBytes(),
                newSendingChainKey.getKey(),
                newN, newPN
            )
            val messageKeyTriple = newSendingChainKey.getMessageKeyTriple()
            val messageKeyBytes = messageKeyTriple.first
            EncryptUtil.log("$localUserId 发送 $remoteUserId 的消息密钥:", messageKeyBytes)
            val messageKey = MessageKey(
                fromUserId = localUserId,
                dhPublicKey = localEphemeralPublicKeyBytes.toHexString(),
                messageKey = messageKeyBytes.toHexString(),
                nKey = newN,
                pnKey = newPN,
            )
            return Result.success(messageKey)
        }
    }

    private suspend fun updateSessionRecordBySend(
        sessionRecord: SessionRecordEntity,
        newRootKey: ByteArray,
        newSendingChainKey: ByteArray,
        newN: Long,
        newPN: Long
    ) {
        val newSessionRecord = sessionRecord.copy(
            rootKey = newRootKey.toHexString(),
            sendingChainKey = newSendingChainKey.toHexString(),
            N = newN,
            PN = newPN,
            updatedTime = TimePlatform.getCurrentTimeStamp()
        )
        sessionRecordDao.update(newSessionRecord)
    }

    /**
     * 是否需要 DH 步骤
     * 发送第一条消息时 需要 DH 步骤
     */
    private fun needDHStep(sessionRecord: SessionRecordEntity): Boolean {
        return sessionRecord.remoteDHKey.isEmpty()
    }
}
