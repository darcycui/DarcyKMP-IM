package com.darcy.kmpdemo.ui.screen.phone.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.darcy.kmpdemo.bean.http.error.toTipsIntent
import com.darcy.kmpdemo.bean.ui.LoginBean
import com.darcy.kmpdemo.crypto.repository.DHExchangeRepository
import com.darcy.kmpdemo.log.logD
import com.darcy.kmpdemo.log.logE
import com.darcy.kmpdemo.log.logI
import com.darcy.kmpdemo.network.http.token.TokenManager
import com.darcy.kmpdemo.storage.memory.IMGlobalStorage
import com.darcy.kmpdemo.storage.memory.TransportGlobalStorage
import com.darcy.kmpdemo.ui.base.BaseViewModel
import com.darcy.kmpdemo.ui.base.IIntent
import com.darcy.kmpdemo.ui.base.IReducer
import com.darcy.kmpdemo.ui.screen.phone.login.event.LoginEvent
import com.darcy.kmpdemo.ui.screen.phone.login.intent.LoginIntent
import com.darcy.kmpdemo.ui.screen.phone.login.reducer.LoginReducer
import com.darcy.kmpdemo.ui.screen.phone.login.repository.LoginRepository
import com.darcy.kmpdemo.ui.screen.phone.login.state.LoginState
import com.darcy.kmpdemo.utils.hexStrToBytes
import com.darcy.kmpdemo.utils.toBytes
import com.darcy.kmpdemo.utils.toPublicKey
import com.darcy.kmpdemo.x3dh.exchange.ECCExchangeHelper
import kotlin.reflect.KClass

class LoginViewModel(
    private val loginRepository: LoginRepository = LoginRepository(),
    private val dhExchangeRepository: DHExchangeRepository = DHExchangeRepository(),
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
                        // 保存当前用户
                        IMGlobalStorage.setCurrentUser(it)
                        // 设置 token
                        TokenManager.setToken(it.token)
                        // 获取 Server DH公钥
                        actionExchangeDHPublicKey(it.id)
                    }
                },
                onError = {
                    logE("登录失败：$it")
                    main { dispatch(it.toTipsIntent()) }
                })
        }
    }

    private fun actionExchangeDHPublicKey(userId: Long) {
        io {
            val ephemeralKey = ECCExchangeHelper.generateKeyPair()
            logD("生成临时私钥：${ephemeralKey.privateKey.toBytes().toHexString()}")
            logD("生成临时公钥：${ephemeralKey.publicKey.toBytes().toHexString()}")
            dhExchangeRepository.getServerDHPublicKey(
                userId = userId,
                publicKey = ephemeralKey.publicKey.toBytes().toHexString(),
                onSuccess = {
                    io {
                        logI("获取 Server DH公钥成功：$it")
                        val sharedSecret = ECCExchangeHelper.getSharedSecret(
                            ephemeralKey.privateKey, it.publicKey.hexStrToBytes().toPublicKey()
                        ).toHexString()
                        // 保存到内存存储
                        TransportGlobalStorage.setServerSharedSecretKey(sharedSecret)
                        sendEvent(LoginEvent.LoginSuccessEvent)
                    }
                },
                onError = {
                    logE("获取 Server DH公钥失败：$it")
                    main { dispatch(it.toTipsIntent()) }
                })
        }
    }
}