package com.darcy.kmpdemo.x3dh.usecase

import com.darcy.kmpdemo.storage.database.getDarcyIMDatabase
import com.darcy.kmpdemo.storage.database.tables.IdentityKeyEntity
import com.darcy.kmpdemo.ui.base.IUseCase
import com.darcy.kmpdemo.utils.UUIDHelper
import com.darcy.kmpdemo.utils.toBytes
import com.darcy.kmpdemo.x3dh.exchange.ECCExchangeHelper

class GenerateIdentityKeyUseCase : IUseCase<Unit, IdentityKeyEntity> {
    private val identityKeyDao = getDarcyIMDatabase().identityKeyDao()
    override suspend fun invoke(params: Map<String, String>, bean: Unit): Result<IdentityKeyEntity> {
        val userId =
            params["userId"]?.toLongOrNull() ?: return Result.failure(Exception("userId is null"))
        val identityKey = ECCExchangeHelper.generateKeyPair()
        val identityKeyEntity = IdentityKeyEntity(
            keyId = UUIDHelper.generateIdentityKeyId(),
            userId = params["userId"]?.toLongOrNull() ?: 0,
            privateKey = identityKey.privateKey.toBytes().toHexString(),
            publicKey = identityKey.publicKey.toBytes().toHexString()
        )
        identityKeyDao.insert(identityKeyEntity)
        val identityKeyEntityFromDb = identityKeyDao.getByUserId(userId) ?: return Result.failure(
            Exception("identityKey is null")
        )
        return Result.success(identityKeyEntityFromDb)
    }
}