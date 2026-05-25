package com.darcy.kmpdemo.ui.screen.phone.chat.privatechat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.darcy.kmpdemo.bean.http.error.ErrorResponse
import com.darcy.kmpdemo.bean.http.error.toTipsIntent
import com.darcy.kmpdemo.bean.http.response.PrivateMessageResponse
import com.darcy.kmpdemo.bean.http.response.PrivateMessageResponsePage
import com.darcy.kmpdemo.bean.http.response.isSelfSent
import com.darcy.kmpdemo.bean.http.response.toEntity
import com.darcy.kmpdemo.bean.http.response.toSTOMPMessage
import com.darcy.kmpdemo.bean.websocket.stomp.toPrivateMessageResponse
import com.darcy.kmpdemo.log.logD
import com.darcy.kmpdemo.log.logE
import com.darcy.kmpdemo.log.logV
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
import com.darcy.kmpdemo.ui.screen.phone.chat.usecase.SaveOfflineToDBMessageUseCase
import com.darcy.kmpdemo.x3dh.MessageKey
import com.darcy.kmpdemo.x3dh.usecase.DoubleRatchetSendStepUseCase
import kotlin.reflect.KClass

class ChatViewModel(
    private val chatRepository: ChatRepository = ChatRepository(),
    private val websocketRepository: WebsocketRepository = WebsocketRepository,
    private val doubleRatchetSendStepUseCase: DoubleRatchetSendStepUseCase = DoubleRatchetSendStepUseCase(),
    private val saveOfflineToDBMessageUseCase: SaveOfflineToDBMessageUseCase = SaveOfflineToDBMessageUseCase(),
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
                actionReceiverFetchMessages(intent.targetId, intent.conversationId)
                actionSenderSyncMessageReadStatus(intent.targetId, intent.conversationId)
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

    private fun actionSenderSyncMessageReadStatus(targetId: Long, conversationId: Long) {
        io {
            chatRepository.senderSyncMessageReadStatusHttp(
                userId = IMGlobalStorage.getCurrentUserId(),
                targetId = targetId,
                conversationId = conversationId,
                conversationType = 1,
                since = "",
                until = "",
                onSuccess = {
                    logD("syncMessageReadStatus success")
                },
                onError = {
                    logE("syncMessageReadStatus error: $it")
                }
            )
        }
    }

    private fun actionReceiverFetchMessages(
        targetId: Long,
        conversationId: Long,
        page: Int = 1,
        size: Int = 50
    ) {
        // http拉取最新消息
        chatRepository.receiverPullOfflineMessageHttp(
            userId = IMGlobalStorage.getCurrentUserId(),
            targetId = targetId,
            conversationId = conversationId,
            conversationType = 1,
            page = page,
            size = size,
            onSuccess = {
                io {
                    // 保存到数据库
                    saveOfflineToDBMessageUseCase.invoke(mapOf(), it.content.toEntity())
                    // 发送已读状态
                    receiverPushMessageReadStatusHttp(it)
                    // 刷新UI
                    main { dispatch(FetchIntent.RefreshByFetchData(it)) }
                    // 获取下一页
                    val hasNextPage = it.last.not()
                    if (hasNextPage) {
                        logV("拉取离线消息成功:存在下一页,继续拉取")
                        actionReceiverFetchMessages(targetId, conversationId, it.number + 1, it.size)
                    } else {
                        logV("拉取离线消息成功:不存在下一页")
                    }
                }
            },
            onError = {
                logE("拉取离线消息错误:${it::class.simpleName} ${it.message}")
                main { dispatch(it.toTipsIntent()) }
            }
        )
    }

    private fun receiverPushMessageReadStatusHttp(page: PrivateMessageResponsePage) {
        // http发送消息已读
        if (page.content.isEmpty()) {
            logW("没有消息")
            return
        }
        // 只有对方发的消息才需要发送已读
        val receiveList = page.content.filter { it.isSelfSent().not() }
        if (receiveList.isEmpty()) {
            logW("没有对方发的消息")
            return
        }
        logV("需要发送消息已读状态(已读)的消息集合: ${receiveList.map { it.msgId }}")
        val first = receiveList.first()
        chatRepository.receiverPushMessageReadStatusHttp(
            userId = IMGlobalStorage.getCurrentUserId(),
            fromUserName = IMGlobalStorage.getCurrentUser().username,
            targetId = first.senderId,
            targetName = first.senderName,
            msgIds = receiveList.map { it.msgId },
            onSuccess = {
                logD("http发送消息已读状态(已读) 成功")
            },
            onError = {
                logE("http发送消息已读状态(已读) 错误: ${it.message}")
                main { dispatch(it.toTipsIntent()) }
            }
        )
    }

    private fun actionSendMessage(message: PrivateMessageResponse) {
        io {
            if (message.content.isEmpty() or message.content.isBlank()) {
                logE("发送消息内容为空")
                val error = ErrorResponse(
                    status = 400,
                    message = "发送消息内容为空"
                )
                main { dispatch(error.toTipsIntent()) }
                return@io
            }
            // websocket发送消息 添加 DH棘轮 公钥
            val localUserId = IMGlobalStorage.getCurrentUserId()
            val remoteUserId = message.receiverId
            val messageKeyLocal = doubleRatchetSendStepUseCase.invoke(
                mapOf(
                    "localUserId" to localUserId.toString(),
                    "remoteUserId" to remoteUserId.toString()
                ), Unit
            ).onFailure {
                logE("发送时计算messageKey错误: ${it.message}")
                it.printStackTrace()
                return@io
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
            websocketRepository.messageFlow.collect { message ->
                logE("接收到消息: $message")
                dispatch(ChatIntent.RefreshByReceiveMessage(message.toPrivateMessageResponse()))
            }
        }
    }

    override fun onCleared() {
        logE("销毁：onCleared")
        websocketRepository.disconnect()
        super.onCleared()
    }
}