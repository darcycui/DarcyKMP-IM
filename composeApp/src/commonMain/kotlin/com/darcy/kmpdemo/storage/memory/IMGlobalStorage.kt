package com.darcy.kmpdemo.storage.memory

import com.darcy.kmpdemo.bean.http.response.LoginResponse
import com.darcy.kmpdemo.network.http.token.TokenManager
import com.darcy.kmpdemo.storage.database.tables.UserEntity
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * 存储全局数据
 * 如 登录用户
 */
object IMGlobalStorage {
    private var currentUser: LoginResponse = LoginResponse.empty()

    // kmp 同步代码 使用协程锁
    private val mutex: Mutex = Mutex()

    fun getCurrentUser(): LoginResponse {
        return currentUser
    }

    fun getCurrentUserId(): Long {
        return currentUser.id
    }

    suspend fun setCurrentUser(user: LoginResponse) {
        mutex.withLock {
            currentUser = user
        }
    }

}