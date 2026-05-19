package com.darcy.kmpdemo.utils

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

object UUIDHelper {
    private const val IDENTITY_KEY_PREFIX = "identity_key_"
    private const val SIGNED_PRE_KEY_PREFIX = "signed_pre_key_"
    private const val ONE_TIME_PRE_KEY_PREFIX = "one_time_pre_key_"

    @OptIn(ExperimentalUuidApi::class)
    fun generateIdentityKeyId(): String {
        return "$IDENTITY_KEY_PREFIX${Uuid.random()}"
    }

    @OptIn(ExperimentalUuidApi::class)
    fun generateSignedPreKeyId(): String {
        return "$SIGNED_PRE_KEY_PREFIX${Uuid.random()}"
    }

    @OptIn(ExperimentalUuidApi::class)
    fun generateOneTimePreKeyId(): String {
        return "$ONE_TIME_PRE_KEY_PREFIX${Uuid.random()}"
    }
}