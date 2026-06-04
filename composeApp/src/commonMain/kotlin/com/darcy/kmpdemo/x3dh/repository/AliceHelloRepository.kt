package com.darcy.kmpdemo.x3dh.repository

import com.darcy.kmpdemo.bean.http.error.ErrorResponse
import com.darcy.kmpdemo.bean.http.request.X3DHPullKeysRequestDTO
import com.darcy.kmpdemo.bean.http.request.X3DHPushHelloRequestDTO
import com.darcy.kmpdemo.bean.http.response.X3DHAliceHelloPullResponse
import com.darcy.kmpdemo.bean.http.response.X3DHAliceHelloPushResponse
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
        HttpManager.doPostJsonRequest(
            serializer<X3DHPushHelloRequestDTO>(),
            serializer<X3DHAliceHelloPushResponse>(),
            PUSH_ALICE_HELLO_MESSAGE_URL,
            X3DHPushHelloRequestDTO(
                aliceUserId = aliceUserId,
                bobUserId = bobUserId,
                aliceIdentityKey = aliceIdentityKey,
                aliceEphemeralKey = aliceEphemeralKey,
                bobOneTimePreKeyId = bobOneTimePreKeyId
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
            },
            false
        )
    }

    fun pullAliceHello(
        aliceUserId: Long,
        bobUserId: Long,
        onSuccess: (X3DHAliceHelloPullResponse) -> Unit,
        onError: (ErrorResponse) -> Unit
    ) {
        HttpManager.doPostJsonRequest(
            serializer<X3DHPullKeysRequestDTO>(),
            serializer<X3DHAliceHelloPullResponse>(),
            PULL_ALICE_HELLO_MESSAGE_URL,
            X3DHPullKeysRequestDTO(
                aliceUserId = aliceUserId,
                bobUserId = bobUserId
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
            },
            false
        )
    }
}