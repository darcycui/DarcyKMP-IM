package com.darcy.kmpdemo.x3dh.user

import dev.whyoleg.cryptography.algorithms.XDH


interface IUser {
    fun getName(): String

    fun getIdentityKeyPair(): XDH.KeyPair

    fun getIdentityPublicKey(): XDH.PublicKey

    fun getIdentityPrivateKey(): XDH.PrivateKey

    fun getSignedPreKeyPair(): XDH.KeyPair

    fun getSignedPreKeyPublicKey(): XDH.PublicKey

    fun getSignedPreKeyPrivateKey(): XDH.PrivateKey

    fun getOneTimePreKeyPair(id: String): XDH.KeyPair

    fun getOneTimePreKeyPublicKeyList(): List<XDH.PublicKey>

    fun getOneTimePreKeyPrivateKeyList(): List<XDH.PrivateKey>
}