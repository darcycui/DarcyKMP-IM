package com.darcy.kmpdemo.x3dh.repository

import com.darcy.kmpdemo.bean.http.error.ErrorResponse
import com.darcy.kmpdemo.bean.http.response.X3DHAliceHelloPullResponse
import com.darcy.kmpdemo.bean.http.response.X3DHAliceHelloPushResponse
import com.darcy.kmpdemo.bean.http.response.X3DHKeysPushResponse
import com.darcy.kmpdemo.log.logD
import com.darcy.kmpdemo.log.logE
import com.darcy.kmpdemo.network.http.HttpManager
import com.darcy.kmpdemo.network.http.urls.Darcy.PULL_ALICE_HELLO_MESSAGE_URL
import com.darcy.kmpdemo.network.http.urls.Darcy.PUSH_ALICE_HELLO_MESSAGE_URL
import com.darcy.kmpdemo.repository.IRepository
import kotlinx.serialization.serializer

class AliceHelloRepository : IRepository {
    fun pushAliceHello(
        aliceUserId: Long,
        bobUserId: Long,
        aliceIdentityKey: String,
        aliceEphemeralKey: String,
        bobOneTimePreKeyId: String,
        onSuccess: (X3DHAliceHelloPushResponse) -> Unit,
        onError: (ErrorResponse) -> Unit,
    ): Unit {
        HttpManager.doPostRequest(
            serializer<X3DHAliceHelloPushResponse>(),
            PUSH_ALICE_HELLO_MESSAGE_URL,
            mapOf(
                "aliceUserId" to aliceUserId.toString(),
                "bobUserId" to bobUserId.toString(),
                "aliceIdentityKey" to aliceIdentityKey,
                "aliceEphemeralKey" to aliceEphemeralKey,
                "bobOneTimePreKeyId" to bobOneTimePreKeyId,
            ),
            needRetry = true,
            needCache = true,
            success = {
                logD("success: itClazz=${it.result::class}")
                onSuccess(it.result)
            },
            successList = { },
            errors = {
                logE("error: it=$it")
                onError(it)
            })
    }

    fun pullAliceHello(
        aliceUserId: Long,
        bobUserId: Long,
        onSuccess: (X3DHAliceHelloPullResponse) -> Unit,
        onError: (ErrorResponse) -> Unit
    ) {
        HttpManager.doPostRequest(
            serializer<X3DHAliceHelloPullResponse>(),
            PULL_ALICE_HELLO_MESSAGE_URL,
            mapOf(
                "aliceUserId" to aliceUserId.toString(),
                "bobUserId" to bobUserId.toString(),
            ),
            needRetry = true,
            needCache = true,
            success = {
                logD("success: itClazz=${it.result::class}")
                onSuccess(it.result)
            },
            successList = { },
            errors = {
                logD("error: it=$it")
                onError(it)
            })
    }
}