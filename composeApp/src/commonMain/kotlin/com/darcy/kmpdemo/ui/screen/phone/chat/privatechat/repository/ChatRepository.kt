package com.darcy.kmpdemo.ui.screen.phone.chat.privatechat.repository

import com.darcy.kmpdemo.bean.http.error.ErrorResponse
import com.darcy.kmpdemo.bean.http.request.MessageReadStatusRequest
import com.darcy.kmpdemo.bean.http.request.PrivateMessageQueryRequestDTO
import com.darcy.kmpdemo.bean.http.request.ReceiverMessageReadStatusMarkRequestDTO
import com.darcy.kmpdemo.bean.http.request.ReceiverOfflineMessageSyncRequestDTO
import com.darcy.kmpdemo.bean.http.request.SenderOfflineMessageReadSyncRequestDTO
import com.darcy.kmpdemo.bean.http.response.MessageReadStatusResponse
import com.darcy.kmpdemo.bean.http.response.PrivateMessageResponsePage
import com.darcy.kmpdemo.log.logD
import com.darcy.kmpdemo.log.logE
import com.darcy.kmpdemo.network.http.HttpManager
import com.darcy.kmpdemo.network.http.urls.Darcy
import com.darcy.kmpdemo.network.http.urls.Darcy.QUERY_PRIVATE_MESSAGE_URL
import com.darcy.kmpdemo.network.http.urls.Darcy.RECEIVER_PUSH_MESSAGE_READ_STATUS_URL
import com.darcy.kmpdemo.repository.IRepository
import com.darcy.kmpdemo.utils.JsonHelper
import kotlinx.serialization.serializer

class ChatRepository : IRepository {

    fun fetchMessages(
        userId: Long,
        conversationId: Long,
        page: Int,
        size: Int,
        onSuccess: (PrivateMessageResponsePage) -> Unit,
        onError: (ErrorResponse) -> Unit
    ): Unit {
        HttpManager.doPostJsonRequest(
            serializer<PrivateMessageQueryRequestDTO>(),
            serializer<PrivateMessageResponsePage>(),
            QUERY_PRIVATE_MESSAGE_URL,
            PrivateMessageQueryRequestDTO(
                conversationId = conversationId,
                conversationType = 1,
                page = page,
                size = size
            ),
            needRetry = true,
            needCache = true,
            success = {
                logD("success: itClazz=${it.result::class}")
                onSuccess(it.result)
            },
            successList = {},
            errors = {
                logE("error: it=$it")
                onError(it)
            },
            false
        )
    }

    fun receiverPullOfflineMessageHttp(
        userId: Long,
        targetId: Long,
        conversationId: Long,
        conversationType: Int,
        page: Int,
        size: Int,
        onSuccess: (PrivateMessageResponsePage) -> Unit,
        onError: (ErrorResponse) -> Unit
    ) {
        HttpManager.doPostJsonRequest(
            serializer<ReceiverOfflineMessageSyncRequestDTO>(),
            serializer<PrivateMessageResponsePage>(),
            url = Darcy.RECEIVER_PULL_OFFLINE_MESSAGE_URL,
            params = ReceiverOfflineMessageSyncRequestDTO(
                userId = userId,
                targetId = targetId,
                conversationId = conversationId,
                conversationType = conversationType,
                deviceId = "",
                clientType = "",
                page = page,
                size = size,
                limit = 50,
                lastMsgId = null,
                lastSyncTime = null
            ),
            needRetry = true,
            needCache = true,
            success = {
                logD("success: itClazz=${it.result::class}")
                onSuccess(it.result)
            },
            successList = {},
            errors = {
                onError(it)
            },
            encrypt = false
        )
    }

    fun receiverPushMessageReadStatusHttp(
        userId: Long,
        fromUserName: String,
        targetId: Long,
        targetName: String,
        msgIds: List<String>,
        onSuccess: (MessageReadStatusResponse) -> Unit,
        onError: (ErrorResponse) -> Unit
    ) {
        HttpManager.doPostJsonRequest(
            serializer<ReceiverMessageReadStatusMarkRequestDTO>(),
            serializer<MessageReadStatusResponse>(),
            RECEIVER_PUSH_MESSAGE_READ_STATUS_URL,
            ReceiverMessageReadStatusMarkRequestDTO(
                userId = userId,
                fromUserName = fromUserName,
                targetId = targetId,
                targetName = targetName,
                msgIds = msgIds,
                conversationId = -1,
                conversationType = 1,
                clientType = "",
                deviceId = ""
            ),
            needRetry = true,
            needCache = true,
            success = {
                logD("success: itClazz=${it.result::class}")
                onSuccess(it.result)
            },
            successList = {},
            errors = {
                logE("error: it=$it")
                onError(it)
            },
            false
        )
    }

    fun senderSyncMessageReadStatusHttp(
        userId: Long,
        targetId: Long,
        conversationId: Long,
        conversationType: Int,
        since: String = "",
        until: String = "",
        onSuccess: (MessageReadStatusResponse) -> Unit,
        onError: (ErrorResponse) -> Unit
    ) {
        HttpManager.doPostJsonRequest(
            serializer<SenderOfflineMessageReadSyncRequestDTO>(),
            serializer<MessageReadStatusResponse>(),
            url = Darcy.SENDER_SYNC_MESSAGE_READ_STATUS_URL,
            params = SenderOfflineMessageReadSyncRequestDTO(
                userId = userId,
                targetId = targetId,
                conversationId = conversationId,
                conversationType = conversationType,
                deviceId = "",
                clientType = "",
                since = since,
                until = until
            ),
            needRetry = true,
            needCache = false,
            success = {
                logD("syncMessageReadStatus success: ${it.result}")
                onSuccess(it.result)
            },
            successList = {},
            errors = onError,
            encrypt = false
        )
    }
}