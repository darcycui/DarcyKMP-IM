package com.darcy.kmpdemo.ui.screen.phone.chat.privatechat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.darcy.kmpdemo.bean.http.error.toTipsIntent
import com.darcy.kmpdemo.bean.http.response.PrivateMessageResponse
import com.darcy.kmpdemo.bean.http.response.toSTOMPMessage
import com.darcy.kmpdemo.bean.websocket.stomp.toPrivateMessageResponse
import com.darcy.kmpdemo.log.logD
import com.darcy.kmpdemo.log.logE
import com.darcy.kmpdemo.log.logI
import com.darcy.kmpdemo.log.logW
import com.darcy.kmpdemo.storage.memory.IMGlobalStorage
import com.darcy.kmpdemo.ui.base.BaseViewModel
import com.darcy.kmpdemo.ui.base.IIntent
import com.darcy.kmpdemo.ui.base.IReducer
import com.darcy.kmpdemo.ui.base.impl.fetch.FetchIntent
import com.darcy.kmpdemo.ui.screen.phone.chat.privatechat.intent.ChatIntent
import com.darcy.kmpdemo.ui.screen.phone.chat.privatechat.reducer.ChatReducer
import com.darcy.kmpdemo.ui.screen.phone.chat.privatechat.repository.ChatRepository
import com.darcy.kmpdemo.ui.screen.phone.chat.privatechat.repository.WebsocketRepository
import com.darcy.kmpdemo.ui.screen.phone.chat.privatechat.state.ChatState
import com.darcy.kmpdemo.ui.screen.phone.chat.privatechat.state.WebSocketConnectionState
import com.darcy.kmpdemo.x3dh.MessageKey
import com.darcy.kmpdemo.x3dh.usecase.DoubleRatchetSendStepUseCase
import kotlin.reflect.KClass

class ChatViewModel(
    private val chatRepository: ChatRepository = ChatRepository(),
    private val websocketRepository: WebsocketRepository = WebsocketRepository,
    private val doubleRatchetSendStepUseCase: DoubleRatchetSendStepUseCase = DoubleRatchetSendStepUseCase(),
) : BaseViewModel<ChatState>() {
    companion object {
        private const val TAG = "ChatViewModel"
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
                return ChatViewModel() as T
            }
        }
    }

    override fun initState(): ChatState {
        return ChatState()
    }

    override fun initReducers(): List<IReducer<ChatState>> {
        return listOf(
            ChatReducer(),
        )
    }

    override fun dispatch(intent: IIntent) {
        when (intent) {
            is FetchIntent.ActionFetchData -> {
                actionFetchMessages(intent.params)
            }

            is ChatIntent.ActionSendMessage -> {
                actionSendMessage(intent.message)
            }

            is ChatIntent.ActionRegisterReceiveMessage -> {
                actionRegisterReceiveMessage()
                actionRegisterConnectionState()
            }

            else -> {
                super.dispatch(intent)
            }
        }
    }

    private fun actionFetchMessages(params: Map<String, String>) {
        // todo: http拉取最新消息
        chatRepository.fetchMessages(
            userId = IMGlobalStorage.getCurrentUserId(),
            conversationId = params["conversationId"]?.toLongOrNull() ?: 0L,
            page = 0,
            size = 10,
            onSuccess = {
                dispatch(FetchIntent.RefreshByFetchData(it))
            },
            onError = {
                logE("拉取消息错误")
                main { dispatch(it.toTipsIntent()) }
            }
        )
    }

    private fun actionSendMessage(message: PrivateMessageResponse) {
        io {
            // todo: websocket发送消息 添加 DH棘轮 公钥
            val localUserId = IMGlobalStorage.getCurrentUserId()
            val remoteUserId = message.receiverId
            val messageKeyLocal = doubleRatchetSendStepUseCase.invoke(
                mapOf(
                    "localUserId" to localUserId.toString(),
                    "remoteUserId" to remoteUserId.toString()
                )
            ).onFailure {
                logE("发送时计算messageKey错误: ${it.message}")
                it.printStackTrace()
            }.getOrElse { MessageKey() }
            logD("$TAG sendMessage messageKeyLocal: $messageKeyLocal")
            /**
             * 发送消息
             */
            websocketRepository.sendMessage(
                message.toSTOMPMessage(),
                messageKeyLocal.toMap()
            )
            dispatch(ChatIntent.RefreshBySendMessage(message))
        }
    }

    private fun actionRegisterConnectionState() {
        io {
            websocketRepository.connect()
            websocketRepository.connectionStateFlow.collect { state ->
                main {
                    logW("$TAG WebSocket 状态改变: ${state.message}")
                    dispatch(ChatIntent.WebSocketState(state))
                }
            }
        }
    }

    private fun actionRegisterReceiveMessage() {
        io {
            io {
                websocketRepository.messageFlow.collect { message ->
                    logE("接收到消息: $message")
                    dispatch(ChatIntent.RefreshByReceiveMessage(message.toPrivateMessageResponse()))
                }
            }
        }
    }

    override fun onCleared() {
        logE("销毁：onCleared")
        websocketRepository.disconnect()
        super.onCleared()
    }
}