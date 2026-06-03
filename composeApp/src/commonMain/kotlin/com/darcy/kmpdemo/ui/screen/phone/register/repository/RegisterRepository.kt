package com.darcy.kmpdemo.ui.screen.phone.register.repository

import com.darcy.kmpdemo.bean.http.error.ErrorResponse
import com.darcy.kmpdemo.bean.http.response.LoginResponse
import com.darcy.kmpdemo.bean.ui.RegisterBean
import com.darcy.kmpdemo.log.logD
import com.darcy.kmpdemo.log.logE
import com.darcy.kmpdemo.network.http.HttpManager
import com.darcy.kmpdemo.network.http.urls.Darcy.REGISTER_URL
import com.darcy.kmpdemo.repository.IRepository
import kotlinx.serialization.serializer

class RegisterRepository : IRepository {
    suspend fun register(
        bean: RegisterBean,
        onSuccess: (LoginResponse) -> Unit,
        onError: (ErrorResponse) -> Unit
    ): Unit {
        HttpManager.doPostRequest(
            serializer<LoginResponse>(),
            REGISTER_URL,
            mapOf(
                "username" to bean.username,
                "password" to bean.passwordHash,
                "nickname" to bean.nickname,
                "avatar" to bean.avatar,
                "phone" to bean.phone,
                "email" to bean.email,
                "gender" to bean.gender,
                "age" to bean.age.toString(),
                "signature" to bean.signature,
                "status" to bean.status.toString(),
                "onlineStatus" to bean.onlineStatus.toString(),
                "settings" to bean.settings.toString(),
                "roles" to bean.roles,
                "token" to bean.token
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