package com.darcy.kmpdemo.platform

import io.ktor.client.engine.HttpClientEngine

expect fun createKtorEngine(): HttpClientEngine
