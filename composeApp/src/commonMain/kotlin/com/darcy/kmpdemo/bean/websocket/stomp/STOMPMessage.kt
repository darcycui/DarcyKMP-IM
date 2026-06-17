package com.darcy.kmpdemo.bean.websocket.stomp

import com.darcy.kmpdemo.bean.http.response.PrivateMessageResponse
import com.darcy.kmpdemo.crypto.message.MessageHelper
import com.darcy.kmpdemo.platform.TimePlatform
import com.darcy.kmpdemo.x3dh.MessageKey
import kotlinx.serialization.Serializable

@Serializable
data class STOMPMessage(
    val senderName: String = "",
    val receiverName: String = "",
    val content: String = "",

    val msgId: String = "",
    val senderId: Long = 0,
    val receiverId: Long = 0,
    val msgType: String = "TEXT",
    val sendTime: String = TimePlatform.getCurrentTimeStamp(),
    val isRead: Boolean = false,
    val isRecalled: Boolean = false,

    val dhPublicKey: String = "",
    val nKey: Long = 0L,
    val pnKey: Long = 0L
)

suspend fun STOMPMessage.toPrivateMessageResponse(messageKeyLocal: MessageKey): PrivateMessageResponse {
    return PrivateMessageResponse(
        senderId = this.senderId,
        senderName = this.senderName,
        receiverId = this.receiverId,
        receiverName = this.receiverName,
//        content = this.content,
        content = MessageHelper.decryptContent(this.content, this.msgId, messageKeyLocal), // todo: End-End Encryption 端到端解密
        msgId = this.msgId,
        msgType = this.msgType,
        sendTime = this.sendTime,
        isRead = this.isRead,
        isRecalled = this.isRecalled
    )
}