package com.darcy.kmpdemo.ui.screen.phone.accept_friend.intent

import com.darcy.kmpdemo.bean.http.response.ApplyFriendResponse
import com.darcy.kmpdemo.ui.base.IIntent

sealed class AcceptFriendIntent : IIntent {
    data class ActionAcceptFriend(val applyId: Long, val targetUserId: Long) : AcceptFriendIntent()

    data class RefreshByAcceptFriend(val response: ApplyFriendResponse) : AcceptFriendIntent()

    data object ActionPageBack : AcceptFriendIntent()
}