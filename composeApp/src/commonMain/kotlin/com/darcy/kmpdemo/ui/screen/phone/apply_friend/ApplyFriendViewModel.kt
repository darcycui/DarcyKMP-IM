package com.darcy.kmpdemo.ui.screen.phone.apply_friend

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.darcy.kmpdemo.bean.http.error.ErrorResponse
import com.darcy.kmpdemo.bean.http.error.toTipsIntent
import com.darcy.kmpdemo.bean.ui.AddFriendBean
import com.darcy.kmpdemo.log.logE
import com.darcy.kmpdemo.log.logV
import com.darcy.kmpdemo.storage.database.daos.IdentityKeyDao
import com.darcy.kmpdemo.storage.database.daos.SessionRecordDao
import com.darcy.kmpdemo.storage.database.getDarcyIMDatabase
import com.darcy.kmpdemo.storage.memory.IMGlobalStorage
import com.darcy.kmpdemo.ui.base.BaseViewModel
import com.darcy.kmpdemo.ui.base.IIntent
import com.darcy.kmpdemo.ui.base.IReducer
import com.darcy.kmpdemo.ui.base.impl.fetch.FetchIntent
import com.darcy.kmpdemo.ui.screen.phone.apply_friend.event.ApplyFriendEvent
import com.darcy.kmpdemo.ui.screen.phone.apply_friend.intent.ApplyFriendIntent
import com.darcy.kmpdemo.ui.screen.phone.apply_friend.reducer.ApplyFriendReducer
import com.darcy.kmpdemo.ui.screen.phone.apply_friend.repository.ApplyFriendRepository
import com.darcy.kmpdemo.ui.screen.phone.apply_friend.state.ApplyFriendState
import com.darcy.kmpdemo.x3dh.repository.AliceHelloRepository
import com.darcy.kmpdemo.x3dh.repository.X3DHRepository
import com.darcy.kmpdemo.x3dh.usecase.CalculateAliceX3DHKeyUseCase
import com.darcy.kmpdemo.utils.JsonHelper
import com.darcy.kmpdemo.utils.toBytes
import com.darcy.kmpdemo.x3dh.usecase.InitAliceDHRatchetUseCase
import com.darcy.kmpdemo.x3dh.usecase.SaveAliceSessionRecordUseCase
import kotlin.reflect.KClass

class ApplyFriendViewModel(
    private val applyFriendRepository: ApplyFriendRepository = ApplyFriendRepository(),
    private val aliceHelloRepository: AliceHelloRepository = AliceHelloRepository(),
    private val identityKeyDao: IdentityKeyDao = getDarcyIMDatabase().identityKeyDao(),
    private val x3DHRepository: X3DHRepository = X3DHRepository(),
    private val calculateAliceX3DHKeyUseCase: CalculateAliceX3DHKeyUseCase = CalculateAliceX3DHKeyUseCase(),
    private val saveAliceX3DHKeyUseCase: SaveAliceSessionRecordUseCase = SaveAliceSessionRecordUseCase(),
    private val sessionRecordDao: SessionRecordDao = getDarcyIMDatabase().sessionRecordDao(),
    private val initAliceDHRatchetUseCase: InitAliceDHRatchetUseCase = InitAliceDHRatchetUseCase(),
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

            is ApplyFriendIntent.ActionPageBack -> {
                io {
                    sendEvent(ApplyFriendEvent.PageBack)
                }
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
            aliceUserId = aliceUserId,
            bobUserId = bobUserId,
            onSuccess = { bobKeys ->
                io {
                    logE("拉取 BobKeys 成功：$bobKeys")
                    val resultPair = calculateAliceX3DHKeyUseCase.invoke(
                        mapOf(
                            "aliceUserId" to aliceUserId.toString(),
                            "bobUserId" to aliceUserId.toString(),
                            "bobKeys" to JsonHelper.toJson(bobKeys),
                        ), Unit
                    ).onFailure {
                        it.printStackTrace()
                    }.getOrElse { Pair(null, null) }
                    // 注意这里 使用一个局部变量来保存这个值 这样检查后就不为空 可以传递给 saveAliceX3DHKeyUseCase
                    val x3DHKey = resultPair.first
                    val aliceEphemeralKey = resultPair.second
                    if (x3DHKey == null || aliceEphemeralKey == null) {
                        val error = ErrorResponse.create(message = "计算 AliceX3DHKey 失败")
                        logE("计算 AliceX3DHKey 失败：")
                        main { dispatch(error.toTipsIntent()) }
                        return@io
                    }
                    // 保存 sessionRecord 到数据库
                    val saveSessionResult = saveAliceX3DHKeyUseCase.invoke(
                        mapOf(
                            "aliceUserId" to aliceUserId.toString(),
                            "bobUserId" to bobUserId.toString(),
                            "aliceX3DHKey" to x3DHKey.toHexString(),
                            "aliceEphemeralPrivateKey" to aliceEphemeralKey.privateKey.toBytes()
                                .toHexString(),
                            "aliceEphemeralPublicKey" to aliceEphemeralKey.publicKey.toBytes()
                                .toHexString(),
                            "bobIdentityKey" to bobKeys.identityKey,
                            "bobSignedPreKey" to bobKeys.signedPreKey,
                        ), Unit
                    ).onFailure {
                        it.printStackTrace()
                    }.getOrElse { false }
                    if (saveSessionResult.not()) {
                        val error =
                            ErrorResponse.create(message = "保存 sessionRecord 到数据库失败")
                        logE("保存 sessionRecord 到数据库失败：")
                        main { dispatch(error.toTipsIntent()) }
                        return@io
                    }
                    pushAliceHello(aliceUserId, bobUserId, bobKeys.oneTimePreKeyId, bobKeys.signedPreKey)
                }
            },
            onError = {
                logE("拉取 BobKeys 失败：$it")
                main { dispatch(it.toTipsIntent()) }
            }
        )
    }

    private suspend fun pushAliceHello(
        aliceUserId: Long,
        bobUserId: Long,
        oneTimePreKeyId: String,
        bobSignedPreKey: String,
    ) {
        val session = sessionRecordDao.getByUserId(aliceUserId, bobUserId)
        if (session == null) {
            logE("sessionRecord 不存在")
            val error = ErrorResponse.create(message = "sessionRecord 不存在")
            main { dispatch(error.toTipsIntent()) }
            return
        }
        val identityKey = identityKeyDao.getByUserId(aliceUserId)
        if (identityKey == null) {
            logE("identityKey 不存在")
            val error = ErrorResponse.create(message = "identityKey 不存在")
            main { dispatch(error.toTipsIntent()) }
            return
        }
        aliceHelloRepository.pushAliceHello(
            aliceUserId = aliceUserId,
            bobUserId = bobUserId,
            aliceIdentityKey = identityKey.publicKey,
            // 获取存储的临时密钥
            aliceEphemeralKey = session.localEphemeralPublicKey,
            bobOneTimePreKeyId = oneTimePreKeyId,
            onSuccess = {
                logE("发送 AliceHello 成功：$it")
                // hello消息发送成功后 再发送好友申请
                doApplyFriendInner(aliceUserId, bobUserId, bobSignedPreKey)
            },
            onError = {
                logE("发送 AliceHello 失败：$it")
                main { dispatch(it.toTipsIntent()) }
            }
        )
    }

    private fun doApplyFriendInner(formUserId: Long, toUserId: Long, bobSignedPreKey: String) {
        applyFriendRepository.applyFriend(
            AddFriendBean(formUserId, toUserId),
            onSuccess = { response ->
                dispatch(ApplyFriendIntent.RefreshByApplyFriend(response))
                // alice 初始化DH棘轮 为发送消息做准备
                io {
                    val initAliceDHRatchetResult = initAliceDHRatchetUseCase.invoke(
                        mapOf(
                            "aliceUserId" to formUserId.toString(),
                            "bobUserId" to toUserId.toString(),
                            "bobSignedPreKey" to bobSignedPreKey,
                        ), Unit
                    ).onSuccess {
                        logV("alice初始化DH棘轮成功")
                    }.onFailure {
                        logE("alice初始化DH棘轮失败：${it::class.simpleName} ${it.message}")
                        it.printStackTrace()
                    }.getOrElse { false }
                    if (initAliceDHRatchetResult.not()) {
                        val error = ErrorResponse.create(message = "alice初始化DH棘轮失败")
                        main { dispatch(error.toTipsIntent()) }
                        return@io
                    }
                }
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