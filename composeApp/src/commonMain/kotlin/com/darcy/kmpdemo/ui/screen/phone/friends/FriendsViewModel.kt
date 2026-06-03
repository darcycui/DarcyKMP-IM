package com.darcy.kmpdemo.ui.screen.phone.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.darcy.kmpdemo.bean.http.error.toTipsIntent
import com.darcy.kmpdemo.bean.http.response.FriendshipResponse
import com.darcy.kmpdemo.bean.ui.FriendsItemBean
import com.darcy.kmpdemo.exception.BaseException
import com.darcy.kmpdemo.log.logE
import com.darcy.kmpdemo.storage.database.tables.FriendshipEntity
import com.darcy.kmpdemo.storage.memory.IMGlobalStorage
import com.darcy.kmpdemo.ui.base.BaseViewModel
import com.darcy.kmpdemo.ui.base.IIntent
import com.darcy.kmpdemo.ui.base.IReducer
import com.darcy.kmpdemo.ui.base.impl.fetch.FetchIntent
import com.darcy.kmpdemo.ui.base.impl.paging.PagingIntent
import com.darcy.kmpdemo.ui.base.impl.screenstatus.ScreenState
import com.darcy.kmpdemo.ui.base.impl.screenstatus.ScreenStateIntent
import com.darcy.kmpdemo.ui.base.impl.tips.TipsIntent
import com.darcy.kmpdemo.ui.screen.phone.conversations.repository.ConversationRepository
import com.darcy.kmpdemo.ui.screen.phone.friends.intent.FriendsIntent
import com.darcy.kmpdemo.ui.screen.phone.friends.reducer.FriendsReducer
import com.darcy.kmpdemo.ui.screen.phone.friends.repository.FriendsRepository
import com.darcy.kmpdemo.ui.screen.phone.friends.state.FriendsState
import io.ktor.client.utils.EmptyContent.status
import kotlin.reflect.KClass

class FriendsViewModel(
    private val repository: FriendsRepository = FriendsRepository(),
    private val conversationRepository: ConversationRepository = ConversationRepository(),
) : BaseViewModel<FriendsState>() {
    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
                return FriendsViewModel() as T
            }
        }
    }

    override fun initState(): FriendsState {
        return FriendsState()
    }

    override fun initReducers(): List<IReducer<FriendsState>> {
        return listOf(FriendsReducer())
    }

    override fun dispatch(intent: IIntent) {
        when (intent) {
            is FetchIntent.ActionFetchData -> { // 获取数据
                actionFetchFriendsList()
            }

            is FriendsIntent.GoAddFriendPage -> {
                actionGoAddFriend()
            }

            is FriendsIntent.GoAcceptFriendPage -> {
                actionGoAccessFriend()
            }

            is FriendsIntent.GoChatPage -> {
                actionGoChatPage(intent.response)
            }

            is FriendsIntent.ActionDeleteFriend -> { // 删除好友
                actionDeleteFriend(intent.userId, intent.friendUserId)
            }

            is PagingIntent.ActionLoadNewPage -> {
                // 分页
            }

            else -> {
                super.dispatch(intent)
            }
        }
    }

    private fun actionGoChatPage(response: FriendshipResponse) {
        io {
            conversationRepository.createConversation(
                userId = IMGlobalStorage.getCurrentUserId().toString(),
                targetId = response.friend.id.toString(),
                conversationType = "1",
                onSuccess = {
                    io {
                        sendEvent(
                            FriendsEvent.GoChat(
                                conversationId = it.id,
                                userId = it.target.id,
                                userName = it.target.username,
                                userAvatar = it.target.avatar,
                            )
                        )
                    }
                },
                onError = {
                    logE("创建会话失败：$it")
                    main { dispatch(it.toTipsIntent()) }
                }
            )
        }
    }

    private fun actionGoAddFriend() {
        io {
            sendEvent(FriendsEvent.GoAddFriend)
        }
    }

    private fun actionGoAccessFriend() {
        io {
            sendEvent(FriendsEvent.GoAcceptFriend)
        }
    }

    private fun actionDeleteFriend(userId: Long, friendUserId: Long) {
        io {
            repository.deleteFriend(
                userId, friendUserId,
                onSuccess = {

                    main {
                        val tips = TipsIntent.ShowTips(
                            title = "提示",
                            tips = it,
                            code = 200,
                            middleButtonText = "确定"
                        )
                        dispatch(tips)
                        dispatch(FetchIntent.ActionFetchData())
                    }
                },
                onError = {
                    logE("删除好友失败：$it")
                    main { dispatch(it.toTipsIntent()) }
                })
        }
    }

    private fun actionFetchFriendsList() {
        io {
            dispatch(ScreenStateIntent.ScreenStateChange(ScreenState.Loading))
            val userId = IMGlobalStorage.getCurrentUserId()
            repository.fetchFriends(
                userId,
                onSuccessList = {
                    dispatch(ScreenStateIntent.ScreenStateChange(ScreenState.Success))
                    dispatch(FetchIntent.RefreshByFetchData(it))
                },
                onError = {
                    dispatchFailure(Exception(it.toString()))
                })
        }
    }

    private fun dispatchFailure(throwable: Throwable) {
        val code = if (throwable is BaseException) throwable.errorCode else -1
        val message = throwable.message ?: "Unknown Error"
        dispatch(ScreenStateIntent.ScreenStateChange(ScreenState.Error(code, message)))
    }
}