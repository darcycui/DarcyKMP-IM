package com.darcy.kmpdemo.ui.screen.phone.apply_friend.event

import com.darcy.kmpdemo.ui.base.IEvent

sealed class ApplyFriendEvent : IEvent {
    data object ApplyFriendSuccessEvent : ApplyFriendEvent()

    data object PageBack : ApplyFriendEvent()
}