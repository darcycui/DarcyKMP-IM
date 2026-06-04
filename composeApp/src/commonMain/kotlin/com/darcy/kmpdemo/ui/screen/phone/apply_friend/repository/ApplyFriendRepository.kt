package com.darcy.kmpdemo.ui.screen.phone.apply_friend.repository

import com.darcy.kmpdemo.bean.http.error.ErrorResponse
import com.darcy.kmpdemo.bean.http.response.ApplyFriendResponse
import com.darcy.kmpdemo.bean.http.response.UserResponse
import com.darcy.kmpdemo.bean.ui.AddFriendBean
import com.darcy.kmpdemo.log.logD
import com.darcy.kmpdemo.log.logE
import com.darcy.kmpdemo.network.http.HttpManager
import com.darcy.kmpdemo.network.http.urls.Darcy.APPLY_FRIEND_URL
import com.darcy.kmpdemo.network.http.urls.Darcy.QUERY_FRIEND_FROM_URL
import com.darcy.kmpdemo.network.http.urls.Darcy.SEARCH_FRIEND_URL
import com.darcy.kmpdemo.repository.IRepository
import kotlinx.serialization.serializer

class ApplyFriendRepository : IRepository {
    fun searchUser(
        phone: String,
        onSuccess: (UserResponse) -> Unit,
        onError: (ErrorResponse) -> Unit
    ): Unit {
        HttpManager.doPostFormRequest(
            serializer<UserResponse>(),
            SEARCH_FRIEND_URL,
            mapOf(
                "phone" to phone,
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

    fun applyFriend(
        bean: AddFriendBean,
        onSuccess: (ApplyFriendResponse) -> Unit,
        onError: (ErrorResponse) -> Unit
    ): Unit {
        HttpManager.doPostFormRequest(
            serializer<ApplyFriendResponse>(),
            APPLY_FRIEND_URL,
            mapOf(
                "fromUserId" to bean.fromUserId.toString(),
                "toUserId" to bean.toUserId.toString(),
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

    fun fetchFriendApplys(
        fromUserId: Long,
        onSuccessList: (List<ApplyFriendResponse>) -> Unit,
        onError: (ErrorResponse) -> Unit
    ): Unit {
        HttpManager.doPostFormRequest(
            serializer<ApplyFriendResponse>(),
            QUERY_FRIEND_FROM_URL,
            mapOf(
                "fromUserId" to fromUserId.toString(),
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

}