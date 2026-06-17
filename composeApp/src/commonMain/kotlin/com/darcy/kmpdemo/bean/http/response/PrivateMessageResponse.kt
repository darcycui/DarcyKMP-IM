package com.darcy.kmpdemo.bean.http.response

import com.darcy.kmpdemo.bean.websocket.stomp.STOMPMessage
import com.darcy.kmpdemo.crypto.message.MessageHelper
import com.darcy.kmpdemo.platform.TimePlatform
import com.darcy.kmpdemo.storage.database.tables.PrivateMessageEntity
import com.darcy.kmpdemo.storage.memory.IMGlobalStorage
import com.darcy.kmpdemo.x3dh.MessageKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PrivateMessageResponsePage(
    val content: List<PrivateMessageResponse> = listOf(),
    val empty: Boolean = false,
    val first: Boolean = false,
    val last: Boolean = false,
    val number: Int = 0,
    val numberOfElements: Int = 0,
    val pageable: Pageable = Pageable(),
    val size: Int = 0,
    val sort: Sort = Sort(),
    val totalElements: Int = 0,
    val totalPages: Int = 0
) {
    @Serializable
    data class Pageable(
        val offset: Int = 0,
        val pageNumber: Int = 0,
        val pageSize: Int = 0,
        val paged: Boolean = false,
        val sort: Sort = Sort(),
        val unpaged: Boolean = false
    ) {
        @Serializable
        data class Sort(
            val empty: Boolean = false,
            val sorted: Boolean = false,
            val unsorted: Boolean = false
        )
    }

    @Serializable
    data class Sort(
        val empty: Boolean = false,
        val sorted: Boolean = false,
        val unsorted: Boolean = false
    )
}

@Serializable
data class PrivateMessageResponse(
    val msgId: String = "",
    val senderId: Long = 0,
    val senderName: String = "",
    val receiverId: Long = 0,
    val receiverName: String = "",
    val content: String = "",
    val msgType: String = "TEXT",
    val sendTime: String = TimePlatform.getCurrentTimeStamp(),
    val isRead: Boolean = false,
    val isRecalled: Boolean = false,
    val dhPublicKey: String = "",
    @SerialName("nKey")
    val nKey: Long = 0L,
    val pnKey: Long = 0L
)

fun PrivateMessageResponse.isSelfSent(): Boolean {
    return this.senderId > 0 && this.senderId == IMGlobalStorage.getCurrentUserId()
}

suspend fun PrivateMessageResponse.toSTOMPMessage(messageKeyLocal: MessageKey): STOMPMessage {
    return STOMPMessage(
        senderId = this.senderId,
        senderName = this.senderName,
        receiverId = this.receiverId,
        receiverName = this.receiverName,
//        content = this.content,
        content = MessageHelper.encryptContent(
            this.content,
            this.msgId,
            messageKeyLocal
        ), // todo: End-End Encryption 端到端加密
        sendTime = this.sendTime,
        isRead = this.isRead,
        isRecalled = this.isRecalled,
        msgId = this.msgId,
        msgType = this.msgType,
        dhPublicKey = this.dhPublicKey,
        nKey = this.nKey,
        pnKey = this.pnKey
    )
}

fun PrivateMessageResponse.toEntity(): PrivateMessageEntity {
    return PrivateMessageEntity(
        msgId = this.msgId,
        userId = this.senderId,
        targetId = this.receiverId,
        content = this.content,
        messageType = 1,
        createdTime = this.sendTime,
        updatedTime = this.sendTime,
        dhPublicKey = this.dhPublicKey,
        nKey = this.nKey,
        pnKey = this.pnKey
    )
}

fun List<PrivateMessageResponse>.toEntity(): List<PrivateMessageEntity> {
    return this.map { it.toEntity() }
}