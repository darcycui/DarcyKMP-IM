package com.darcy.kmpdemo.network.http.impl.ktor

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.js.Js

actual fun createKtorEngine(): HttpClientEngine = Js.create()
