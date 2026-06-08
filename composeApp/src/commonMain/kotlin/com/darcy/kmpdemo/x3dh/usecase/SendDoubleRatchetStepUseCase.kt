package com.darcy.kmpdemo.x3dh.usecase

import com.darcy.kmpdemo.log.logD
import com.darcy.kmpdemo.log.logV
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
        EncryptUtil.log("$localUserId 根密钥:", lastRootKey)
        val remoteDHKey = sessionRecord.remoteDHKey.hexStrToBytes().toPublicKey()
        var localEphemeralPublicKeyBytes: ByteArray
        val localEphemeralPrivateKey =
            sessionRecord.localEphemeralPrivateKey.hexStrToBytes().toPrivateKey()
        val localEphemeralPublicKey =
            sessionRecord.localEphemeralPublicKey.hexStrToBytes().toPublicKey()
        localEphemeralPublicKeyBytes = localEphemeralPublicKey.toBytes()
        EncryptUtil.log("$localUserId 本地公钥:", localEphemeralPublicKey)
        EncryptUtil.log("$localUserId DH 私钥:", localEphemeralPrivateKey)
        EncryptUtil.log("$localUserId DH 公钥:", remoteDHKey)
        val newN: Long = sessionRecord.N + 1
        val newPN: Long = sessionRecord.PN
        logW("$localUserId DH 棘轮无需步进:")
        logV("$localUserId 发送链索引: N=$newN, PN=$newPN")
        val newSendingChainKey = ChainKey(
            HKDF1(),
            sessionRecord.sendingChainKey.hexStrToBytes(),
            sessionRecord.N
        ).getNextChainKey()
        EncryptUtil.log("$localUserId 发送链密钥:", newSendingChainKey.getKey())
        sessionRecordDao.update(
            sessionRecord.copy(
                sendingChainKey = newSendingChainKey.getKey().toHexString(),
                N = newN,
                PN = newPN,
                updatedTime = TimePlatform.getCurrentTimeStamp()
            )
        )
        val messageKeyTriple = newSendingChainKey.getMessageKeyTriple()
        val messageKeyBytes = messageKeyTriple.first
        EncryptUtil.logI("$localUserId 发送 $remoteUserId 的消息密钥:", messageKeyBytes)
        val messageKey = MessageKey(
            fromUserId = localUserId,
            toUserId = remoteUserId,
            dhPublicKey = localEphemeralPublicKeyBytes.toHexString(),
            messageKey = messageKeyBytes.toHexString(),
            nKey = newN,
            pnKey = newPN,
            url = "/private"
        )
        return Result.success(messageKey)
    }
}
