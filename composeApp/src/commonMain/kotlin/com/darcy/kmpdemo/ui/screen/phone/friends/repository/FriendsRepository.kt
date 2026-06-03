package com.darcy.kmpdemo.ui.screen.phone.friends.repository

import com.darcy.kmpdemo.bean.http.error.ErrorResponse
import com.darcy.kmpdemo.bean.http.response.FriendshipResponse
import com.darcy.kmpdemo.log.logD
import com.darcy.kmpdemo.log.logE
import com.darcy.kmpdemo.network.http.HttpManager
import com.darcy.kmpdemo.network.http.urls.Darcy.DELETE_FRIENDSHIP_URL
import com.darcy.kmpdemo.network.http.urls.Darcy.QUERY_FRIENDSHIP_LIST_URL
import com.darcy.kmpdemo.repository.IRepository
import kotlinx.serialization.serializer

class FriendsRepository : IRepository {

    fun fetchFriends(
        userId: Long,
        onSuccessList: (List<FriendshipResponse>) -> Unit,
        onError: (ErrorResponse) -> Unit
    ): Unit {
        HttpManager.doPostRequest(
            serializer<FriendshipResponse>(),
            QUERY_FRIENDSHIP_LIST_URL,
            mapOf(
                "userId" to userId.toString(),
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

    fun deleteFriend(
        userId: Long,
        friendId: Long,
        onSuccess: (String) -> Unit,
        onError: (ErrorResponse) -> Unit
    ): Unit {
        HttpManager.doPostRequest(
            serializer<String>(),
            DELETE_FRIENDSHIP_URL,
            mapOf(
                "userId" to userId.toString(),
                "friendId" to friendId.toString(),
            ),
            needRetry = true,
            needCache = true,
            success = {
                logD("success: it=${it.result::class}")
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
}