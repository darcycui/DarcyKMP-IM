package com.darcy.kmpdemo.network.websocket.parser

import com.darcy.kmpdemo.bean.http.error.ErrorResponse
import com.darcy.kmpdemo.bean.websocket.stomp.STOMPMessage
import kotlinx.serialization.KSerializer

interface IWebsocketParser {
    fun toBean(
        json: String,
        success: ((STOMPMessage) -> Unit),
        error: ((ErrorResponse) -> Unit)
    )
}