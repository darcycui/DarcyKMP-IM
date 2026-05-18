package com.darcy.kmpdemo.storage.memory

object X3DHGlobalStorage {
    private val x3DHEphemeralKeys = mutableMapOf<Long, String>()

    fun getX3DHEphemeralKey(userId: Long): String {
        return x3DHEphemeralKeys[userId] ?: ""
    }

    fun setX3DHEphemeralKey(userId: Long, key: String) {
        x3DHEphemeralKeys[userId] = key
    }
}