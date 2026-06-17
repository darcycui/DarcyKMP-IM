package com.darcy.kmpdemo.ui.screen.phone.chat.privatechat.repository

import com.darcy.kmpdemo.bean.http.request.MessageReadStatusRequest
import com.darcy.kmpdemo.bean.http.response.MessageReadStatusResponse
import com.darcy.kmpdemo.bean.http.response.PrivateMessageResponse
import com.darcy.kmpdemo.bean.http.response.toEntity
import com.darcy.kmpdemo.bean.websocket.stomp.STOMPMessage
import com.darcy.kmpdemo.bean.websocket.stomp.toPrivateMessageResponse
import com.darcy.kmpdemo.log.logD
import com.darcy.kmpdemo.log.logE
import com.darcy.kmpdemo.log.logI
import com.darcy.kmpdemo.log.logW
import com.darcy.kmpdemo.network.http.urls.WebSockets.WEBSOCKET_URL
import com.darcy.kmpdemo.network.websocket.WebSocketManager
import com.darcy.kmpdemo.network.websocket.impl.krossbow.KrossbowWebsocketClientImpl.Companion.SEND_MESSAGE_READ_STATUS
import com.darcy.kmpdemo.network.websocket.impl.krossbow.KrossbowWebsocketClientImpl.Companion.SEND_PRIVATE
import com.darcy.kmpdemo.network.websocket.listener.IOuterListener
import com.darcy.kmpdemo.repository.IRepository
import com.darcy.kmpdemo.storage.memory.IMGlobalStorage
import com.darcy.kmpdemo.ui.screen.phone.chat.privatechat.state.WebSocketConnectionState
import com.darcy.kmpdemo.ui.screen.phone.chat.usecase.SaveMessageToDBUseCase
import com.darcy.kmpdemo.utils.JsonHelper
import com.darcy.kmpdemo.x3dh.MessageKey
import com.darcy.kmpdemo.x3dh.usecase.CreateMessageReadStatusUseCase
import com.darcy.kmpdemo.x3dh.usecase.ReceiveDoubleRatchetStepUseCase
import com.darcy.kmpdemo.x3dh.usecase.MarkMessageReadStatusUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext

/**
 * websocket STOMP协议聊天
 */
object WebsocketRepository : IRepository {
    private const val TAG = "WebsocketRepository"
    private val webSocketManager: WebSocketManager = WebSocketManager
    private val imGlobalStorage: IMGlobalStorage = IMGlobalStorage
    private val receiveDoubleRatchetStepUseCase = ReceiveDoubleRatchetStepUseCase()
    private val createMessageReadStatusUseCase = CreateMessageReadStatusUseCase()
    private val markMessageReadStatusUseCase = MarkMessageReadStatusUseCase()
    private val saveMessageToDBUseCase: SaveMessageToDBUseCase = SaveMessageToDBUseCase()

    private val _messageFlow = MutableSharedFlow<PrivateMessageResponse>(replay = 0)
    val messageFlow: SharedFlow<PrivateMessageResponse> = _messageFlow.asSharedFlow()

    private val _messageReadStatusFlow = MutableSharedFlow<MessageReadStatusResponse>(replay = 1)
    val messageReadStatusFlow: SharedFlow<MessageReadStatusResponse> =
        _messageReadStatusFlow.asSharedFlow()
    private val _connectionStateFlow =
        MutableStateFlow<WebSocketConnectionState>(WebSocketConnectionState.Disconnected)
    val connectionStateFlow: SharedFlow<WebSocketConnectionState> =
        _connectionStateFlow.asStateFlow()

    @OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class)
    private val dispatcher: CoroutineDispatcher = newSingleThreadContext("websocketRepository")
    private val exceptionHandler: CoroutineExceptionHandler =
        CoroutineExceptionHandler { _, throwable ->
            logE("$TAG exceptionHandler: ${throwable.message}")
            throwable.printStackTrace()
        }
    private val scope = CoroutineScope(dispatcher + SupervisorJob() + exceptionHandler)


    fun connect() {
        scope.launch {
            logD("$TAG connect")
            init()
            setupListener()
            webSocketManager.connect()
        }
    }

    private fun init() {
        logD("$TAG init")
        webSocketManager.init(
            url = WEBSOCKET_URL,
            userToken = imGlobalStorage.getCurrentUser().token,
//            userToken = "",
        )
    }

    private fun setupListener() {
        logD("$TAG setupListener")
        webSocketManager.setOuterListener(object : IOuterListener {
            override fun onOpen() {
                scope.launch {
                    _connectionStateFlow.emit(WebSocketConnectionState.Connected)
                }
            }

            override fun onSend(message: String) {
            }

            override fun onSend(bytes: ByteArray) {
                TODO("Not yet implemented")
            }

            override fun onMessage(body: String, headers: Map<String, String>) {
                handleReceiveMessage(body, headers)
            }

            override fun onMessage(bytes: ByteArray, headers: Map<String, String>) {
                TODO("Not yet implemented")
            }

            override fun onMessageReadStatus(
                body: String,
                headers: Map<String, String>
            ) {
                handleMessageReadStatus(body, headers)
            }

            override fun onFailure(errorMessage: String) {
                scope.launch {
                    _connectionStateFlow.emit(WebSocketConnectionState.Error(errorMessage))
                }
                //disconnect()
            }

            override fun onClosed() {
                scope.launch {
                    _connectionStateFlow.emit(WebSocketConnectionState.Disconnected)
                }
            }
        })
    }

    fun disconnect() {
        scope.launch {
            webSocketManager.disconnect()
        }
    }

    fun sendMessage(message: STOMPMessage, headers: Map<String, String>) {
        scope.launch {
            logD("$TAG sendMessage toUser:${message.receiverName}")
            val messageJson = JsonHelper.toJson(message)
            webSocketManager.sendText(
                messageJson,
                SEND_PRIVATE,
                headers
            )
            logW("$TAG sendMessage:创建数据库已读状态(未读)")
            createMessageReadStatusUseCase.invoke(
                mapOf("stompMessage" to messageJson), Unit
            ).onSuccess {
                logI("$TAG 创建数据库已读状态(未读) 创建成功:${message.msgId}")
            }.onFailure {
                logE("$TAG 创建数据库已读状态(未读) 创建失败:${message.msgId} ${it.message}")
                it.printStackTrace()
            }.getOrElse { }
        }
    }

    /**
     * 收到消息
     */
    private fun handleReceiveMessage(message: String, headers: Map<String, String>) {
        scope.launch(Dispatchers.Default) {
            runCatching {
                logD("$TAG handleMessage fromUser:$headers")
                val localUserId = imGlobalStorage.getCurrentUserId()
                val messageKey = MessageKey.fromMap(headers)
                val messageEntity = JsonHelper.fromJson<STOMPMessage>(message)
                messageEntity?.let { entity ->
                    val messageKeyLocal = receiveDoubleRatchetStepUseCase.invoke(
                        mapOf(
                            "localUserId" to localUserId.toString(),
                            "remoteUserId" to messageKey.fromUserId.toString(),
                            "remoteDHKey" to messageKey.dhPublicKey,
                            "N_KEY" to messageKey.nKey.toString(),
                            "PN_KEY" to messageKey.pnKey.toString(),
                            "msgId" to entity.msgId,
                        ), Unit
                    ).onFailure {
                        logE("$TAG 接收时计算messageKey错误: ${it.message}")
                        it.printStackTrace()
                        return@launch
                    }.getOrElse { MessageKey() }
                    logD("$TAG 接收消息 messageKeyLocal=$messageKeyLocal")
                    // 保存到数据库
                    val response = messageEntity.toPrivateMessageResponse(messageKeyLocal)
                    saveMessageToDBUseCase.invoke(mapOf(), listOf(response.toEntity()))
                        .onSuccess {
                            logI("$TAG 接收后 保存到数据库成功: ${response.msgId}")
                        }.onFailure { e ->
                            logE("$TAG 接收后 保存到数据库错误: ${response.msgId} ${e::class.simpleName} ${e.message}")
                        }
                    _messageFlow.emit(response)
                    // 发送已读状态
                    sendMessageReadStatus(entity, localUserId)
                } ?: run {
                    logE("$TAG 接收消息 messageEntity is null after json parse")
                }
            }.onFailure {
                logE("$TAG 接收消息错误: ${it.message}")
                it.printStackTrace()
            }
        }
    }

    /**
     * 收到消息后 标记消息已读
     */
    fun sendMessageReadStatus(
        messageEntity: STOMPMessage,
        localUserId: Long,
    ) {
        scope.launch {
            logD("$TAG 发送已读状态 sendMessageReadStatus")
            val messageReadStatusRequest = MessageReadStatusRequest(
                userId = messageEntity.receiverId,
                fromUserName = messageEntity.receiverName,
                targetId = messageEntity.senderId,
                targetName = messageEntity.senderName,
                msgIds = listOf(messageEntity.msgId),
                conversationType = 1,
                clientType = "",
                deviceId = ""
            )
            val headers = mapOf(
                "fromUserId" to localUserId.toString(),
                "toUserId" to messageEntity.senderId.toString(),
                "url" to "WS:/private"
            )
            logI("$TAG 发送已读状态 headers:$headers")
            webSocketManager.sendText(
                JsonHelper.toJson(messageReadStatusRequest),
                SEND_MESSAGE_READ_STATUS,
                mapOf(
                    "fromUserId" to localUserId.toString(),
                    "toUserId" to messageEntity.senderId.toString(),
                    "url" to "WS:/private"
                )
            )
        }
    }

    /**
     * 收到已读状态
     */
    private fun handleMessageReadStatus(body: String, headers: Map<String, String>) {
        val messageReadStatusResponse = JsonHelper.fromJson<MessageReadStatusResponse>(body)
        if (messageReadStatusResponse == null || messageReadStatusResponse.msgIds.isEmpty()) {
            logE("$TAG onMessageReadStatus:messageReadStatusResponseList is null or empty")
            return
        }
        val msgIds = messageReadStatusResponse.msgIds
        scope.launch {
            logW("$TAG onMessageReadStatus:更新数据库已读状态(已读)")
            markMessageReadStatusUseCase.invoke(
                mapOf("messageReadStatusResponse" to body), Unit
            ).onSuccess {
                logI("$TAG onMessageReadStatus:更新数据库已读状态(已读) 成功:${msgIds}")
                _messageReadStatusFlow.emit(messageReadStatusResponse)
            }.onFailure {
                logE("$TAG onMessageReadStatus:更新数据库已读状态(已读) 失败:${msgIds} ${it.message}")
            }
        }
    }
}