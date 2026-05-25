package com.darcy.kmpdemo.x3dh.usecase

import com.darcy.kmpdemo.storage.database.getDarcyIMDatabase
import com.darcy.kmpdemo.storage.database.tables.SignedPreKeyEntity
import com.darcy.kmpdemo.ui.base.IUseCase
import com.darcy.kmpdemo.utils.UUIDHelper
import com.darcy.kmpdemo.utils.toBytes
import com.darcy.kmpdemo.x3dh.exchange.ECCExchangeHelper

class GenerateSignedPreKeyUseCase : IUseCase<Unit, SignedPreKeyEntity> {
    private val identityKeyDao = getDarcyIMDatabase().identityKeyDao()
    private val signedPreKeyDao = getDarcyIMDatabase().signedPreKeyDao()
    override suspend fun invoke(params: Map<String, String>, bean: Unit): Result<SignedPreKeyEntity> {
        val userId =
            params["userId"]?.toLongOrNull() ?: return Result.failure(Exception("userId is null"))
        val identityKeyId = params["identityKeyId"]
            ?: return Result.failure(Exception("identityKeyId is null"))
        val identityKey = identityKeyDao.getByKeyId(identityKeyId)
            ?: return Result.failure(Exception("identityKey is null"))
        val signedPreKey = ECCExchangeHelper.generateKeyPair()
        val signedPreKeyEntity = SignedPreKeyEntity(
            keyId = UUIDHelper.generateSignedPreKeyId(),
            identityKeyId = identityKeyId,
            userId = userId,
            privateKey = signedPreKey.privateKey.toBytes().toHexString(),
            publicKey = signedPreKey.publicKey.toBytes().toHexString(),
            signature = "" // todo 使用身份密钥签名
        )
        signedPreKeyDao.insert(signedPreKeyEntity)
        val signedPreKeyFromDb = signedPreKeyDao.getByUserId(userId) ?: return Result.failure(
            Exception("signedPreKey is null")
        )
        return Result.success(signedPreKeyFromDb)
    }
}