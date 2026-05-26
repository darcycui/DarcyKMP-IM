package com.darcy.kmpdemo.bean.http.response

import com.darcy.kmpdemo.bean.websocket.stomp.STOMPMessage
import com.darcy.kmpdemo.platform.TimePlatform
import com.darcy.kmpdemo.storage.database.tables.PrivateMessageEntity
import com.darcy.kmpdemo.storage.memory.IMGlobalStorage
import com.darcy.kmpdemo.utils.UUIDHelper
import kotlinx.serialization.Serializable
import kotlin.time.Clock

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
    val isRecalled: Boolean = false
)

fun PrivateMessageResponse.isSelfSent(): Boolean {
    return this.senderId > 0 && this.senderId == IMGlobalStorage.getCurrentUserId()
}

fun PrivateMessageResponse.toSTOMPMessage(): STOMPMessage {
    return STOMPMessage(
        senderId = this.senderId,
        senderName = this.senderName,
        receiverId = this.receiverId,
        receiverName = this.receiverName,
        content = this.content,
        sendTime = this.sendTime,
        isRead = this.isRead,
        isRecalled = this.isRecalled,
        msgId = this.msgId,
        msgType = this.msgType
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
        updatedTime = this.sendTime
    )
}

fun List<PrivateMessageResponse>.toEntity(): List<PrivateMessageEntity> {
    return this.map { it.toEntity() }
}