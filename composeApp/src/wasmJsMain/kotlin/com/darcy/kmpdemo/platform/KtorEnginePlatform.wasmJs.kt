package com.darcy.kmpdemo.platform

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.js.Js

actual fun createKtorEngine(): HttpClientEngine = Js.create()
