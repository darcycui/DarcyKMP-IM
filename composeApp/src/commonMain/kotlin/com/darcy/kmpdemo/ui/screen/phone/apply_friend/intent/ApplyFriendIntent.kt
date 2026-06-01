package com.darcy.kmpdemo.ui.screen.phone.apply_friend.intent

import com.darcy.kmpdemo.bean.http.response.ApplyFriendResponse
import com.darcy.kmpdemo.bean.http.response.UserResponse
import com.darcy.kmpdemo.ui.base.IIntent

sealed class ApplyFriendIntent : IIntent {
    data class ActionSearchUser(
        val phone: String
    ) : ApplyFriendIntent()

    data class RefreshBySearchUser(
        val response: UserResponse
    ): ApplyFriendIntent()

    data class ActionApplyFriend(
        val toUserId: Long
    ) : ApplyFriendIntent()

    data class RefreshByApplyFriend(
        val response: ApplyFriendResponse
    ): ApplyFriendIntent()

    data object ActionPageBack : ApplyFriendIntent()
}