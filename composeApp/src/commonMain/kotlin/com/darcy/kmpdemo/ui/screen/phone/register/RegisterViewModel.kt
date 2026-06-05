package com.darcy.kmpdemo.ui.screen.phone.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.darcy.kmpdemo.bean.http.error.ErrorResponse
import com.darcy.kmpdemo.bean.http.error.toTipsIntent
import com.darcy.kmpdemo.bean.http.request.toDTO
import com.darcy.kmpdemo.crypto.repository.DHExchangeRepository
import com.darcy.kmpdemo.log.logD
import com.darcy.kmpdemo.log.logE
import com.darcy.kmpdemo.log.logI
import com.darcy.kmpdemo.storage.database.tables.OneTimePreKeyEntity
import com.darcy.kmpdemo.storage.memory.IMGlobalStorage
import com.darcy.kmpdemo.storage.memory.TransportGlobalStorage
import com.darcy.kmpdemo.ui.base.BaseViewModel
import com.darcy.kmpdemo.ui.base.IIntent
import com.darcy.kmpdemo.ui.base.IReducer
import com.darcy.kmpdemo.ui.screen.phone.login.event.LoginEvent
import com.darcy.kmpdemo.ui.screen.phone.register.event.RegisterEvent
import com.darcy.kmpdemo.ui.screen.phone.register.intent.RegisterIntent
import com.darcy.kmpdemo.ui.screen.phone.register.reducer.RegisterReducer
import com.darcy.kmpdemo.ui.screen.phone.register.repository.RegisterRepository
import com.darcy.kmpdemo.ui.screen.phone.register.state.RegisterState
import com.darcy.kmpdemo.utils.JsonHelper
import com.darcy.kmpdemo.utils.hexStrToBytes
import com.darcy.kmpdemo.utils.toBytes
import com.darcy.kmpdemo.utils.toPublicKey
import com.darcy.kmpdemo.x3dh.exchange.ECCExchangeHelper
import com.darcy.kmpdemo.x3dh.usecase.GenerateIdentityKeyUseCase
import com.darcy.kmpdemo.x3dh.usecase.GenerateOneTimePreKeysUseCase
import com.darcy.kmpdemo.x3dh.usecase.GenerateSignedPreKeyUseCase
import com.darcy.kmpdemo.x3dh.repository.X3DHRepository
import kotlin.reflect.KClass

class RegisterViewModel(
    private val registerRepository: RegisterRepository = RegisterRepository(),
    private val identityKeyUseCase: GenerateIdentityKeyUseCase = GenerateIdentityKeyUseCase(),
    private val signedPreKeyUseCase: GenerateSignedPreKeyUseCase = GenerateSignedPreKeyUseCase(),
    private val oneTimePreKeyUseCase: GenerateOneTimePreKeysUseCase = GenerateOneTimePreKeysUseCase(),
    private val dhExchangeRepository: DHExchangeRepository = DHExchangeRepository(),
    private val x3DHRepository: X3DHRepository = X3DHRepository(),
) : BaseViewModel<RegisterState>() {
    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
                return RegisterViewModel() as T
            }
        }
    }

    override fun initState(): RegisterState {
        return RegisterState()
    }

    override fun initReducers(): List<IReducer<RegisterState>> {
        return listOf(RegisterReducer())
    }

    override fun dispatch(intent: IIntent) {
        when (intent) {
            is RegisterIntent.ActionRegister -> {
                actionRegister(intent)
            }

            else -> super.dispatch(intent)
        }
    }

    private fun actionRegister(intent: RegisterIntent.ActionRegister) {
        io {
            registerRepository.register(
                intent.bean,
                onSuccess = {
                    io {
                        IMGlobalStorage.setCurrentUser(it)
                        // 获取 Server DH公钥
                        actionExchangeDHPublicKey(it.id)
                    }
                },
                onError = {
                    logE("注册失败：$it")
                    main { dispatch(it.toTipsIntent()) }
                }
            )
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
                    logI("获取 Server DH公钥成功：$it")
                    val sharedSecret = ECCExchangeHelper.getSharedSecret(
                        ephemeralKey.privateKey, it.publicKey.hexStrToBytes().toPublicKey()
                    ).toHexString()
                    io {
                        // 保存到内存存储
                        TransportGlobalStorage.setServerSharedSecretKey(sharedSecret)
                        val (identityKey, signedPreKey, oneTimePreKeys) = generateX3DHKeys()
                        if (identityKey.isNotEmpty() && signedPreKey.isNotEmpty() && oneTimePreKeys.isNotEmpty()) {
                            pushX3DHKeys(identityKey, signedPreKey, oneTimePreKeys)
                        } else {
                            logE("生成X3DH密钥失败")
                            //main { dispatch(ErrorResponse(message = "生成X3DH密钥失败").toTipsIntent()) }
                        }
                    }
                },
                onError = {
                    logE("获取 Server DH公钥失败：$it")
                    main { dispatch(it.toTipsIntent()) }
                })
        }
    }

    suspend fun pushX3DHKeys(
        identityKey: String,
        signedPreKey: String,
        oneTimePreKeys: List<OneTimePreKeyEntity>
    ) {
        x3DHRepository.pushX3DHKeys(
            userId = IMGlobalStorage.getCurrentUserId(),
            identityKey = identityKey,
            signedPreKey = signedPreKey,
            oneTimePreKeys = oneTimePreKeys.map { it.toDTO() },
            onSuccess = {
                io {
                    logE("推送X3DH密钥成功：$it")
                    sendEvent(RegisterEvent.RegisterSuccessEvent)
                }
            },
            onError = {
                logE("推送X3DH密钥失败：$it")
                main { dispatch(it.toTipsIntent()) }
            }
        )
    }

    suspend fun generateX3DHKeys(): Triple<String, String, List<OneTimePreKeyEntity>> {
        var paramsMap = mapOf(
            "userId" to IMGlobalStorage.getCurrentUserId().toString(),
        )
        val identityKeyEntity = identityKeyUseCase.invoke(paramsMap, Unit).onFailure {
            it.printStackTrace()
            logE("生成X3DH密钥 identityKey 失败：$it")
        }.getOrElse { null }
        if (identityKeyEntity == null) {
            val error = ErrorResponse(message = "生成X3DH密钥 identityKey 失败")
            main { dispatch(error.toTipsIntent()) }
            return Triple("", "", emptyList())
        }
        val identityKey: String =
            identityKeyUseCase.invoke(paramsMap, Unit).map { it.publicKey }.getOrElse { "" }

        paramsMap = paramsMap + ("identityKeyId" to identityKeyEntity.keyId)
        val signedPreKey: String = signedPreKeyUseCase.invoke(paramsMap, Unit).onFailure {
            it.printStackTrace()
            logE("生成X3DH密钥 signedPreKey 失败：$it")
        }.map { it.publicKey }.getOrElse { "" }

        val oneTimePreKeys: List<OneTimePreKeyEntity> =
            oneTimePreKeyUseCase.invoke(paramsMap, Unit).onFailure {
                it.printStackTrace()
                logE("生成X3DH密钥 oneTimePreKeys 失败：$it")
            }.getOrElse { emptyList() }.map {
                it.privateKey = ""
                it
            }
//        val oneTimePreKeysStr = JsonHelper.toJson(oneTimePreKeys)
        return Triple(identityKey, signedPreKey, oneTimePreKeys)
    }
}