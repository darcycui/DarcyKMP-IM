package com.darcy.kmpdemo.ui.screen.phone.login.repository

import com.darcy.kmpdemo.bean.http.error.ErrorResponse
import com.darcy.kmpdemo.bean.http.response.LoginResponse
import com.darcy.kmpdemo.log.logD
import com.darcy.kmpdemo.log.logE
import com.darcy.kmpdemo.network.http.HttpManager
import com.darcy.kmpdemo.network.http.urls.Darcy.LOGIN_URL
import com.darcy.kmpdemo.repository.IRepository
import kotlinx.serialization.serializer

class LoginRepository : IRepository {
    suspend fun login(
        username: String,
        password: String,
        onSuccess: (LoginResponse) -> Unit,
        onError: (ErrorResponse) -> Unit
    ): Unit {
        HttpManager.doPostRequest(
            serializer<LoginResponse>(),
            LOGIN_URL,
            mapOf(
                "phone" to username,
                "password" to password
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

}