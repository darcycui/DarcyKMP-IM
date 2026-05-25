package com.darcy.kmpdemo.ui.screen.phone.accept_friend

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.darcy.kmpdemo.bean.http.error.ErrorResponse
import com.darcy.kmpdemo.bean.http.error.toTipsIntent
import com.darcy.kmpdemo.log.logD
import com.darcy.kmpdemo.log.logE
import com.darcy.kmpdemo.storage.memory.IMGlobalStorage
import com.darcy.kmpdemo.ui.base.BaseViewModel
import com.darcy.kmpdemo.ui.base.IIntent
import com.darcy.kmpdemo.ui.base.IReducer
import com.darcy.kmpdemo.ui.base.impl.fetch.FetchIntent
import com.darcy.kmpdemo.ui.screen.phone.accept_friend.intent.AcceptFriendIntent
import com.darcy.kmpdemo.ui.screen.phone.accept_friend.reducer.AcceptFriendReducer
import com.darcy.kmpdemo.ui.screen.phone.accept_friend.repository.AcceptFriendRepository
import com.darcy.kmpdemo.ui.screen.phone.accept_friend.state.AcceptFriendState
import com.darcy.kmpdemo.x3dh.repository.AliceHelloRepository
import com.darcy.kmpdemo.x3dh.usecase.CalculateBobX3DHKeyUseCase
import com.darcy.kmpdemo.x3dh.usecase.SaveBobSessionRecordUseCase
import kotlin.reflect.KClass

class AcceptFriendViewModel(
    private val acceptFriendRepository: AcceptFriendRepository = AcceptFriendRepository(),
    private val aliceHelloRepository: AliceHelloRepository = AliceHelloRepository(),
    private val calculateBobX3DHKeyUseCase: CalculateBobX3DHKeyUseCase = CalculateBobX3DHKeyUseCase(),
    private val saveBobSessionRecordUseCase: SaveBobSessionRecordUseCase = SaveBobSessionRecordUseCase(),
) : BaseViewModel<AcceptFriendState>() {
    companion object {
        val Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
                return AcceptFriendViewModel() as T
            }
        }
    }

    override fun initState(): AcceptFriendState {
        return AcceptFriendState()
    }

    override fun initReducers(): List<IReducer<AcceptFriendState>> {
        return listOf(AcceptFriendReducer())
    }

    override fun dispatch(intent: IIntent) {
        when (intent) {
            is AcceptFriendIntent.ActionAcceptFriend -> {
                pullAliceHello( intent.applyId, intent.targetUserId)
            }

            is FetchIntent.ActionFetchData -> {
                actionFetchFriendApplys()
            }

            else -> {
                super.dispatch(intent)
            }
        }
    }

    private fun actionFetchFriendApplys() {
        acceptFriendRepository.fetchFriendApplys(
            IMGlobalStorage.getCurrentUserId(),
            onSuccessList = {
                dispatch(FetchIntent.RefreshByFetchData(it))
            },
            onError = {
                logE("查询申请列表失败：$it")
                main { dispatch(it.toTipsIntent()) }
            })
    }

    private fun pullAliceHello(friendRequestId: Long, remoteUserId: Long) {
        val localUserId = IMGlobalStorage.getCurrentUserId()
        aliceHelloRepository.pullAliceHello(
            remoteUserId,
            localUserId,
            onSuccess = { alideKeys ->
                io {
                    logD("拉取 AliceHello 成功：$alideKeys")
                    val x3DHKey = calculateBobX3DHKeyUseCase.invoke(
                        mapOf(
                            "bobUserId" to localUserId.toString(),
                            "aliceIdentityKey" to alideKeys.aliceIdentityKey,
                            "aliceEphemeralKey" to alideKeys.aliceEphemeralKey,
                            "bobOneTimePreKeyId" to alideKeys.bobOneTimePreKeyId
                        ),Unit
                    ).onFailure {
                        it.printStackTrace()
                    }.getOrElse { null }
                    if (x3DHKey == null) {
                        logE("计算 BobX3DHKey 失败")
                        val error = ErrorResponse.create(message = "计算 BobX3DHKey 失败")
                        main { dispatch(error.toTipsIntent()) }
                        return@io
                    }
                    val saveSessionResult = saveBobSessionRecordUseCase.invoke(
                        mapOf(
                            "localUserId" to localUserId.toString(),
                            "remoteUserId" to remoteUserId.toString(),
                            "bobX3DHKey" to x3DHKey.toHexString(),
                            "aliceIdentityKey" to alideKeys.aliceIdentityKey,
                            "aliceEphemeralKey" to alideKeys.aliceEphemeralKey
                        ),Unit
                    ).onFailure {
                        it.printStackTrace()
                    }.getOrElse { false }
                    if (saveSessionResult.not()) {
                        logE("保存 SessionRecord 到数据库失败")
                        val error =
                            ErrorResponse.create(message = "保存 SessionRecord 到数据库失败")
                        main { dispatch(error.toTipsIntent()) }
                        return@io
                    }
                    actionAcceptFriend(friendRequestId)
                }
            },
            onError = {
                logE("拉取 AliceHello 失败：$it")
                main { dispatch(it.toTipsIntent()) }
            }
        )
    }

    private fun actionAcceptFriend(friendRequestId: Long) {
        acceptFriendRepository.acceptFriend(
            friendRequestId = friendRequestId,
            onSuccess = {
                dispatch(AcceptFriendIntent.RefreshByAcceptFriend(it))
            },
            onError = {
                logE("添加失败：$it")
                main { dispatch(it.toTipsIntent()) }
            })
    }
}