package com.darcy.kmpdemo.platform

import io.ktor.client.engine.HttpClientEngine

/**
 *  创建Ktor引擎 同时设置自定义SSL证书
 */
expect fun createKtorEngine(): HttpClientEngine
