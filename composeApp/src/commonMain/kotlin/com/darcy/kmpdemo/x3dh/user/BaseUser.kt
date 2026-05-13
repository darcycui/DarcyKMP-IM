package com.darcy.kmpdemo.x3dh.user

import com.darcy.kmpdemo.x3dh.exchange.ECCExchangeHelper
import dev.whyoleg.cryptography.algorithms.XDH

abstract class BaseUser() : IUser {
    protected var innerIdentityKeyPair: XDH.KeyPair = ECCExchangeHelper.generateKeyPair()
    protected var innerSignedPreKeyPair: XDH.KeyPair = ECCExchangeHelper.generateKeyPair()
    protected var innerOneTimePreKeyPairMap: MutableMap<String, XDH.KeyPair> = HashMap(100)

    init {
        for (i in 0..99) {
            val item: XDH.KeyPair = ECCExchangeHelper.generateKeyPair()
            innerOneTimePreKeyPairMap[(i + 1).toString()] = item
        }
    }

    override fun getIdentityKeyPair(): XDH.KeyPair {
        return innerIdentityKeyPair
    }

    override fun getIdentityPublicKey(): XDH.PublicKey {
        return innerIdentityKeyPair.publicKey
    }

    override fun getIdentityPrivateKey(): XDH.PrivateKey {
        return innerIdentityKeyPair.privateKey
    }

    override fun getSignedPreKeyPair(): XDH.KeyPair {
        return innerSignedPreKeyPair
    }

    override fun getSignedPreKeyPublicKey(): XDH.PublicKey {
        return innerSignedPreKeyPair.publicKey
    }

    override fun getSignedPreKeyPrivateKey(): XDH.PrivateKey {
        return innerSignedPreKeyPair.privateKey
    }

    override fun getOneTimePreKeyPair(id: String): XDH.KeyPair {
        if (!innerOneTimePreKeyPairMap.containsKey(id)) {
            throw RuntimeException("OneTimePreKeyPair 不存在:$id")
        }
        return innerOneTimePreKeyPairMap[id]!!
    }

    override fun getOneTimePreKeyPublicKeyList(): List<XDH.PublicKey> {
        return innerOneTimePreKeyPairMap.values.toList().map { it.publicKey }
    }

    override fun getOneTimePreKeyPrivateKeyList(): List<XDH.PrivateKey> {
        return innerOneTimePreKeyPairMap.values.toList().map { it.privateKey }
    }
}