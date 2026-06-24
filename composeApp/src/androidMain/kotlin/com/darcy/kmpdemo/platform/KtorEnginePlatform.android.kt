package com.darcy.kmpdemo.platform

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO
import kotlinx.coroutines.Dispatchers

actual fun createKtorEngine(): HttpClientEngine = CIO.create {
    dispatcher = Dispatchers.Default
}
