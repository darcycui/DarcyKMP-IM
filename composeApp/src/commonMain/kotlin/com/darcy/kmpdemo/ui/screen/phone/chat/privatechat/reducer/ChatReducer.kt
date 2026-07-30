package com.darcy.kmpdemo.ui.screen.phone.chat.privatechat.reducer

import com.darcy.kmpdemo.bean.http.response.PrivateMessageResponse
import com.darcy.kmpdemo.bean.http.response.PrivateMessageResponsePage
import com.darcy.kmpdemo.ui.base.IIntent
import com.darcy.kmpdemo.ui.base.combined.ScreenStateFetchPagingTipsCombinedReducer
import com.darcy.kmpdemo.ui.base.impl.screenstatus.ScreenState
import com.darcy.kmpdemo.ui.base.impl.tips.TipsIntent
import com.darcy.kmpdemo.ui.screen.phone.chat.privatechat.intent.ChatIntent
import com.darcy.kmpdemo.ui.screen.phone.chat.privatechat.state.ChatState
import com.darcy.kmpdemo.ui.screen.phone.chat.privatechat.state.WebSocketConnectionState

class ChatReducer :
    ScreenStateFetchPagingTipsCombinedReducer<ChatState, List<PrivateMessageResponse>>() {
    override fun onReduce(
        intent: IIntent,
        state: ChatState
    ): ChatState {
        return when (intent) {
            is ChatIntent.RefreshByReceiveMessage -> {
                state.copy(
                    items = state.items + listOf(intent.message)
                )
            }
            is ChatIntent.RefreshBySendMessage -> {
                state.copy(
                    items = state.items + listOf(intent.message)
                )
            }
            is ChatIntent.WebSocketState -> {
                state.copy(
                    webSocketConnectionState = intent.state
                )
            }
            is ChatIntent.RefreshByReceiveMessageReadStatus -> {
                state.copy(
                    items = state.items.map {
                        if (it.msgId in intent.response.msgIds) {
                            it.copy(isRead = true)
                        } else {
                            it
                        }
                    }
                )
            }

            is ChatIntent.RefreshByPreviousPageLoadingState -> {
                state.copy(
                    isLoadingPreviousPage = intent.isLoading
                )
            }

            is ChatIntent.RefreshByNoMorePreviousPage -> {
                state.copy(
                    hasMorePreviousPage = false,
                    isLoadingPreviousPage = false,
                )
            }

            else -> super.reduce(intent, state)
        }
    }

    override fun onScreenState(
        state: ChatState,
        newScreenState: ScreenState
    ): ChatState {
        return state.copy(screenState = newScreenState)
    }

    override fun onFetch(
        state: ChatState,
        result: List<PrivateMessageResponse>
    ): ChatState {
        return state.copy(
            items = result,
            enabledLoadPreviousPage = true
        )
    }

    override fun onPaging(
        state: ChatState,
        pageNumber: Int,
        response: List<PrivateMessageResponse>
    ): ChatState {
        return state.copy(
            items = response + state.items,
            isLoadingPreviousPage = false,
        )
    }

    override fun onShowTips(
        state: ChatState,
        intent: TipsIntent.ShowTips
    ): ChatState {
        return state.copy(
            tipsState = state.tipsState.copy(
                showTips = true,
                title = intent.title,
                tips = intent.tips,
                code = intent.code,
                middleButtonText = intent.middleButtonText,
                positiveButtonText = intent.positiveButtonText,
                negativeButtonText = intent.negativeButtonText,
            )
        )
    }

    override fun onDismissTips(state: ChatState): ChatState {
        return state.copy(
            tipsState = state.tipsState.copy(
                showTips = false
            )
        )
    }
}