package com.darcy.kmpdemo.x3dh.usecase

import com.darcy.kmpdemo.log.logD
import com.darcy.kmpdemo.log.logI
import com.darcy.kmpdemo.log.logW
import com.darcy.kmpdemo.platform.TimePlatform
import com.darcy.kmpdemo.storage.database.getDarcyIMDatabase
import com.darcy.kmpdemo.storage.database.tables.SessionRecordEntity
import com.darcy.kmpdemo.storage.database.tables.SkippedMessageKeyEntity
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
import dev.whyoleg.cryptography.algorithms.XDH

class ReceiveDoubleRatchetStepUseCase : IUseCase<Unit, MessageKey> {
    private val sessionRecordDao = getDarcyIMDatabase().sessionRecordDao()
    private val skippedMessageKeyDao = getDarcyIMDatabase().skippedMessageKeyDao()

    override suspend fun invoke(params: Map<String, String>, bean: Unit): Result<MessageKey> {
        val localUserId = params["localUserId"]?.toLongOrNull()
            ?: return Result.failure(Exception("localUserId is null"))
        val remoteUserId = params["remoteUserId"]?.toLongOrNull()
            ?: return Result.failure(Exception("remoteUserId is null"))
        val remoteDHKey =
            params["remoteDHKey"]?.hexStrToBytes()?.toPublicKey() ?: return Result.failure(
                Exception("remoteDHKey is null")
            )
        val msgId = params["msgId"] ?: return Result.failure(Exception("msgId is null"))
        val N = params["N_KEY"]?.toLongOrNull()
            ?: return Result.failure(Exception("N (message number in sending chain) is null"))
        val PN = params["PN_KEY"]?.toLongOrNull()
            ?: return Result.failure(Exception("PN (previous chain length) is null"))

        val sessionRecord =
            sessionRecordDao.getByUserId(localUserId, remoteUserId) ?: return Result.failure(
                Exception("sessionRecord is null")
            )

        logD("$localUserId 收到消息 - 消息ID:$msgId, N=$N, PN=$PN, 当前接收链索引:${sessionRecord.receivingChainIndex}")

        return when {
            N > sessionRecord.receivingChainIndex -> {
                logI("$localUserId 顺序或未来消息，处理并缓存跳过密钥")
                handleInOrderMessage(
                    localUserId, remoteUserId, msgId,
                    sessionRecord, remoteDHKey, N, PN,
                )
            }

            N <= sessionRecord.receivingChainIndex -> {
                logW("$localUserId 重复消息或过期消息（N:$N <= 当前:${sessionRecord.receivingChainIndex}）")
                handleDuplicateOrSkippedMessage(
                    localUserId, remoteUserId, msgId,
                    sessionRecord, remoteDHKey, N
                )
            }

            else -> {
                logW("$localUserId 异常情况")
                Result.failure(Exception("消息索引异常"))
            }
        }
    }

    private suspend fun handleDuplicateOrSkippedMessage(
        localUserId: Long,
        remoteUserId: Long,
        msgId: String,
        sessionRecord: SessionRecordEntity,
        remoteDHKey: XDH.PublicKey,
        N: Long
    ): Result<MessageKey> {
        val skippedKey = skippedMessageKeyDao.findByIndexAndDHKey(
            localUserId, remoteUserId, N, remoteDHKey.toBytes().toHexString()
        )

        if (skippedKey != null) {
            logI("$localUserId 找到跳过的消息密钥 - 消息ID:$msgId, 索引:$N")
            val messageKey = MessageKey(
                fromUserId = remoteUserId,
                dhPublicKey = skippedKey.dhPublicKey,
                messageKey = skippedKey.messageKey,
                macKey = skippedKey.macKey,
                iv = skippedKey.iv,
            )

//            val deleted = skippedMessageKeyDao.delete(skippedKey)
//            if (deleted > 0) {
//                logD("$localUserId 已删除跳过的密钥缓存 - 索引:$N")
//            }

            return Result.success(messageKey)
        } else {
            logW("$localUserId 消息已过期或重复，且无缓存密钥 - 索引:$N")
            return Result.failure(Exception("消息已过期或重复，索引:$N"))
        }
    }

    private suspend fun handleInOrderMessage(
        localUserId: Long,
        remoteUserId: Long,
        msgId: String,
        sessionRecord: SessionRecordEntity,
        remoteDHKey: XDH.PublicKey,
        N: Long,
        PN: Long,
    ): Result<MessageKey> {
        val lastRootKey = sessionRecord.rootKey.hexStrToBytes()
        var localEphemeralPublicKeyBytes: ByteArray

        val needsDHStep = needDHStep(remoteDHKey, sessionRecord.remoteDHKey)

        if (needsDHStep) {
//            cacheSkippedMessageKeysForDHStep(
//                localUserId, remoteUserId, sessionRecord,
//                PN, N, remoteDHKey
//            )
            val localEphemeralPrivateKey =
                sessionRecord.localEphemeralPrivateKey.hexStrToBytes().toPrivateKey()
            val localEphemeralPublicKey =
                sessionRecord.localEphemeralPublicKey.hexStrToBytes().toPublicKey()
            logW("$localUserId DH 棘轮步进开始")
            logD("$localUserId 对方发送链信息: N=$N, PN=$PN")
            localEphemeralPublicKeyBytes = localEphemeralPublicKey.toBytes()
            EncryptUtil.log("$localUserId 本地公钥:", localEphemeralPublicKey)
            EncryptUtil.log("$localUserId DH 私钥:", localEphemeralPrivateKey)
            EncryptUtil.log("$localUserId DH 公钥:", remoteDHKey)
            val receivingNewDHSharedSecret =
                ECCExchangeHelper.getSharedSecret(localEphemeralPrivateKey, remoteDHKey)
            logW("$localUserId DH 棘轮步进完成 ${receivingNewDHSharedSecret.toHexString()}")
            EncryptUtil.log("$localUserId 根密钥(旧):", lastRootKey)
            val receivingDHRatchetResult = HKDF1().deriveSecrets(
                receivingNewDHSharedSecret,
                lastRootKey,
                "DHInfo".encodeToByteArray(),
                64
            )
            val receivingPair = EncryptUtil.splitArray64(receivingDHRatchetResult, 32)
            val receivingNewRootKey = receivingPair.first
            EncryptUtil.log("$localUserId 根密钥(新):", receivingNewRootKey)
            val receivingNewChainKey = receivingPair.second
            EncryptUtil.log("$localUserId 接收链初始化密钥:", receivingNewChainKey)
            val receiverChain = ChainKey(HKDF1(), receivingNewChainKey, 0).getNextChainKey()
            EncryptUtil.log("$localUserId 接收链密钥:", receiverChain.getKey())

            val messageKeyBytes = receiverChain.getMessageKeys()
            EncryptUtil.logI("$localUserId 接收 $remoteUserId 的消息密钥:", messageKeyBytes)
            val messageKey = MessageKey(
                fromUserId = remoteUserId,
                dhPublicKey = localEphemeralPublicKeyBytes.toHexString(),
                messageKey = messageKeyBytes.toHexString()
            )
            sessionRecordDao.update(
                sessionRecord.copy(
                    remoteDHKey = remoteDHKey.toBytes().toHexString(),
                    rootKey = receivingNewRootKey.toHexString(),
                    receivingChainKey = receiverChain.getKey().toHexString(),
                    receivingChainIndex = N,
                    updatedTime = TimePlatform.getCurrentTimeStamp()
                )
            )
            logD("$localUserId 处理消息成功 - 索引:$N, 消息ID:$msgId")
            // cleanupOldSkippedKeys(localUserId, remoteUserId)

            logW("$localUserId DH 棘轮步进开始")
            val localEphemeralKey = ECCExchangeHelper.generateKeyPair()
            val newLocalEphemeralPrivateKey = localEphemeralKey.privateKey
            val newLocalEphemeralPublicKey = localEphemeralKey.publicKey
            localEphemeralPublicKeyBytes = newLocalEphemeralPublicKey.toBytes()
            EncryptUtil.log("$localUserId 本地新公钥:", newLocalEphemeralPublicKey)
            EncryptUtil.log("$localUserId DH 新私钥:", newLocalEphemeralPrivateKey)
            EncryptUtil.log("$localUserId DH 公钥:", remoteDHKey)
            val sendingNewDHSharedSecret =
                ECCExchangeHelper.getSharedSecret(newLocalEphemeralPrivateKey, remoteDHKey)
            logW("$localUserId DH 棘轮步进完成 ${sendingNewDHSharedSecret.toHexString()}")

            EncryptUtil.log("$localUserId 根密钥(旧):", receivingNewRootKey)
            val sendingDHRatchetResult = HKDF1().deriveSecrets(
                sendingNewDHSharedSecret,
                receivingNewRootKey,
                "DHInfo".encodeToByteArray(),
                64
            )
            val sendingPair = EncryptUtil.splitArray64(sendingDHRatchetResult, 32)
            val sendingNewRootKey = sendingPair.first
            EncryptUtil.log("$localUserId 根密钥(新):", sendingNewRootKey)
            val sendingNewChainKey = sendingPair.second
            EncryptUtil.log("$localUserId 发送链初始化密钥:", sendingNewChainKey)
            val senderChain = ChainKey(HKDF1(), sendingNewChainKey, 0)
            EncryptUtil.log("$localUserId 发送链密钥:", senderChain.getKey())
            sessionRecordDao.update(
                sessionRecord.copy(
                    remoteDHKey = remoteDHKey.toBytes().toHexString(),
                    rootKey = sendingNewRootKey.toHexString(),
                    receivingChainKey = receiverChain.getKey().toHexString(),
                    sendingChainKey = senderChain.getKey().toHexString(),
                    receivingChainIndex = N,
                    localEphemeralPrivateKey = newLocalEphemeralPrivateKey.toBytes().toHexString(),
                    localEphemeralPublicKey = newLocalEphemeralPublicKey.toBytes().toHexString(),
                    updatedTime = TimePlatform.getCurrentTimeStamp(),
                    N = 0,  // 重置N
                    PN = PN, // todo 更新 PN ?
                )
            )

            return Result.success(messageKey)
        } else {
            localEphemeralPublicKeyBytes = sessionRecord.localEphemeralPublicKey.hexStrToBytes()
            logW("$localUserId DH 棘轮无需步进")
            val localEphemeralPrivateKey =
                sessionRecord.localEphemeralPrivateKey.hexStrToBytes().toPrivateKey()
            val localEphemeralPublicKey =
                sessionRecord.localEphemeralPublicKey.hexStrToBytes().toPublicKey()
            EncryptUtil.log("$localUserId 本地公钥:", localEphemeralPublicKey)
            EncryptUtil.log("$localUserId DH 私钥:", localEphemeralPrivateKey)
            EncryptUtil.log("$localUserId DH 公钥:", remoteDHKey)
            val newReceivingChainKey = ChainKey(
                HKDF1(),
                sessionRecord.receivingChainKey.hexStrToBytes(),
                sessionRecord.receivingChainIndex
            ).getNextChainKey()
            EncryptUtil.log("$localUserId 接收链密钥:", newReceivingChainKey.getKey())
            val messageKeyBytes = newReceivingChainKey.getMessageKeys()
            EncryptUtil.logI("$localUserId 接收 $remoteUserId 的消息密钥:", messageKeyBytes)
            val messageKey = MessageKey(
                fromUserId = remoteUserId,
                dhPublicKey = localEphemeralPublicKeyBytes.toHexString(),
                messageKey = messageKeyBytes.toHexString()
            )
            logD("$localUserId 处理消息成功 - 索引:$N, 消息ID:$msgId")
            sessionRecordDao.update(
                sessionRecord.copy(
                    remoteDHKey = remoteDHKey.toBytes().toHexString(),
                    receivingChainKey = newReceivingChainKey.getKey().toHexString(),
                    receivingChainIndex = N,
                    updatedTime = TimePlatform.getCurrentTimeStamp()
                )
            )
            return Result.success(messageKey)
        }
    }

    private suspend fun cleanupOldSkippedKeys(
        localUserId: Long,
        remoteUserId: Long
    ) {
        val currentSessionRecord = sessionRecordDao.getByUserId(localUserId, remoteUserId) ?: return
        val threshold = currentSessionRecord.receivingChainIndex - 100
        if (threshold > 0) {
            val deletedCount = skippedMessageKeyDao.deleteOlderThan(
                localUserId, remoteUserId, threshold
            )
            if (deletedCount > 0) {
                logI("$localUserId 清理过期跳过密钥 $deletedCount 条（阈值:$threshold）")
            }
        }
    }

    private fun needDHStep(newRemoteDHKey: XDH.PublicKey, localRemoteDHKey: String): Boolean {
        val newRemoteDHKeyHex = newRemoteDHKey.toBytes().toHexString()
        return newRemoteDHKeyHex.isNotEmpty() && newRemoteDHKeyHex != localRemoteDHKey
    }
}
