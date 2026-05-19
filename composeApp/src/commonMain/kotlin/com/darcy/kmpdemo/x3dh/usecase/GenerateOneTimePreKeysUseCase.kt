package com.darcy.kmpdemo.x3dh.usecase

import com.darcy.kmpdemo.storage.database.getDarcyIMDatabase
import com.darcy.kmpdemo.storage.database.tables.OneTimePreKeyEntity
import com.darcy.kmpdemo.ui.base.IUseCase
import com.darcy.kmpdemo.utils.UUIDHelper
import com.darcy.kmpdemo.utils.toBytes
import com.darcy.kmpdemo.x3dh.exchange.ECCExchangeHelper

class GenerateOneTimePreKeysUseCase : IUseCase<List<OneTimePreKeyEntity>> {
    private val oneTimePreKeyDao = getDarcyIMDatabase().oneTimePreKeyDao()
    override suspend fun invoke(params: Map<String, String>): Result<List<OneTimePreKeyEntity>> {
        val userId =
            params["userId"]?.toLongOrNull() ?: return Result.failure(Exception("userId is null"))
        val oneTimePreKeyList = (0 until 100).map {
            val oneTimePreKey = ECCExchangeHelper.generateKeyPair()
            OneTimePreKeyEntity(
                keyId = UUIDHelper.generateOneTimePreKeyId(),
                userId = userId,
                privateKey = oneTimePreKey.privateKey.toBytes().toHexString(),
                publicKey = oneTimePreKey.publicKey.toBytes().toHexString()
            )
        }
        oneTimePreKeyDao.insertAll(oneTimePreKeyList)
        return Result.success(oneTimePreKeyDao.getAllByUserId(userId))
    }
}