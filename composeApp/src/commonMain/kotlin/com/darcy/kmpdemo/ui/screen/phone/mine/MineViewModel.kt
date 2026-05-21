package com.darcy.kmpdemo.ui.screen.phone.mine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.darcy.kmpdemo.bean.http.response.LoginResponse
import com.darcy.kmpdemo.bean.http.response.MineResponse
import com.darcy.kmpdemo.bean.ui.UserItemBean
import com.darcy.kmpdemo.log.logD
import com.darcy.kmpdemo.platform.FilePlatform
import com.darcy.kmpdemo.storage.memory.IMGlobalStorage
import com.darcy.kmpdemo.ui.base.BaseViewModel
import com.darcy.kmpdemo.ui.base.IIntent
import com.darcy.kmpdemo.ui.base.IReducer
import com.darcy.kmpdemo.ui.base.impl.fetch.FetchIntent
import com.darcy.kmpdemo.ui.screen.phone.mine.event.MineEvent
import com.darcy.kmpdemo.ui.screen.phone.mine.intent.MineIntent
import com.darcy.kmpdemo.ui.screen.phone.mine.reducer.MineReducer
import com.darcy.kmpdemo.ui.screen.phone.mine.state.MineState
import com.darcy.kmpdemo.utils.PickHelper
import kotlin.reflect.KClass

class MineViewModel : BaseViewModel<MineState>() {
    companion object {
        val Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
                return MineViewModel() as T
            }
        }
    }

    override fun initState(): MineState {
        return MineState()
    }

    override fun initReducers(): List<IReducer<MineState>> {
        return listOf(MineReducer())
    }

    override fun dispatch(intent: IIntent) {
        when (intent) {
            is FetchIntent.ActionFetchData -> { // 加载数据
                actionLoadData()
            }

            is MineIntent.ActionLogout -> { // 退出登录
                actionLogout()
            }

            is MineIntent.ActionPickImage -> { // 选择图片
                actionPickImage()
            }

            else -> {
                super.dispatch(intent)
            }
        }
    }

    private fun actionLoadData() {
        io {
            val user = IMGlobalStorage.getCurrentUser()
            val uiBean = UserItemBean(
                id = user.id,
                name = user.username,
                nickName = user.nickname,
                age = user.age,
                sex = user.gender,
                avatar = user.avatar,
            )
            dispatch(FetchIntent.RefreshByFetchData(MineResponse(uiBean)))
        }
    }

    private fun actionLogout() {
        io {
            IMGlobalStorage.setCurrentUser(LoginResponse.empty())
            sendEvent(MineEvent.ActionLogout)
        }
    }

    private fun actionPickImage() {
        io {
            val path = PickHelper.pickImage()
            logD("pick image: $path")
            val cachePath = FilePlatform.dealUriIfNeed(path)
            dispatch(MineIntent.RefreshAvatar(cachePath.toString()))
        }
    }
}