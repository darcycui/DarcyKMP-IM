package com.darcy.kmpdemo.ui.screen.phone.register.repository

import com.darcy.kmpdemo.bean.http.error.ErrorResponse
import com.darcy.kmpdemo.bean.http.request.RegisterRequestDTO
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
        HttpManager.doPostJsonRequest(
            serializer<RegisterRequestDTO>(),
            serializer<LoginResponse>(),
            REGISTER_URL,
            params = RegisterRequestDTO(
                username = bean.username,
                password = bean.passwordHash,
                nickname = bean.nickname,
                avatar = bean.avatar,
                phone = bean.phone,
                email = bean.email,
                gender = bean.gender,
                signature = bean.signature,
                settings = bean.settings.toString(),
                roles = bean.roles,
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