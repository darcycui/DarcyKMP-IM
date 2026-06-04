package com.darcy.kmpdemo.x3dh.repository

import com.darcy.kmpdemo.bean.http.error.ErrorResponse
import com.darcy.kmpdemo.bean.http.request.OneTimePreKeyInputDTO
import com.darcy.kmpdemo.bean.http.request.X3DHPullKeysRequestDTO
import com.darcy.kmpdemo.bean.http.request.X3DHPushKeysRequestDTO
import com.darcy.kmpdemo.bean.http.response.X3DHKeysPullResponse
import com.darcy.kmpdemo.bean.http.response.X3DHKeysPushResponse
import com.darcy.kmpdemo.log.logD
import com.darcy.kmpdemo.log.logE
import com.darcy.kmpdemo.network.http.HttpManager
import com.darcy.kmpdemo.network.http.urls.Darcy.PULL_X3DH_KEYS_URL
import com.darcy.kmpdemo.network.http.urls.Darcy.PUSH_X3DH_KEYS_URL
import com.darcy.kmpdemo.repository.IRepository
import kotlinx.serialization.serializer

class X3DHRepository : IRepository {
    fun pushX3DHKeys(
        userId: Long,
        identityKey: String,
        signedPreKey: String,
        oneTimePreKeys: List<OneTimePreKeyInputDTO>,
        onSuccess: (X3DHKeysPushResponse) -> Unit,
        onError: (ErrorResponse) -> Unit
    ): Unit {
        HttpManager.doPostJsonRequest(
            serializer<X3DHPushKeysRequestDTO>(),
            serializer<X3DHKeysPushResponse>(),
            PUSH_X3DH_KEYS_URL,
            X3DHPushKeysRequestDTO(
                userId = userId,
                identityKey = identityKey,
                signedPreKey = signedPreKey,
                oneTimePreKeys = oneTimePreKeys
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

    fun pullX3DHBobKeys(
        aliceUserId: Long,
        bobUserId: Long,
        onSuccess: (X3DHKeysPullResponse) -> Unit,
        onError: (ErrorResponse) -> Unit
    ): Unit {
        HttpManager.doPostJsonRequest(
            serializer<X3DHPullKeysRequestDTO>(),
            serializer<X3DHKeysPullResponse>(),
            PULL_X3DH_KEYS_URL,
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
                logE("error: it=$it")
                onError(it)
            },
            false
        )
    }
}