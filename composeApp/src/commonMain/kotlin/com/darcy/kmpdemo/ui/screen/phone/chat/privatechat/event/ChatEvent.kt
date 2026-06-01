package com.darcy.kmpdemo.ui.screen.phone.chat.privatechat.event

import com.darcy.kmpdemo.ui.base.IEvent

sealed class ChatEvent : IEvent {
    data class ScrollToBottom(val bottomItemIndex: Int) : ChatEvent()

}