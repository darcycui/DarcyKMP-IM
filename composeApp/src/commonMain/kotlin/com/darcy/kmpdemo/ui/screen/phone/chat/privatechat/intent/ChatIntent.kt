package com.darcy.kmpdemo.ui.screen.phone.chat.privatechat.intent

import com.darcy.kmpdemo.bean.http.response.MessageReadStatusResponse
import com.darcy.kmpdemo.bean.http.response.PrivateMessageResponse
import com.darcy.kmpdemo.ui.base.IIntent
import com.darcy.kmpdemo.ui.screen.phone.chat.privatechat.state.WebSocketConnectionState

sealed class ChatIntent : IIntent {
    data class ActionSendMessage(val message: PrivateMessageResponse) : ChatIntent()

    data class RefreshBySendMessage(val message: PrivateMessageResponse) : ChatIntent()

    data object ActionRegisterReceiveMessage : ChatIntent()

    data class RefreshByReceiveMessage(val message: PrivateMessageResponse) : ChatIntent()

    data class RefreshByReceiveMessageReadStatus(
        val response: MessageReadStatusResponse
    ) : ChatIntent()

    data class WebSocketState(val state: WebSocketConnectionState) : ChatIntent()
}