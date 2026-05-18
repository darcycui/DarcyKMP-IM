package com.darcy.kmpdemo.x3dh.usecase

import com.darcy.kmpdemo.storage.database.getDarcyIMDatabase
import com.darcy.kmpdemo.ui.base.IUseCase
import com.darcy.kmpdemo.utils.EncryptUtil
import com.darcy.kmpdemo.utils.hexStrToBytes
import com.darcy.kmpdemo.utils.toPrivateKey
import com.darcy.kmpdemo.utils.toPublicKey
import com.darcy.kmpdemo.x3dh.chain.HKDF1
import com.darcy.kmpdemo.x3dh.exchange.ECCExchangeHelper

class CalculateBobX3DHKeyUseCase : IUseCase<ByteArray> {
    private val identityKeyDao = getDarcyIMDatabase().identityKeyDao()
    private val signedPreKeyDao = getDarcyIMDatabase().signedPreKeyDao()
    private val oneTimePreKeyDao = getDarcyIMDatabase().oneTimePreKeyDao()
    override suspend fun invoke(params: Map<String, String>): Result<ByteArray> {
        val bobUserId = params["bobUserId"]?.toLong() ?: return Result.failure(Exception("userId is null"))
        val aliceIdentityKey = params["aliceIdentityKey"] ?: return Result.failure(Exception("aliceIdentityKey is null"))
        val aliceEphemeralKey = params["aliceEphemeralKey"] ?: return Result.failure(Exception("aliceEphemeralKey is null"))
        val oneTimePreKeyId = params["oneTimePreKeyId"]?.toLong() ?: return Result.failure(Exception("oneTimePreKeyId is null"))
        val bobIdentityKey = identityKeyDao.getByUserId(bobUserId) ?: return Result.failure(Exception("bobIdentityKey is null"))
        val bobIdentityPrivate = bobIdentityKey.privateKey.hexStrToBytes().toPrivateKey()
        val bobSignedPreKey = signedPreKeyDao.getByUserId(bobUserId) ?: return Result.failure(Exception("bobSignedPreKey is null"))
        val bobSignedPreKeyPrivate = bobSignedPreKey.privateKey.hexStrToBytes().toPrivateKey()
        val bobOneTimePreKey = oneTimePreKeyDao.getById(oneTimePreKeyId) ?: return Result.failure(Exception("bobOneTimePreKey is null"))
        val bobOneTimePreKeyPrivate = bobOneTimePreKey.privateKey.hexStrToBytes().toPrivateKey()

        val aliceIdentityPublic = aliceIdentityKey.hexStrToBytes().toPublicKey()
        val aliceEphemeralPublic = aliceEphemeralKey.hexStrToBytes().toPublicKey()

        val dh1 = ECCExchangeHelper.getSharedSecret(bobIdentityPrivate, aliceIdentityPublic)
        val dh2 = ECCExchangeHelper.getSharedSecret(bobIdentityPrivate, aliceEphemeralPublic)
        val dh3 = ECCExchangeHelper.getSharedSecret(bobSignedPreKeyPrivate, aliceEphemeralPublic)
        val dh4 = ECCExchangeHelper.getSharedSecret(bobOneTimePreKeyPrivate, aliceEphemeralPublic)
        val sharedSecret = EncryptUtil.appendArrays(dh1, dh2, dh3, dh4)
        val x3DHKey =
            HKDF1().deriveSecrets(sharedSecret, ByteArray(32), "Info".encodeToByteArray(), 64)
        return Result.success(x3DHKey)
    }
}