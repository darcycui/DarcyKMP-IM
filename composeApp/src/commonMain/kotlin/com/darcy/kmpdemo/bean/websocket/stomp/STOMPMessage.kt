package com.darcy.kmpdemo.bean.websocket.stomp

import com.darcy.kmpdemo.bean.http.response.PrivateMessageResponse
import com.darcy.kmpdemo.platform.TimePlatform
import kotlinx.serialization.Serializable
import kotlin.time.Clock

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

fun STOMPMessage.toPrivateMessageResponse(): PrivateMessageResponse {
    return PrivateMessageResponse(
        senderId = this.senderId,
        senderName = this.senderName,
        receiverId = this.receiverId,
        receiverName = this.receiverName,
        content = this.content,
        msgId = this.msgId,
        msgType = this.msgType,
        sendTime = this.sendTime,
        isRead = this.isRead,
        isRecalled = this.isRecalled
    )
}