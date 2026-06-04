package com.darcy.kmpdemo.ui.screen.phone.friends.repository

import com.darcy.kmpdemo.bean.http.error.ErrorResponse
import com.darcy.kmpdemo.bean.http.request.CommonRequestDTO
import com.darcy.kmpdemo.bean.http.request.FriendshipDeleteRequestDTO
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
        HttpManager.doPostJsonRequest(
            serializer<CommonRequestDTO>(),
            serializer<FriendshipResponse>(),
            QUERY_FRIENDSHIP_LIST_URL,
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

    fun deleteFriend(
        userId: Long,
        friendId: Long,
        onSuccess: (String) -> Unit,
        onError: (ErrorResponse) -> Unit
    ): Unit {
        HttpManager.doPostJsonRequest(
            serializer<FriendshipDeleteRequestDTO>(),
            serializer<String>(),
            DELETE_FRIENDSHIP_URL,
            FriendshipDeleteRequestDTO(userId, friendId),
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