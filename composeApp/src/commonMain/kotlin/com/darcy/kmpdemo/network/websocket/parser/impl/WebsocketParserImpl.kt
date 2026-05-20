package com.darcy.kmpdemo.network.websocket.parser.impl

import com.darcy.kmpdemo.bean.http.error.ErrorResponse
import com.darcy.kmpdemo.bean.websocket.stomp.STOMPMessage
import com.darcy.kmpdemo.network.http.parser.impl.kotlinxJson
import com.darcy.kmpdemo.network.websocket.parser.IWebsocketParser

class WebsocketParserImpl : IWebsocketParser {
    override fun toBean(
        json: String,
        success: (STOMPMessage) -> Unit,
        error: (ErrorResponse) -> Unit
    ) {
        runCatching {
            kotlinxJson.decodeFromString<STOMPMessage>(json).let {
                success.invoke(it)
            }
        }.onFailure {
            error.invoke(
                ErrorResponse.create(
                    status = -1,
                    error = "Websocket Parser Error",
                    message = "Websocket解析失败: ${it.message}"
                )
            )
        }
    }
}