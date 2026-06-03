package com.darcy.kmpdemo.storage.memory

import com.darcy.kmpdemo.log.logD
import com.darcy.kmpdemo.log.logE
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.concurrent.Volatile

object TransportGlobalStorage {
    private const val TAG = "TransportGlobalStorage"

    @Volatile
    private var serverDhKey: String = ""
    private val mutex = Mutex()


    suspend fun setServerDhKey(key: String) {
        mutex.withLock {
            if (key.isEmpty() || key.isBlank()) {
                logE("setServerDhKey is null or blank")
                return
            }
            logD("setServerDhKey: $key")
            serverDhKey = key
        }
    }

    fun getServerDhKey(): String {
        return serverDhKey.also {
            logD("getServerDhKey: $it")
        }
    }

}