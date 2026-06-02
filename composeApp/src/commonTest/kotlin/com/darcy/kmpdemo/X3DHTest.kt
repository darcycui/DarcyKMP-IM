package com.darcy.kmpdemo

import com.darcy.kmpdemo.utils.EncryptUtil
import com.darcy.kmpdemo.utils.bytesToHexStr
import com.darcy.kmpdemo.utils.hexStrToBytes
import com.darcy.kmpdemo.utils.toPrivateKey
import com.darcy.kmpdemo.utils.toPublicKey
import com.darcy.kmpdemo.x3dh.chain.HKDF1
import com.darcy.kmpdemo.x3dh.exchange.ECCExchangeHelper
import kotlin.test.Test
import kotlin.test.assertContentEquals

data class X3DHBobKeys(
    val identityKey: String = "45719fdb359ba56c3115dcde90533459559cb79abe918ff8c25710fa19551d6d",
    val signedPreKey: String = "2fc06c206dee050b7f0f7869dddeced0f9cccc0be79b015bb2f0bb01bf24b666",
    val oneTimePreKey: String = "831d0ea4848fc45db766e68a0014230cd967fddf603e37fdea5422ca1737062e"
)

class X3DHTest {

    @Test
    fun test() {
        val bobKeys = X3DHBobKeys()
        val K1 = aliceCalculateKey(bobKeys)
        println("K1==${K1.bytesToHexStr()}")
        val K2 = bobCalculateKey()
        println("K2=${K2.bytesToHexStr()}")
        assertContentEquals(K1, K2, "密钥交换错误")
    }

    val aliceIdentityPrivateKey =
        "8c4aae7a93367905f9f8a68491173059bfd53aa6ccb9906ba59d247f650b1231"
    val aliceEphemeralPrivateKey =
        "f1b760d87917b117017d2328792fb28b95e652bd71d7c4db44c18b1e3dc79337"

    fun aliceCalculateKey(bobKeys: X3DHBobKeys): ByteArray {
        val aliceIdentityPrivate = aliceIdentityPrivateKey.hexStrToBytes().toPrivateKey()
        val aliceEphemeralPrivate = aliceEphemeralPrivateKey.hexStrToBytes().toPrivateKey()

        val bobIdentityPublic = bobKeys.identityKey.hexStrToBytes().toPublicKey()
        val bobSignedPreKeyPublic = bobKeys.signedPreKey.hexStrToBytes().toPublicKey()
        val bobOneTimePreKeyPublic = bobKeys.oneTimePreKey.hexStrToBytes().toPublicKey()

        val dh1 = ECCExchangeHelper.getSharedSecret(aliceIdentityPrivate, bobIdentityPublic)
        val dh2 = ECCExchangeHelper.getSharedSecret(aliceEphemeralPrivate, bobIdentityPublic)
        val dh3 = ECCExchangeHelper.getSharedSecret(aliceEphemeralPrivate, bobSignedPreKeyPublic)
        val dh4 = ECCExchangeHelper.getSharedSecret(aliceEphemeralPrivate, bobOneTimePreKeyPublic)
        val sharedSecret = EncryptUtil.appendArrays(dh1, dh2, dh3, dh4)
        return HKDF1().deriveSecrets(sharedSecret, ByteArray(32), "Info".encodeToByteArray(), 64)
    }

    val aliceIdentityPublicKey =
        "3d4fa7151d41dd6242145c651e3b26f2c8c2b285e28f6843bffd82d6232d832a"
    val aliceEphemeralPublicKey =
        "a0b9b7d397986c7c2c492d6b03039eeccba10c32ad86538546a46fb2b0b07226"

    val bobIdentityPrivate =
        "286e814b3eb5bcee87f41fa35e68b73d24be78b46adb789c65dd0f917cb8239c"
    val bobSignedPreKeyPrivate =
        "51bbbd6b05dc99556ed59688154f8895d44d70897fa567e933d121d33b7f9a96"
    val bobOneTimePreKeyPrivate =
        "55f2e93e1f74ce86dfbf8772264a216c6cb3e69979fd50771fb5c7c7841f53a0"

    fun bobCalculateKey(): ByteArray {
        val bobIdentityPrivate = bobIdentityPrivate.hexStrToBytes().toPrivateKey()
        val bobSignedPreKeyPrivate = bobSignedPreKeyPrivate.hexStrToBytes().toPrivateKey()
        val bobOneTimePreKeyPrivate = bobOneTimePreKeyPrivate.hexStrToBytes().toPrivateKey()

        val aliceIdentityPublic = aliceIdentityPublicKey.hexStrToBytes().toPublicKey()
        val aliceEphemeralPublic = aliceEphemeralPublicKey.hexStrToBytes().toPublicKey()

        val dh1 = ECCExchangeHelper.getSharedSecret(bobIdentityPrivate, aliceIdentityPublic)
        val dh2 = ECCExchangeHelper.getSharedSecret(bobIdentityPrivate, aliceEphemeralPublic)
        val dh3 = ECCExchangeHelper.getSharedSecret(bobSignedPreKeyPrivate, aliceEphemeralPublic)
        val dh4 = ECCExchangeHelper.getSharedSecret(bobOneTimePreKeyPrivate, aliceEphemeralPublic)
        val sharedSecret = EncryptUtil.appendArrays(dh1, dh2, dh3, dh4)
        return HKDF1().deriveSecrets(sharedSecret, ByteArray(32), "Info".encodeToByteArray(), 64)

    }
}