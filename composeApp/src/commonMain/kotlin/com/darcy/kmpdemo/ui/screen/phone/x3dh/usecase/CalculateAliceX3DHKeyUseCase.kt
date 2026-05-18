package com.darcy.kmpdemo.ui.screen.phone.x3dh.usecase

import com.darcy.kmpdemo.bean.http.response.X3DHKeysPullResponse
import com.darcy.kmpdemo.storage.database.daos.IdentityKeyDao
import com.darcy.kmpdemo.storage.database.daos.SessionRecordDao
import com.darcy.kmpdemo.storage.database.getDarcyIMDatabase
import com.darcy.kmpdemo.storage.memory.X3DHGlobalStorage
import com.darcy.kmpdemo.ui.base.IUseCase
import com.darcy.kmpdemo.utils.EncryptUtil
import com.darcy.kmpdemo.utils.JsonHelper
import com.darcy.kmpdemo.utils.hexStrToBytes
import com.darcy.kmpdemo.utils.toBytes
import com.darcy.kmpdemo.utils.toPrivateKey
import com.darcy.kmpdemo.utils.toPublicKey
import com.darcy.kmpdemo.x3dh.chain.HKDF1
import com.darcy.kmpdemo.x3dh.exchange.ECCExchangeHelper
import dev.whyoleg.cryptography.algorithms.XDH

class CalculateAliceX3DHKeyUseCase : IUseCase<Pair<ByteArray, XDH.KeyPair>> {
    private val identityKeyDao: IdentityKeyDao = getDarcyIMDatabase().identityKeyDao()

    /**
     * 计算X3DH密钥
     * @param params
     * @return Pair<ByteArray, XDH.KeyPair>  x3DHKey 和 aliceEphemeralKey
     */
    override suspend fun invoke(params: Map<String, String>): Result<Pair<ByteArray, XDH.KeyPair>> {
        val userId =
            params["userId"]?.toLong() ?: return Result.failure(Exception("userId is null"))
        val bobKeysStr = params["bobKeys"] ?: return Result.failure(Exception("bobKeys is null"))
        val bobKeys = JsonHelper.fromJson<X3DHKeysPullResponse>(bobKeysStr)
        val aliceIdentityPrivateKey = identityKeyDao.getById(userId) ?: return Result.failure(
            Exception("aliceIdentityPrivateKey is null")
        )
        val aliceIdentityPrivate = aliceIdentityPrivateKey.privateKey.hexStrToBytes().toPrivateKey()
        val aliceEphemeralKey = ECCExchangeHelper.generateKeyPair()
        val aliceEphemeralPrivate = aliceEphemeralKey.privateKey

        val bobIdentityPublic = bobKeys.identityKey.hexStrToBytes().toPublicKey()
        val bobSignedPreKeyPublic = bobKeys.signedPreKey.hexStrToBytes().toPublicKey()
        val bobOneTimePreKeyPublic = bobKeys.oneTimePreKey.hexStrToBytes().toPublicKey()

        val dh1 = ECCExchangeHelper.getSharedSecret(aliceIdentityPrivate, bobIdentityPublic)
        val dh2 = ECCExchangeHelper.getSharedSecret(aliceEphemeralPrivate, bobIdentityPublic)
        val dh3 = ECCExchangeHelper.getSharedSecret(aliceEphemeralPrivate, bobSignedPreKeyPublic)
        val dh4 = ECCExchangeHelper.getSharedSecret(aliceEphemeralPrivate, bobOneTimePreKeyPublic)
        val sharedSecret = EncryptUtil.appendArrays(dh1, dh2, dh3, dh4)
        val x3DHKey =
            HKDF1.deriveSecrets(sharedSecret, ByteArray(32), "Info".encodeToByteArray(), 64)
        // 保存临时密钥
        X3DHGlobalStorage.setX3DHEphemeralKey(
            bobKeys.userId,
            aliceEphemeralKey.publicKey.toBytes().toHexString()
        )
        return Result.success(Pair(x3DHKey, aliceEphemeralKey))
    }
}