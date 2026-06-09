package com.darcy.kmpdemo.network.websocket.impl.krossbow

import kotlinx.coroutines.flow.Flow
import org.hildan.krossbow.stomp.StompReceipt
import org.hildan.krossbow.stomp.frame.FrameBody
import org.hildan.krossbow.stomp.frame.StompFrame
import org.hildan.krossbow.stomp.headers.StompSendHeaders
import org.hildan.krossbow.stomp.headers.StompSubscribeHeaders

interface ISessionWrap {
    suspend fun sendText(
        message: String,
        destination: String,
        headers: Map<String, String>,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit,
    )

    suspend fun subscribe(
        destination: String,
        onMessage: (String, Map<String, String>) -> Unit,
        onFailure: (String) -> Unit
    )

    suspend fun disconnect()
}