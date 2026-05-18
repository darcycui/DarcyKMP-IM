package com.darcy.kmpdemo.ui.screen.phone.x3dh.usecase

import com.darcy.kmpdemo.bean.http.request.X3DHAliceHelloRequest
import com.darcy.kmpdemo.bean.http.response.X3DHKeysPullResponse
import com.darcy.kmpdemo.storage.database.getDarcyIMDatabase
import com.darcy.kmpdemo.ui.base.IUseCase
import com.darcy.kmpdemo.utils.EncryptUtil
import com.darcy.kmpdemo.utils.JsonHelper
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
        val userId =
            params["userId"]?.toLong() ?: return Result.failure(Exception("userId is null"))
        val aliceKeysStr =
            params["aliceKeys"] ?: return Result.failure(Exception("aliceKeys is null"))
        val oneTimePreKeyId = params["oneTimePreKeyId"]?.toLong()
            ?: return Result.failure(Exception("oneTimePreKeyId is null"))
        val aliceKeys = JsonHelper.fromJson<X3DHAliceHelloRequest>(aliceKeysStr)
        val bobIdentityKey = identityKeyDao.getByUserId(userId)
            ?: return Result.failure(Exception("bobIdentityKey is null"))
        val bobIdentityPrivate = bobIdentityKey.privateKey.hexStrToBytes().toPrivateKey()
        val bobSignedPreKey = signedPreKeyDao.getByUserId(userId)
            ?: return Result.failure(Exception("bobSignedPreKey is null"))
        val bobSignedPreKeyPrivate = bobSignedPreKey.privateKey.hexStrToBytes().toPrivateKey()
        val bobOneTimePreKey = oneTimePreKeyDao.getById(oneTimePreKeyId) ?: return Result.failure(
            Exception("bobOneTimePreKey is null")
        )
        val bobOneTimePreKeyPrivate = bobOneTimePreKey.privateKey.hexStrToBytes().toPrivateKey()

        val aliceIdentityPublic = aliceKeys.identityKey.hexStrToBytes().toPublicKey()
        val aliceEphemeralPublic = aliceKeys.ephemeralKey.hexStrToBytes().toPublicKey()

        val dh1 = ECCExchangeHelper.getSharedSecret(bobIdentityPrivate, aliceIdentityPublic)
        val dh2 = ECCExchangeHelper.getSharedSecret(bobIdentityPrivate, aliceEphemeralPublic)
        val dh3 = ECCExchangeHelper.getSharedSecret(bobSignedPreKeyPrivate, aliceEphemeralPublic)
        val dh4 = ECCExchangeHelper.getSharedSecret(bobOneTimePreKeyPrivate, aliceEphemeralPublic)
        val sharedSecret = EncryptUtil.appendArrays(dh1, dh2, dh3, dh4)
        val x3DHKey =
            HKDF1.deriveSecrets(sharedSecret, ByteArray(32), "Info".encodeToByteArray(), 64)
        return Result.success(x3DHKey)
    }
}