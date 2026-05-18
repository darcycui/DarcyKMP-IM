package com.darcy.kmpdemo.ui.screen.phone.apply_friend

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.darcy.kmpdemo.bean.http.error.ErrorResponse
import com.darcy.kmpdemo.bean.http.error.toTipsIntent
import com.darcy.kmpdemo.bean.http.request.X3DHAliceHelloRequest
import com.darcy.kmpdemo.bean.http.response.X3DHKeysPullResponse
import com.darcy.kmpdemo.bean.ui.AddFriendBean
import com.darcy.kmpdemo.log.logE
import com.darcy.kmpdemo.storage.database.daos.IdentityKeyDao
import com.darcy.kmpdemo.storage.database.getDarcyIMDatabase
import com.darcy.kmpdemo.storage.memory.IMGlobalStorage
import com.darcy.kmpdemo.storage.memory.X3DHGlobalStorage
import com.darcy.kmpdemo.ui.base.BaseViewModel
import com.darcy.kmpdemo.ui.base.IIntent
import com.darcy.kmpdemo.ui.base.IReducer
import com.darcy.kmpdemo.ui.base.impl.fetch.FetchIntent
import com.darcy.kmpdemo.ui.screen.phone.apply_friend.intent.ApplyFriendIntent
import com.darcy.kmpdemo.ui.screen.phone.apply_friend.reducer.ApplyFriendReducer
import com.darcy.kmpdemo.ui.screen.phone.apply_friend.repository.ApplyFriendRepository
import com.darcy.kmpdemo.ui.screen.phone.apply_friend.state.ApplyFriendState
import com.darcy.kmpdemo.ui.screen.phone.x3dh.repository.FirstHelloRepository
import com.darcy.kmpdemo.ui.screen.phone.x3dh.repository.X3DHRepository
import com.darcy.kmpdemo.ui.screen.phone.x3dh.usecase.CalculateAliceX3DHKeyUseCase
import com.darcy.kmpdemo.utils.JsonHelper
import com.darcy.kmpdemo.utils.toBytes
import dev.whyoleg.cryptography.algorithms.XDH
import kotlin.emptyArray
import kotlin.reflect.KClass

class ApplyFriendViewModel(
    private val applyFriendRepository: ApplyFriendRepository = ApplyFriendRepository(),
    private val firstHelloRepository: FirstHelloRepository = FirstHelloRepository(),
    private val identityKeyDao: IdentityKeyDao = getDarcyIMDatabase().identityKeyDao(),
    private val x3DHRepository: X3DHRepository = X3DHRepository(),
    private val calculateAliceX3DHKeyUseCase: CalculateAliceX3DHKeyUseCase = CalculateAliceX3DHKeyUseCase(),
    private val saveAliceX3DHKeyUseCase: CalculateAliceX3DHKeyUseCase = CalculateAliceX3DHKeyUseCase()
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
                pullX3DHBobKeys(IMGlobalStorage.getCurrentUserId(), intent.toUserId)
            }

            else -> {
                super.dispatch(intent)

            }
        }
    }

    private fun actionFetchFriendApplys() {
        applyFriendRepository.fetchFriendApplys(
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

    private fun pullX3DHBobKeys(aliceUserId: Long, bobUserId: Long) {
        x3DHRepository.pullX3DHBobKeys(
            bobUserId = bobUserId,
            onSuccess = {
                io {
                    logE("拉取 BobKeys 成功：$it")
                    val resultPair = calculateAliceX3DHKeyUseCase.invoke(
                        mapOf(
                            "bobUserId" to aliceUserId.toString(),
                            "bobKeys" to JsonHelper.toJson(it),
                        )
                    ).getOrElse { Pair(null, null) }
                    // 注意这里 使用一个局部变量来保存这个值 这样检查后就不为空 可以传递给 saveAliceX3DHKeyUseCase
                    val x3DHKey = resultPair.first
                    val aliceEphemeralKey = resultPair.second
                    if (x3DHKey == null || aliceEphemeralKey == null) {
                        val error = ErrorResponse.create(message = "计算 AliceX3DHKey 失败")
                        logE("计算 AliceX3DHKey 失败：")
                        main { dispatch(error.toTipsIntent()) }
                        return@io
                    }
                    // todo 保存 sessionRecord 到数据库
                    saveAliceX3DHKeyUseCase.invoke(
                        mapOf(
                            "aliceUserId" to aliceUserId.toString(),
                            "bobUserId" to bobUserId.toString(),
                            "aliceX3DHKey" to x3DHKey.toHexString(),
                            "aliceEphemeralPrivateKey" to aliceEphemeralKey.privateKey.toBytes().toHexString(),
                            "aliceEphemeralPublicKey" to aliceEphemeralKey.publicKey.toBytes().toHexString(),
                            "bobIdentityKey" to it.identityKey,
                            "bobSignedPreKey" to it.signedPreKey,
                        )
                    )
                    sendAliceHello(aliceUserId, bobUserId)
                }
            },
            onError = {
                logE("拉取 BobKeys 失败：$it")
                main { dispatch(it.toTipsIntent()) }
            }
        )
    }

    private suspend fun sendAliceHello(aliceUserId: Long, bobUserId: Long) {
        val identityKey = identityKeyDao.getByUserId(aliceUserId)
            ?: throw Exception("未找到用户 $aliceUserId 的密钥 identityKey")
        firstHelloRepository.sendAliceHello(
            bean = X3DHAliceHelloRequest(
                identityKey = identityKey.publicKey,
                // 获取存储的临时密钥
                ephemeralKey = X3DHGlobalStorage.getX3DHEphemeralKey(bobUserId)
            ),
            onSuccess = {
                logE("发送 AliceHello 成功：$it")
                // hello消息发送成功后 再发送好友申请
                doApplyFriendInner(aliceUserId, bobUserId)
            },
            onError = {
                logE("发送 AliceHello 失败：$it")
                main { dispatch(it.toTipsIntent()) }
            }
        )
    }

    private fun doApplyFriendInner(formUserId: Long, toUserId: Long) {
        applyFriendRepository.applyFriend(
            AddFriendBean(formUserId, toUserId),
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
        applyFriendRepository.searchUser(
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