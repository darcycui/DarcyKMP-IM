package com.darcy.kmpdemo.crypto.repository

import com.darcy.kmpdemo.bean.http.error.ErrorResponse
import com.darcy.kmpdemo.bean.http.response.DHExchangeResponse
import com.darcy.kmpdemo.log.logD
import com.darcy.kmpdemo.log.logE
import com.darcy.kmpdemo.network.http.HttpManager
import com.darcy.kmpdemo.network.http.urls.Darcy.EXCHANGE_SERVER_DH_URL
import com.darcy.kmpdemo.repository.IRepository
import kotlinx.serialization.serializer

class DHExchangeRepository: IRepository {
    fun getServerDHPublicKey(
        userId: Long,
        publicKey: String,
        onSuccess: (DHExchangeResponse) -> Unit,
        onError: (ErrorResponse) -> Unit
    ) {
        HttpManager.doPostRequest(
            serializer<DHExchangeResponse>(),
            EXCHANGE_SERVER_DH_URL,
            mapOf(
                "userId" to userId.toString(),
                "publicKey" to publicKey,
            ),
            needRetry = false,
            needCache = false,
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