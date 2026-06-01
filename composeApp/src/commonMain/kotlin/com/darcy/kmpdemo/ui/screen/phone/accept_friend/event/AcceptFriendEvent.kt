package com.darcy.kmpdemo.ui.screen.phone.accept_friend.event

import com.darcy.kmpdemo.ui.base.IEvent

sealed class AcceptFriendEvent : IEvent {
    object AcceptFriendSuccess : AcceptFriendEvent()
    object PageBack : AcceptFriendEvent()
}