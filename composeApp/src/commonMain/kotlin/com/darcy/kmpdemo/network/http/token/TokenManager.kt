package com.darcy.kmpdemo.network.http.token

import com.darcy.kmpdemo.log.logW
import com.darcy.kmpdemo.network.http.token.TokenManager.setToken
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object TokenManager {
    private var token: String = ""

    private val setMutex: Mutex = Mutex()

    fun getToken(): String {
        logW("getToken: $token")
        return token
    }

    suspend fun setToken(token: String) {
        setMutex.withLock {
            logW("setToken: $token")
            this.token = token
        }
    }

    suspend fun clearToken() {
        setToken("")
    }

    suspend fun refreshToken() {
        val token = setMutex.withLock {
            token.ifEmpty {
                // 刷新token
                doRefreshToken()
            }
        }
        setToken(token)
    }

    suspend fun doRefreshToken(): String {
        return "refreshToken"
    }
}