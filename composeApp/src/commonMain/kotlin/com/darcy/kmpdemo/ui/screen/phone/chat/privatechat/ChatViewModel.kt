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
import com.darcy.kmpdemo.log.logI
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
import com.darcy.kmpdemo.ui.screen.phone.chat.usecase.QueryMessageFromDBByPageUseCase
import com.darcy.kmpdemo.ui.screen.phone.chat.usecase.SaveMessageToDBUseCase
import com.darcy.kmpdemo.utils.JsonHelper
import com.darcy.kmpdemo.utils.UUIDHelper
import com.darcy.kmpdemo.x3dh.MessageKey
import com.darcy.kmpdemo.x3dh.usecase.SendDoubleRatchetStepUseCase
import com.darcy.kmpdemo.x3dh.usecase.MarkMessageReadStatusUseCase
import com.darcy.kmpdemo.x3dh.usecase.ReceiveDoubleRatchetStepUseCase
import kotlin.reflect.KClass

class ChatViewModel(
    private val chatRepository: ChatRepository = ChatRepository(),
    private val websocketRepository: WebsocketRepository = WebsocketRepository,
    private val sendDoubleRatchetStepUseCase: SendDoubleRatchetStepUseCase = SendDoubleRatchetStepUseCase(),
    private val saveMessageToDBUseCase: SaveMessageToDBUseCase = SaveMessageToDBUseCase(),
    private val queryMessageFromDBByPageUseCase: QueryMessageFromDBByPageUseCase = QueryMessageFromDBByPageUseCase(),
    private val markMessageReadStatusUseCase: MarkMessageReadStatusUseCase = MarkMessageReadStatusUseCase(),
    private val receiveDoubleRatchetStepUseCase: ReceiveDoubleRatchetStepUseCase = ReceiveDoubleRatchetStepUseCase(),
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
                val targetId = intent.params["targetId"]?.toLongOrNull() ?: 0
                val conversationId = intent.params["conversationId"]?.toLongOrNull() ?: 0
                actionReceiverPullOfflineMessages(targetId, conversationId)
            }

            is ChatIntent.ActionSendMessage -> {
                actionSendMessage(intent.message.copy(msgId = UUIDHelper.generateMessageId()))
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

    private fun actionLoadMessageByPage(
        targetId: Long,
        conversationId: Long,
        page: Int,
        size: Int = 10
    ) {
        io {
            val userId = IMGlobalStorage.getCurrentUserId()
            // 从数据库 分页读取数据 刷新UI
            val messageList = queryMessageFromDBByPageUseCase.invoke(
                mapOf(
                    "userId" to userId.toString(),
                    "targetId" to targetId.toString(),
                    "conversationId" to conversationId.toString(),
                    "page" to page.toString(),
                    "size" to size.toString()
                ), Unit
            ).getOrElse { emptyList() }
            logV("loadMessageByPage messageList: $messageList")
            main { dispatch(FetchIntent.RefreshByFetchData(messageList)) }
        }
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
            val messageKeyLocal = sendDoubleRatchetStepUseCase.invoke(
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
            // 保存到数据库
            saveMessageToDBUseCase.invoke(mapOf(), listOf(message.toEntity()))
                .onSuccess {
                    logI("$TAG 发送前 保存到数据库成功: ${message.msgId}")
                }.onFailure {
                    logE("$TAG 发送前 保存到数据库错误: ${message.msgId} ${it::class.simpleName} ${it.message}")
                }
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
        io {
            websocketRepository.messageReadStatusFlow.collect { response ->
                logE("接收到已读消息: ${response.msgIds}")
                dispatch(ChatIntent.RefreshByReceiveMessageReadStatus(response))
            }

        }
    }

    private fun actionReceiverPullOfflineMessages(
        targetId: Long,
        conversationId: Long,
        page: Int = 1,
        size: Int = 50
    ) {
        val userId = IMGlobalStorage.getCurrentUserId()
        // http拉取最新消息
        chatRepository.receiverPullOfflineMessageHttp(
            userId = userId,
            targetId = targetId,
            conversationId = conversationId,
            conversationType = 1,
            page = page,
            size = size,
            onSuccess = { response ->
                io {
                    // http收到消息的处理
                    response.content.forEach { item ->
                        logV("$TAG http:接收到消息: ${item.msgId}")
                        val messageKeyLocal = receiveDoubleRatchetStepUseCase.invoke(
                            mapOf(
                                "localUserId" to userId.toString(),
                                "remoteUserId" to targetId.toString(),
                                "remoteDHKey" to item.dhPublicKey,
                                "msgId" to item.msgId,
                                "N_KEY" to item.nKey.toString(),
                                "PN_KEY" to item.pnKey.toString(),
                            ), Unit
                        ).onFailure {
                            logE("$TAG 接收时计算messageKey错误: ${it.message}")
                            it.printStackTrace()
                            return@io
                        }.getOrElse { MessageKey() }
                        logD("$TAG pullOfflineMessages messageKeyLocal=$messageKeyLocal")
                    }
                    // 保存到数据库
                    val msgIds = response.content.map { it.msgId }
                    saveMessageToDBUseCase.invoke(mapOf(), response.content.toEntity())
                        .onSuccess {
                            logI("$TAG 离线消息保存到数据库成功: $msgIds")
                        }.onFailure {
                            logE("$TAG 离线消息保存到数据库错误: $msgIds ${it::class.simpleName} ${it.message}")
                        }
                    // 发送已读状态
                    receiverPushMessageReadStatusHttp(response)
                    // 获取下一页
                    val hasNextPage = response.last.not()
                    if (hasNextPage) {
                        logV("拉取离线消息成功:存在下一页,继续拉取")
                        actionReceiverPullOfflineMessages(
                            targetId,
                            conversationId,
                            response.number + 1,
                            response.size
                        )
                    } else {
                        logI("拉取离线消息成功:已拉取全部消息")
                        // 同步离线已读状态
                        actionSenderSyncMessageReadStatus(targetId, conversationId)
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

    private fun actionSenderSyncMessageReadStatus(targetId: Long, conversationId: Long) {
        chatRepository.senderSyncMessageReadStatusHttp(
            userId = IMGlobalStorage.getCurrentUserId(),
            targetId = targetId,
            conversationId = conversationId,
            conversationType = 1,
            since = "",
            until = "",
            onSuccess = { response ->
                logD("syncMessageReadStatus success")
                io {
                    logW("$TAG http:更新数据库已读状态(已读) 用于双棘轮")
                    markMessageReadStatusUseCase.invoke(
                        mapOf("messageReadStatusResponse" to JsonHelper.toJson(response)), Unit
                    ).onSuccess {
                        logI("$TAG http:更新数据库已读状态(已读) 成功:${response.msgIds}")
                    }.onFailure {
                        logE("$TAG http:更新数据库已读状态(已读) 失败:${response.msgIds} ${it.message}")
                    }
                    // 刷新UI
                    actionLoadMessageByPage(targetId, conversationId, 1)
                }
            },
            onError = {
                logE("syncMessageReadStatus error: $it")
            }
        )
    }

    override fun onCleared() {
        logE("销毁：onCleared")
        websocketRepository.disconnect()
        super.onCleared()
    }
}