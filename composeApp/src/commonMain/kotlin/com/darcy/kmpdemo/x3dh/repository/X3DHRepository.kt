package com.darcy.kmpdemo.x3dh.repository

import com.darcy.kmpdemo.bean.http.error.ErrorResponse
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
        oneTimePreKeys: String,
        onSuccess: (X3DHKeysPushResponse) -> Unit,
        onError: (ErrorResponse) -> Unit
    ): Unit {
        HttpManager.doPostFormRequest(
            serializer<X3DHKeysPushResponse>(),
            PUSH_X3DH_KEYS_URL,
            mapOf(
                "userId" to userId.toString(),
                "identityKey" to identityKey,
                "signedPreKey" to signedPreKey,
                "oneTimePreKeys" to oneTimePreKeys,
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
        HttpManager.doPostFormRequest(
            serializer<X3DHKeysPullResponse>(),
            PULL_X3DH_KEYS_URL,
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
                logE("error: it=$it")
                onError(it)
            },
            false
        )
    }
}