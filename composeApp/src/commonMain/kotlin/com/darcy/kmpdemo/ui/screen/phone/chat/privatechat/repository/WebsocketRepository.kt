package com.darcy.kmpdemo.ui.screen.phone.chat.privatechat.repository

import com.darcy.kmpdemo.bean.websocket.stomp.STOMPMessage
import com.darcy.kmpdemo.log.logD
import com.darcy.kmpdemo.log.logE
import com.darcy.kmpdemo.log.logI
import com.darcy.kmpdemo.log.logW
import com.darcy.kmpdemo.network.http.urls.WebSockets.WEBSOCKET_URL
import com.darcy.kmpdemo.network.http.parser.impl.kotlinxJson
import com.darcy.kmpdemo.network.websocket.WebSocketManager
import com.darcy.kmpdemo.network.websocket.listener.IOuterListener
import com.darcy.kmpdemo.repository.IRepository
import com.darcy.kmpdemo.storage.memory.IMGlobalStorage
import com.darcy.kmpdemo.ui.screen.phone.chat.privatechat.state.WebSocketConnectionState
import com.darcy.kmpdemo.x3dh.MessageKey
import com.darcy.kmpdemo.x3dh.usecase.DoubleRatchetReceiveStepUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlin.concurrent.Volatile

/**
 * websocket STOMP协议聊天
 */
object WebsocketRepository : IRepository {
    private const val TAG = "WebsocketRepository"
    private val webSocketManager: WebSocketManager = WebSocketManager
    private val imGlobalStorage: IMGlobalStorage = IMGlobalStorage
    private val doubleRatchetReceiveStepUseCase = DoubleRatchetReceiveStepUseCase()

    private val _messageFlow = MutableSharedFlow<STOMPMessage>(replay = 0)
    val messageFlow: SharedFlow<STOMPMessage> = _messageFlow.asSharedFlow()
    private val _connectionStateFlow = MutableSharedFlow<WebSocketConnectionState>(replay = 1)
    val connectionStateFlow: SharedFlow<WebSocketConnectionState> =
        _connectionStateFlow.asSharedFlow()

    @Volatile
    private var isConnected = false

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
            if (isConnected) {
                logW("$TAG already connected")
                return@launch
            }
            runCatching {
                init()
                setupListener()
                webSocketManager.connect()
            }.onFailure {
                logE("$TAG Connection failed: ${it.message}")
                it.printStackTrace()
                isConnected = false
            }
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
                //logI("$TAG onOpen")
                isConnected = true
                scope.launch {
                    _connectionStateFlow.emit(WebSocketConnectionState.Connected)
                }
            }

            override fun onSend(message: String) {
                //logD("WebsocketRepository onSend:$message")
            }

            override fun onSend(bytes: ByteArray) {
                TODO("Not yet implemented")
            }

            override fun onMessage(body: String, headers: Map<String, String>) {
                //logV("$TAG onMessage:$headers $body")
                handleReceiveMessage(body, headers)
            }

            override fun onMessage(bytes: ByteArray, headers: Map<String, String>) {
                TODO("Not yet implemented")
            }

            override fun onFailure(errorMessage: String) {
                //logE("$TAG onFailure:$errorMessage")
                scope.launch {
                    _connectionStateFlow.emit(WebSocketConnectionState.Error(errorMessage))
                }
                //disconnect()
            }

            override fun onClosed() {
                //logW("$TAG onClosed")
                isConnected = false
                scope.launch {
                    _connectionStateFlow.emit(WebSocketConnectionState.Disconnected)
                }
            }
        })
    }

    fun disconnect() {
        scope.launch {
            //logD("$TAG disconnect")
            if (!isConnected) {
                logW("$TAG already disconnected")
                return@launch
            }
            webSocketManager.disconnect()
            isConnected = false
        }
    }

    fun sendMessage(message: STOMPMessage, headers: Map<String, String>) {
        scope.launch {
            logD("$TAG sendMessage toUser:${message.receiverName}")
            if (!isConnected) {
                logE("$TAG Cannot send message: not connected")
                return@launch
            }
            webSocketManager.send(message, headers)
        }
    }

    fun isConnected(): Boolean {
        return isConnected
    }

    /**
     * 收到消息
     */
    private fun handleReceiveMessage(message: String, headers: Map<String, String>) {
        runCatching {
            scope.launch(Dispatchers.Default) {
                logD("$TAG handleMessage fromUser:${headers["fromUser"]}")
                val localUserId = imGlobalStorage.getCurrentUserId()
                val messageKey = MessageKey.fromMap(headers)
                val messageKeyLocal = doubleRatchetReceiveStepUseCase.invoke(
                    mapOf(
                        "localUserId" to localUserId.toString(),
                        "remoteUserId" to messageKey.fromUserId.toString(),
                        "remoteDHKey" to messageKey.dhPublicKey,
                    )
                ).onFailure {
                    logE("$TAG handleReceiveMessage failed: ${it.message}")
                    it.printStackTrace()
                    return@launch
                }.getOrElse { MessageKey() }
                logD("$TAG handleReceiveMessage messageKeyLocal=$messageKeyLocal")
                val messageEntity = kotlinxJson.decodeFromString<STOMPMessage>(message)
                _messageFlow.emit(messageEntity)
            }
        }.onFailure {
            logE("$TAG handle message failed: ${it.message}")
            it.printStackTrace()
        }
    }
}