package com.darcy.kmpdemo.ui.screen.phone.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.darcy.kmpdemo.bean.http.error.toTipsIntent
import com.darcy.kmpdemo.bean.ui.LoginBean
import com.darcy.kmpdemo.bean.http.response.UsersResponse
import com.darcy.kmpdemo.bean.ui.UserItemBean
import com.darcy.kmpdemo.log.logE
import com.darcy.kmpdemo.storage.database.tables.UserEntity
import com.darcy.kmpdemo.storage.memory.IMGlobalStorage
import com.darcy.kmpdemo.ui.base.BaseViewModel
import com.darcy.kmpdemo.ui.base.IIntent
import com.darcy.kmpdemo.ui.base.IReducer
import com.darcy.kmpdemo.ui.base.impl.fetch.FetchIntent
import com.darcy.kmpdemo.ui.base.impl.tips.TipsIntent
import com.darcy.kmpdemo.ui.screen.phone.login.event.LoginEvent
import com.darcy.kmpdemo.ui.screen.phone.login.intent.LoginIntent
import com.darcy.kmpdemo.ui.screen.phone.login.reducer.LoginReducer
import com.darcy.kmpdemo.ui.screen.phone.login.repository.LoginRepository
import com.darcy.kmpdemo.ui.screen.phone.login.state.LoginState
import kmpdarcydemo.composeapp.generated.resources.Res
import kmpdarcydemo.composeapp.generated.resources.confirm
import org.jetbrains.compose.resources.getString
import kotlin.reflect.KClass

class LoginViewModel(
    private val loginRepository: LoginRepository = LoginRepository(),
) : BaseViewModel<LoginState>() {
    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
                return LoginViewModel() as T
            }
        }
    }

    override fun initState(): LoginState {
        return LoginState()
    }

    override fun initReducers(): List<IReducer<LoginState>> {
        return listOf(LoginReducer())
    }

    override fun dispatch(intent: IIntent) {
        when (intent) {
            is LoginIntent.ActionLogin -> { // 登录
                actionLogin(intent.userEntity)
            }

            is LoginIntent.ActionGoRegister -> { // 注册
                actionGoRegister()
            }

            else -> {
                super.dispatch(intent)
            }
        }
    }

    private fun actionGoRegister() {
        io {
            sendEvent(LoginEvent.GoRegisterEvent)
        }
    }

    private fun actionLogin(userEntity: LoginBean) {
        io {
            loginRepository.login(
                userEntity.phone,
                userEntity.password,
                onSuccess = {
                    io {
                        IMGlobalStorage.setCurrentUser(it)
                        sendEvent(LoginEvent.LoginSuccessEvent)
                    }
                },
                onError = {
                    logE("登录失败：$it")
                    main { dispatch(it.toTipsIntent()) }
                })
        }
    }
}