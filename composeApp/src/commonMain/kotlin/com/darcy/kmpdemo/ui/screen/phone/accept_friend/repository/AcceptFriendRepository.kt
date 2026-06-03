package com.darcy.kmpdemo.ui.screen.phone.accept_friend.repository

import com.darcy.kmpdemo.bean.http.error.ErrorResponse
import com.darcy.kmpdemo.bean.http.response.ApplyFriendResponse
import com.darcy.kmpdemo.log.logD
import com.darcy.kmpdemo.log.logE
import com.darcy.kmpdemo.network.http.HttpManager
import com.darcy.kmpdemo.network.http.urls.Darcy.ACCEPT_FRIEND_URL
import com.darcy.kmpdemo.network.http.urls.Darcy.QUERY_FRIEND_TO_URL
import com.darcy.kmpdemo.repository.IRepository
import kotlinx.serialization.serializer

class AcceptFriendRepository : IRepository {
    fun fetchFriendApplys(
        toUserId: Long,
        onSuccessList: (List<ApplyFriendResponse>) -> Unit,
        onError: (ErrorResponse) -> Unit
    ): Unit {
        HttpManager.doPostRequest(
            serializer<ApplyFriendResponse>(),
            QUERY_FRIEND_TO_URL,
            mapOf(
                "toUserId" to toUserId.toString(),
            ),
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
    fun acceptFriend(
        friendRequestId: Long,
        onSuccess: (ApplyFriendResponse) -> Unit,
        onError: (ErrorResponse) -> Unit
    ): Unit {
        HttpManager.doPostRequest(
            serializer<ApplyFriendResponse>(),
            ACCEPT_FRIEND_URL,
            mapOf(
                "friendRequestId" to friendRequestId.toString(),
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