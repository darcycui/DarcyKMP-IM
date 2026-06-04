package com.darcy.kmpdemo.ui.screen.phone.conversations.repository

import com.darcy.kmpdemo.bean.http.error.ErrorResponse
import com.darcy.kmpdemo.bean.http.request.CommonRequestDTO
import com.darcy.kmpdemo.bean.http.request.ConversationCreateRequestDTO
import com.darcy.kmpdemo.bean.http.response.ConversationResponse
import com.darcy.kmpdemo.log.logD
import com.darcy.kmpdemo.log.logE
import com.darcy.kmpdemo.network.http.HttpManager
import com.darcy.kmpdemo.network.http.urls.Darcy.CREATE_CONVERSATION_URL
import com.darcy.kmpdemo.network.http.urls.Darcy.QUERY_CONVERSATION_LIST_URL
import com.darcy.kmpdemo.repository.IRepository
import kotlinx.serialization.serializer

class ConversationRepository : IRepository {

    fun fetchConversations(
        userId: Long,
        onSuccessList: (List<ConversationResponse>) -> Unit,
        onError: (ErrorResponse) -> Unit
    ): Unit {
        HttpManager.doPostJsonRequest(
            serializer<CommonRequestDTO>(),
            serializer<ConversationResponse>(),
            QUERY_CONVERSATION_LIST_URL,
            CommonRequestDTO(userId),
            needRetry = true,
            needCache = true,
            success = {},
            successList = {
                logD("success: itClazz=${it.result::class}")
                onSuccessList(it.result)
            },
            errors = {
                logE("error: it=$it")
                onError(it)
            },
            false
        )
    }


    fun createConversation(
        userId: String,
        targetId: String,
        conversationType: String,
        onSuccess: (ConversationResponse) -> Unit,
        onError: (ErrorResponse) -> Unit
    ): Unit {
        HttpManager.doPostJsonRequest(
            serializer<ConversationCreateRequestDTO>(),
            serializer<ConversationResponse>(),
            CREATE_CONVERSATION_URL,
            ConversationCreateRequestDTO(
                userId.toLong(), targetId.toLong(), conversationType.toInt()
            ),
            needRetry = true,
            needCache = true,
            success = {
                logD("success: itClazz=${it.result::class}")
                onSuccess(it.result)
            },
            successList = { },
            errors = {
                logE("error: it=$it")
                onError(it)
            },
            false
        )
    }

}