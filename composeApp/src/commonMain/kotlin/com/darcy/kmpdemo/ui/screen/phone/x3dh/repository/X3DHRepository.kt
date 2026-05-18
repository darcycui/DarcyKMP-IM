package com.darcy.kmpdemo.ui.screen.phone.x3dh.repository

import com.darcy.kmpdemo.bean.http.error.ErrorResponse
import com.darcy.kmpdemo.bean.http.request.X3DHBobKeysRequest
import com.darcy.kmpdemo.bean.http.response.X3DHKeysPullResponse
import com.darcy.kmpdemo.bean.http.response.X3DHKeysPushResponse
import com.darcy.kmpdemo.network.http.HttpManager
import com.darcy.kmpdemo.network.http.urls.Darcy.REGISTER_URL
import com.darcy.kmpdemo.repository.IRepository
import kotlinx.serialization.serializer

class X3DHRepository : IRepository {
    fun pushX3DHKeys(
        bean: X3DHBobKeysRequest,
        onSuccess: (X3DHKeysPushResponse) -> Unit,
        onError: (ErrorResponse) -> Unit
    ): Unit {
        HttpManager.doPostRequest(
            serializer<X3DHKeysPushResponse>(),
            REGISTER_URL,
            mapOf(
                "identityKey" to bean.identityKey,
                "signedPreKey" to bean.signedPreKey,
                "oneTimePreKey" to bean.oneTimePreKey,
            ),
            needRetry = true,
            needCache = true,
            success = {
                println("success: itClazz=${it.result::class}")
                onSuccess(it.result)
            },
            successList = { },
            errors = {
                println("error: it=$it")
                onError(it)
            })
    }

    fun pullX3DHBobKeys(
        bobUserId: Long,
        onSuccess: (X3DHKeysPullResponse) -> Unit,
        onError: (ErrorResponse) -> Unit
    ): Unit {
        HttpManager.doPostRequest(
            serializer<X3DHKeysPullResponse>(),
            REGISTER_URL,
            mapOf(
                "bobUserId" to bobUserId.toString(),
            ),
            needRetry = true,
            needCache = true,
            success = {
                println("success: itClazz=${it.result::class}")
                onSuccess(it.result)
            },
            successList = { },
            errors = {
                println("error: it=$it")
                onError(it)
            })
    }
}