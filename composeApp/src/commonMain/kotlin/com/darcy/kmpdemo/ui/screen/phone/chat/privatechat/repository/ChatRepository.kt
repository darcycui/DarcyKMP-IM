package com.darcy.kmpdemo.ui.screen.phone.chat.privatechat.repository

import com.darcy.kmpdemo.bean.http.error.ErrorResponse
import com.darcy.kmpdemo.bean.http.request.MessageReadStatusRequest
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
        HttpManager.doPostRequest(
            serializer<PrivateMessageResponsePage>(),
            QUERY_PRIVATE_MESSAGE_URL,
            mapOf(
                "conversationId" to conversationId.toString(),
                "page" to page.toString(),
                "size" to size.toString(),
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
            })
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
        HttpManager.doGetRequest(
            serializer = PrivateMessageResponsePage.serializer(),
            url = Darcy.RECEIVER_PULL_OFFLINE_MESSAGE_URL,
            params = mapOf(
                "userId" to userId.toString(),
                "targetId" to targetId.toString(),
                "conversationId" to conversationId.toString(),
                "conversationType" to conversationType.toString(),
                "page" to page.toString(),
                "size" to size.toString()
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
            }
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
        val messageReadStatusRequest = MessageReadStatusRequest(
            userId = userId,
            fromUserName = fromUserName,
            targetId = targetId,
            targetName = targetName,
            msgIds = msgIds,
            conversationType = 1,
            clientType = "",
            deviceId = ""
        )
        HttpManager.doPostRequest(
            serializer<MessageReadStatusResponse>(),
            RECEIVER_PUSH_MESSAGE_READ_STATUS_URL,
            mapOf(
                "messageReadStatusInputDTO" to JsonHelper.toJson(messageReadStatusRequest),
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
            })
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
        HttpManager.doPostRequest(
            serializer = MessageReadStatusResponse.serializer(),
            url = Darcy.SENDER_SYNC_MESSAGE_READ_STATUS_URL,
            params = mapOf(
                "userId" to userId.toString(),
                "targetId" to targetId.toString(),
                "conversationId" to conversationId.toString(),
                "conversationType" to conversationType.toString(),
                "since" to since,
                "until" to until
            ),
            needRetry = true,
            needCache = false,
            success = {
                logD("syncMessageReadStatus success: ${it.result}")
                onSuccess(it.result)
            },
            successList = {},
            errors = onError
        )
    }
}