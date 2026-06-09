package com.darcy.kmpdemo.network.websocket.impl.krossbow

import com.darcy.kmpdemo.crypto.JsonCryptoHelper
import com.darcy.kmpdemo.log.logD
import com.darcy.kmpdemo.log.logE
import com.darcy.kmpdemo.network.websocket.frame.toJsonString
import org.hildan.krossbow.stomp.StompSession
import org.hildan.krossbow.stomp.frame.FrameBody
import org.hildan.krossbow.stomp.frame.StompFrame
import org.hildan.krossbow.stomp.headers.ExperimentalHeadersApi
import org.hildan.krossbow.stomp.headers.StompSendHeaders
import org.hildan.krossbow.stomp.subscribe

class CryptoSessionWrapImpl(
    private val session: StompSession
) : ISessionWrap {
    companion object {
        private val TAG = CryptoSessionWrapImpl::class.simpleName
    }

    override suspend fun sendText(
        message: String,
        destination: String,
        headers: Map<String, String>,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit,
    ) {
        runCatching {
            logD("$TAG 发送消息...")
            // 加密消息体
            val jsonMessage = encryptFrameBody(message, headers)
            // val receipt = it.sendText(SEND_PRIVATE, jsonMessage)
            val receipt = session.send(
                headers = StompSendHeaders(destination) { setAll(headers) },
                body = FrameBody.Text(jsonMessage)
            )
            logD("$TAG 收到确认帧: $receipt")
            onSuccess(jsonMessage)
        }.onFailure { it ->
            onFailure("发送消息失败: ${it::class.simpleName} ${it.message}")
            it.printStackTrace()
        }.onSuccess {
            logD("$TAG 发送成功")
        }
    }

    @OptIn(ExperimentalHeadersApi::class)
    override suspend fun subscribe(
        destination: String,
        onMessage: (String, Map<String, String>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        session.subscribe(destination).collect { frame: StompFrame ->
            runCatching {
                logD("$TAG 接收消息...")
                val headers = frame.headers.asMap() // 获取消息 headers
                val body = frame.body.toJsonString() // 获取消息体
                // 解密消息体
                val realBody = decryptFrameBody(body, headers)
                // 将消息体和 headers 一起传递给外部监听器
                onMessage(realBody, headers)
            }.onSuccess {
                logD("$TAG 接收消息成功")
            }.onFailure {
                logE("$TAG 接收消息失败: ${it::class.simpleName} ${it.message}")
                it.printStackTrace()
                onFailure("")
            }
        }
    }


    private suspend fun encryptFrameBody(
        body: String,
        headers: Map<String, String>,
    ): String {
        val url = headers["url"] ?: ""
        val encryptedMessage = JsonCryptoHelper.encryptWebsocketJson(body, url)
        return encryptedMessage
    }

    private suspend fun decryptFrameBody(
        body: String,
        headers: Map<String, String>
    ): String {
        val url = headers["url"] ?: ""
        val decryptedMessage = JsonCryptoHelper.decryptWebsocketJson(body, url)
        return decryptedMessage
    }

    override suspend fun disconnect() {
        session.disconnect()
    }
}

fun StompSession.cryptoWrap(): ISessionWrap {
    return CryptoSessionWrapImpl(this)
}