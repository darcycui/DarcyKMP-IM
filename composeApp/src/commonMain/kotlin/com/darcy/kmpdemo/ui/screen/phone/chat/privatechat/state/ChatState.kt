package com.darcy.kmpdemo.ui.screen.phone.chat.privatechat.state

import com.darcy.kmpdemo.bean.http.response.PrivateMessageResponse
import com.darcy.kmpdemo.ui.base.IState
import com.darcy.kmpdemo.ui.base.impl.paging.PagingState
import com.darcy.kmpdemo.ui.base.impl.screenstatus.ScreenState
import com.darcy.kmpdemo.ui.base.impl.tips.TipsState

data class ChatState(
    val items: List<PrivateMessageResponse> = emptyList(),
    val screenState: ScreenState = ScreenState.Initial,
    val pagingState: PagingState = PagingState(),
    val tipsState: TipsState = TipsState(),
    val webSocketConnectionState: WebSocketConnectionState = WebSocketConnectionState.Disconnected,
) : IState
