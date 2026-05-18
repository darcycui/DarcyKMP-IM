package com.darcy.kmpdemo.ui.screen.phone.friends.repository

import com.darcy.kmpdemo.bean.http.error.ErrorResponse
import com.darcy.kmpdemo.bean.http.response.FriendshipResponse
import com.darcy.kmpdemo.network.http.HttpManager
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
                println("success: itClazz=${it.result::class}")
                onSuccessList(it.result)
            },
            errors = {
                println("error: it=$it")
                onError(it)
            })
    }
}