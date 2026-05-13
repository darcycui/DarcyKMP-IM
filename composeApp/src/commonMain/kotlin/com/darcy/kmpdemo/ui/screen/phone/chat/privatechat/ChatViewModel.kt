package com.darcy.kmpdemo.ui.screen.phone.chat.privatechat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.darcy.kmpdemo.bean.http.error.toTipsIntent
import com.darcy.kmpdemo.bean.http.response.PrivateMessageResponse
import com.darcy.kmpdemo.bean.http.response.toSTOMPMessage
import com.darcy.kmpdemo.bean.websocket.stomp.STOMPMessage
import com.darcy.kmpdemo.bean.websocket.stomp.toPrivateMessageResponse
import com.darcy.kmpdemo.log.logE
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
import kotlin.reflect.KClass

class ChatViewModel(
    private val chatRepository: ChatRepository = ChatRepository(),
    private val websocketRepository: WebsocketRepository = WebsocketRepository,
) : BaseViewModel<ChatState>() {
    companion object {
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
        // todo: websocket发送消息 添加 DH棘轮 公钥
        val headers = mapOf(
            "dhPublicKey" to ""
        )
        websocketRepository.sendMessage(message.toSTOMPMessage(), headers)
        dispatch(ChatIntent.RefreshBySendMessage(message))
    }

    private fun actionRegisterReceiveMessage() {
        io {
            websocketRepository.connect()
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