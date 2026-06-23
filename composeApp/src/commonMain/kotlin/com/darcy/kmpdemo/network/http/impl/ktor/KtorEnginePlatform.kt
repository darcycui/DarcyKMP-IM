package com.darcy.kmpdemo.network.http.impl.ktor

import io.ktor.client.engine.HttpClientEngine

expect fun createKtorEngine(): HttpClientEngine
