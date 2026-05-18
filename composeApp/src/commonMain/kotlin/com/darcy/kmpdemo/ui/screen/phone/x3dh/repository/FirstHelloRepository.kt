package com.darcy.kmpdemo.ui.screen.phone.x3dh.repository

import com.darcy.kmpdemo.bean.http.error.ErrorResponse
import com.darcy.kmpdemo.bean.http.request.X3DHAliceHelloRequest
import com.darcy.kmpdemo.bean.http.response.X3DHKeysPushResponse
import com.darcy.kmpdemo.network.http.HttpManager
import com.darcy.kmpdemo.network.http.urls.Darcy.SEND_ALICE_HELLO_MESSAGE_URL
import com.darcy.kmpdemo.repository.IRepository
import kotlinx.serialization.serializer

class FirstHelloRepository : IRepository {
    fun sendAliceHello(
        bean: X3DHAliceHelloRequest,
        onSuccess: (X3DHKeysPushResponse) -> Unit,
        onError: (ErrorResponse) -> Unit
    ): Unit {
        HttpManager.doPostRequest(
            serializer<X3DHKeysPushResponse>(),
            SEND_ALICE_HELLO_MESSAGE_URL,
            mapOf(
                "identityKey" to bean.identityKey,
                "ephemeralKey" to bean.ephemeralKey,
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