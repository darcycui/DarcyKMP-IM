package com.darcy.kmpdemo.ui.screen.phone.conversations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.darcy.kmpdemo.bean.http.error.ErrorResponse
import com.darcy.kmpdemo.bean.http.error.toTipsIntent
import com.darcy.kmpdemo.bean.http.response.ConversationResponse
import com.darcy.kmpdemo.bean.ui.ChatListItemBean
import com.darcy.kmpdemo.exception.BaseException
import com.darcy.kmpdemo.storage.database.tables.ConversationEntity
import com.darcy.kmpdemo.storage.memory.IMGlobalStorage
import com.darcy.kmpdemo.ui.base.BaseViewModel
import com.darcy.kmpdemo.ui.base.IIntent
import com.darcy.kmpdemo.ui.base.IReducer
import com.darcy.kmpdemo.ui.base.impl.fetch.FetchIntent
import com.darcy.kmpdemo.ui.base.impl.paging.PagingIntent
import com.darcy.kmpdemo.ui.base.impl.screenstatus.ScreenState
import com.darcy.kmpdemo.ui.base.impl.screenstatus.ScreenStateIntent
import com.darcy.kmpdemo.ui.screen.phone.conversations.event.ConversationEvent
import com.darcy.kmpdemo.ui.screen.phone.conversations.intent.ConversationIntent
import com.darcy.kmpdemo.ui.screen.phone.conversations.reducer.ConversationReducer
import com.darcy.kmpdemo.ui.screen.phone.conversations.repository.ConversationRepository
import com.darcy.kmpdemo.ui.screen.phone.conversations.state.ConversationState
import kotlin.reflect.KClass

class ConversationViewModel(
    private val conversationRepository: ConversationRepository = ConversationRepository(),
) : BaseViewModel<ConversationState>() {
    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
                return ConversationViewModel() as T
            }
        }
    }

    override fun initState(): ConversationState {
        return ConversationState()
    }

    override fun initReducers(): List<IReducer<ConversationState>> {
        return listOf(ConversationReducer())
    }

    override fun dispatch(intent: IIntent) {
        when (intent) {
            is FetchIntent.ActionFetchData -> { // 获取数据
                actionFetchChatList()
            }

            is ConversationIntent.GoChatPage -> { // 进入聊天页面
                actionGoChatPage(intent.response)
            }

            is PagingIntent.ActionLoadNewPage -> {
                // 分页
            }

            else -> {
                super.dispatch(intent)
            }
        }
    }

    private fun actionFetchChatList() {
        io {
            dispatch(ScreenStateIntent.ScreenStateChange(ScreenState.Loading))
            val userId = IMGlobalStorage.getCurrentUserId()
            conversationRepository.fetchConversations(
                userId,
                onSuccessList = {
                    dispatch(ScreenStateIntent.ScreenStateChange(ScreenState.Success))
                    dispatch(FetchIntent.RefreshByFetchData(it))
                },
                onError = {
                    main { dispatch(it.toTipsIntent()) }
                })
        }
    }

    private fun actionGoChatPage(response: ConversationResponse) {
        io {
            sendEvent(
                ConversationEvent.GoChatPage(
                    conversationId = response.id,
                    userId = response.target.id,
                    userName = response.target.username,
                    userAvatar = response.target.avatar,
                )
            )
        }
    }
}