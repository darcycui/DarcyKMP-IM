package com.darcy.kmpdemo.ui.screen.phone.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.darcy.kmpdemo.bean.http.error.toTipsIntent
import com.darcy.kmpdemo.bean.http.response.FriendsResponse
import com.darcy.kmpdemo.bean.http.response.FriendshipResponse
import com.darcy.kmpdemo.bean.ui.FriendsItemBean
import com.darcy.kmpdemo.exception.BaseException
import com.darcy.kmpdemo.log.logE
import com.darcy.kmpdemo.repository.FriendshipDaoRepository
import com.darcy.kmpdemo.repository.FriendshipUserCrossRefDaoRepository
import com.darcy.kmpdemo.repository.UserDaoRepository
import com.darcy.kmpdemo.storage.database.tables.FriendshipEntity
import com.darcy.kmpdemo.storage.database.tables.FriendshipUserCrossRef
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
    private val userDaoRepository: UserDaoRepository = UserDaoRepository(),
    private val friendshipDaoRepository: FriendshipDaoRepository = FriendshipDaoRepository(),
    private val friendshipUserCrossRefDaoRepository: FriendshipUserCrossRefDaoRepository = FriendshipUserCrossRefDaoRepository(),
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

            is FriendsIntent.ActionAddFriend -> { // 添加好友
                actionAddFriend2(intent.userIdFrom, intent.userIdTo, intent.markName)
            }

            is FriendsIntent.ActionDeleteFriend -> { // 删除好友
                actionDeleteFriend(intent.userId, intent.friendUserId)
            }

            is FriendsIntent.ActionUpdateFriend -> { // 更新好友
                actionUpdateFriend()
            }

            is FriendsIntent.ActionQueryFriendsList -> { // 获取好友列表
                actionQueryFriendsList2(intent.userId)
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

    private fun actionQueryFriendsList2(userId: Long) {
        io {
            val userFriends = friendshipUserCrossRefDaoRepository.getUserFriends(userId)
            val uiBeanList: List<FriendsItemBean> = userFriends.friends.map { item ->
                val userIdSelected =
                    if (item.userIdFrom == userId) item.userIdTo else item.userIdFrom
                val userName = userDaoRepository.getUserById(userIdSelected).name
                val userAvatar = userDaoRepository.getUserById(userIdSelected).avatar
                FriendsItemBean(
                    id = userIdSelected,
                    name = userName,
                    nickName = item.markNameOfTo,
                    avatar = userAvatar,
                )
            }
            dispatch(FetchIntent.RefreshByFetchData(FriendsResponse(uiBeanList)))
        }
    }

    private fun actionUpdateFriend() {


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

    private fun actionAddFriend2(userIdFrom: Long, userIdTo: Long, markName: String) {
        io {
            friendshipDaoRepository.insert(
                FriendshipEntity(
                    userIdFrom = userIdFrom,
                    userIdTo = userIdTo,
                    markNameOfFrom = "",
                    markNameOfTo = markName,
                )
            )
            val friendshipId =
                friendshipDaoRepository.getByUserId(userIdFrom, userIdTo).friendshipId ?: -1L
            friendshipUserCrossRefDaoRepository.insert(
                FriendshipUserCrossRef(friendshipId, userIdFrom)
            )
            friendshipUserCrossRefDaoRepository.insert(
                FriendshipUserCrossRef(friendshipId, userIdTo)
            )
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