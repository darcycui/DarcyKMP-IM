package com.darcy.kmpdemo.storage.memory

import com.darcy.kmpdemo.log.logD
import com.darcy.kmpdemo.log.logE
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.concurrent.Volatile

object TransportGlobalStorage {
    private const val TAG = "TransportGlobalStorage"

    @Volatile
    private var sharedSecretKey: String = ""
    private val mutex = Mutex()


    suspend fun setServerSharedSecretKey(key: String) {
        mutex.withLock {
            if (key.isEmpty() || key.isBlank()) {
                logE("$TAG setServerDhKey is null or blank")
                return
            }
            logD("$TAG setServerDhKey: $key")
            sharedSecretKey = key
        }
    }

    fun getServerSharedSecretKey(): String {
        return sharedSecretKey.also {
            logD("$TAG getSharedSecretKey: $it")
        }
    }

    suspend fun clear() {
        mutex.withLock {
            sharedSecretKey = ""
        }
    }

}