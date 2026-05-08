package com.darcy.kmpdemo.ui.screen.phone.apply_friend

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.darcy.kmpdemo.bean.http.error.toTipsIntent
import com.darcy.kmpdemo.bean.http.response.ApplyFriendResponse
import com.darcy.kmpdemo.bean.ui.AddFriendBean
import com.darcy.kmpdemo.log.logE
import com.darcy.kmpdemo.storage.memory.IMGlobalStorage
import com.darcy.kmpdemo.ui.base.BaseViewModel
import com.darcy.kmpdemo.ui.base.IIntent
import com.darcy.kmpdemo.ui.base.IReducer
import com.darcy.kmpdemo.ui.base.impl.fetch.FetchIntent
import com.darcy.kmpdemo.ui.base.impl.tips.TipsIntent
import com.darcy.kmpdemo.ui.screen.phone.apply_friend.intent.ApplyFriendIntent
import com.darcy.kmpdemo.ui.screen.phone.apply_friend.reducer.ApplyFriendReducer
import com.darcy.kmpdemo.ui.screen.phone.apply_friend.repository.ApplyFriendRepository
import com.darcy.kmpdemo.ui.screen.phone.apply_friend.state.ApplyFriendState
import kmpdarcydemo.composeapp.generated.resources.Res
import kmpdarcydemo.composeapp.generated.resources.confirm
import org.jetbrains.compose.resources.getString
import kotlin.reflect.KClass

class ApplyFriendViewModel(
    private val repository: ApplyFriendRepository = ApplyFriendRepository(),
) : BaseViewModel<ApplyFriendState>() {
    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
                return ApplyFriendViewModel() as T
            }
        }
    }

    override fun initState(): ApplyFriendState {
        return ApplyFriendState()
    }

    override fun initReducers(): List<IReducer<ApplyFriendState>> {
        return listOf(ApplyFriendReducer())
    }

    override fun dispatch(intent: IIntent) {
        when (intent) {
            is ApplyFriendIntent.ActionSearchUser -> {
                actionSearchUser(intent.phone)
            }

            is FetchIntent.ActionFetchData -> {
                actionFetchFriendApplys()
            }

            is ApplyFriendIntent.ActionApplyFriend -> {
                actionApplyFriend(intent.userId)
            }

            else -> {
                super.dispatch(intent)

            }
        }
    }

    private fun actionFetchFriendApplys() {
        repository.fetchFriendApplys(
            fromUserId = IMGlobalStorage.getCurrentUserId(),
            onSuccessList = {
                dispatch(FetchIntent.RefreshByFetchData(it))
            },
            onError = {
                logE("获取申请列表失败：$it")
                main { dispatch(it.toTipsIntent()) }
            }
        )
    }

    private fun actionApplyFriend(userId: Long) {
        // todo apply friend
        val formUserId = IMGlobalStorage.getCurrentUserId()
        repository.applyFriend(
            AddFriendBean(formUserId, userId),
            onSuccess = {
                dispatch(ApplyFriendIntent.RefreshByApplyFriend(it))
            },
            onError = {
                logE("申请失败：$it")
                main { dispatch(it.toTipsIntent()) }
            })

    }

    private fun actionSearchUser(phone: String) {
        // todo search user
        repository.searchUser(
            phone,
            onSuccess = {
                dispatch(ApplyFriendIntent.RefreshBySearchUser(it))
            },
            onError = {
                logE("搜索失败：$it")
                main { dispatch(it.toTipsIntent()) }
            })
    }
}