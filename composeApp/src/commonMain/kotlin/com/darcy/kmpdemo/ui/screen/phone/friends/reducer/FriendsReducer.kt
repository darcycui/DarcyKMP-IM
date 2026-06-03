package com.darcy.kmpdemo.ui.screen.phone.friends.reducer

import com.darcy.kmpdemo.bean.http.response.FriendshipResponse
import com.darcy.kmpdemo.ui.base.IIntent
import com.darcy.kmpdemo.ui.base.combined.ScreenStateFetchPagingTipsCombinedReducer
import com.darcy.kmpdemo.ui.base.impl.screenstatus.ScreenState
import com.darcy.kmpdemo.ui.base.impl.tips.TipsIntent
import com.darcy.kmpdemo.ui.screen.phone.friends.state.FriendsState

class FriendsReducer :
    ScreenStateFetchPagingTipsCombinedReducer<FriendsState, List<FriendshipResponse>>() {
    override fun onReduce(
        intent: IIntent,
        state: FriendsState
    ): FriendsState {
        return state
    }

    override fun onScreenState(
        state: FriendsState,
        newScreenState: ScreenState
    ): FriendsState {
        return state.copy(screenState = newScreenState)
    }

    override fun onFetch(
        state: FriendsState,
        result: List<FriendshipResponse>
    ): FriendsState {
        return state.copy(
            items = result
        )
    }

    override fun onPaging(
        state: FriendsState,
        pageNumber: Int,
        response: List<FriendshipResponse>
    ): FriendsState {
        return state.copy(
            items = state.items + response
        )
    }

    override fun onShowTips(
        state: FriendsState,
        intent: TipsIntent.ShowTips
    ): FriendsState {
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

    override fun onDismissTips(state: FriendsState): FriendsState {
        return state.copy(
            tipsState = state.tipsState.copy(
                showTips = false
            )
        )
    }
}