package com.darcy.kmpdemo.network.websocket.impl

import com.darcy.kmpdemo.bean.websocket.stomp.STOMPMessage
import com.darcy.kmpdemo.log.logE
import com.darcy.kmpdemo.network.http.impl.ktor.ktorClient
import com.darcy.kmpdemo.network.parser.impl.kotlinxJson
import com.darcy.kmpdemo.network.websocket.IWebSocketClient
import com.darcy.kmpdemo.network.websocket.listener.IOuterListener
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.stomp.StompSession
import org.hildan.krossbow.stomp.config.HeartBeat
import org.hildan.krossbow.stomp.config.HeartBeatTolerance
import org.hildan.krossbow.stomp.frame.StompFrame
import org.hildan.krossbow.stomp.instrumentation.KrossbowInstrumentation
import org.hildan.krossbow.stomp.sendText
import org.hildan.krossbow.stomp.subscribeText
import org.hildan.krossbow.websocket.WebSocketClient
import org.hildan.krossbow.websocket.WebSocketFrame
import org.hildan.krossbow.websocket.ktor.KtorWebSocketClient
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class KrossbowWebsocketClientImpl : IWebSocketClient, IOuterListener {
    companion object {
        private val TAG = KrossbowWebsocketClientImpl::class.simpleName
        private val SEND_PRIVATE = "/app/sendPrivateMessage"
        private val SEND_TARGET_GROUP = "/app/sendTargetGroupMessage"
        private val SEND_TOPIC = "/app/sendGroupAllMessage"
        private val RECEIVE_PRIVATE = "/user/queue/message"
        private val RECEIVE_TARGET_GROUP = "/topic/group/" // + groupId
    }

    private var url: String = ""
    private var fromUser: String = ""
    private var outListener: IOuterListener? = null
    private var session: StompSession? = null
    private var privateSubscriptionJob: Job? = null
    private var topicSubscriptionJob: Job? = null

    @OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class)
    private val dispatcher: CoroutineDispatcher = newSingleThreadContext("websocket-stomp")
    private val exceptionHandler: CoroutineExceptionHandler =
        CoroutineExceptionHandler { _, throwable ->
            logE("$TAG exceptionHandler: ${throwable.message}")
            throwable.printStackTrace()
        }
    private val scope = CoroutineScope(dispatcher + SupervisorJob() + exceptionHandler)
    private lateinit var stompClient: StompClient
    private lateinit var webSocketClient: WebSocketClient

    override fun init(url: String, fromUser: String) {
        this.url = url
        this.fromUser = fromUser
        webSocketClient = KtorWebSocketClient(ktorClient)
        // val kcrossbowWebsocketClient: WebSocketClient = WebSocketClient.builtIn()
        stompClient = StompClient(webSocketClient) {
            connectionTimeout = 30.toDuration(DurationUnit.SECONDS)
            disconnectTimeout = 30.toDuration(DurationUnit.SECONDS)
            subscriptionCompletionTimeout = 30.toDuration(DurationUnit.SECONDS)
            receiptTimeout = 10.toDuration(DurationUnit.SECONDS) // 确认帧超时
            autoReceipt = true  // 自动开启确认帧
            gracefulDisconnect = true
            connectWithStompCommand = true // 使用 STOMP CONNECT 命令进行连接握手
            heartBeat = HeartBeat(
                minSendPeriod = 10.toDuration(DurationUnit.SECONDS),
                expectedPeriod = 10.toDuration(DurationUnit.SECONDS)
            )
            heartBeatTolerance = HeartBeatTolerance()
            defaultSessionCoroutineContext = dispatcher + SupervisorJob() + exceptionHandler
            // 监听器
            instrumentation = object :KrossbowInstrumentation{
                override suspend fun onStompFrameSent(frame: StompFrame) {
                    super.onStompFrameSent(frame)
                    println("$TAG onStompFrameSent --> ${frame.toString()}")
                }

                override suspend fun onFrameDecoded(
                    originalFrame: WebSocketFrame,
                    decodedFrame: StompFrame
                ) {
                    super.onFrameDecoded(originalFrame, decodedFrame)
                    //println("$TAG originalFrame --> ${originalFrame.toString()}")
                    println("$TAG onFrameDecoded --> ${decodedFrame.toString()}")
                }

                override suspend fun onWebSocketClientError(exception: Throwable) {
                    super.onWebSocketClientError(exception)
                    println("$TAG onWebSocketClientError --> ${exception.message}")
                    onFailure(exception.message ?: "")
                    exception.printStackTrace()
                }

                override suspend fun onWebSocketClosed(cause: Throwable?) {
                    super.onWebSocketClosed(cause)
                    println("$TAG onWebSocketClosed --> ${cause?.message}")
                }

                override suspend fun onWebSocketFrameReceived(frame: WebSocketFrame) {
                    super.onWebSocketFrameReceived(frame)
                    println("$TAG onWebSocketFrameReceived <-- ${frame.toString()}")
                }

            }
        }
    }

    override suspend fun connect() {
        if (outListener == null) {
            throw NullPointerException("outListener is null. please call setOutListener() first.")
        }
        if (session != null) {
            println("$TAG already connected!")
            return
        }
        runCatching {
            println("$TAG connect...")
            session = stompClient.connect(
                this.url,
                customStompConnectHeaders = mapOf(
                    "Authorization" to fromUser,
//                    "accept-version" to "1.2,1.1,1.0"
                )
            )
            onOpen()
            session?.let {
                // 启动私有消息订阅
                privateSubscriptionJob = scope.launch {
                    it.subscribeText(RECEIVE_PRIVATE).collect { message ->
                        println("$TAG onReceive message <-- $message")
                        onMessage(message)
                    }

                }
                // 启动主题消息订阅
                topicSubscriptionJob = scope.launch {
                    it.subscribeText(SEND_TOPIC).collect { message ->
                        println("$TAG onReceive topic message <-- $message")
                        onMessage(message)
                    }
                }
            } ?: run {
                println("$TAG session is null.")
                onFailure("session is null")
            }
        }.onFailure {
            println("$TAG connect error: ${it::class.simpleName} ${it.message}")
            it.printStackTrace()
            onFailure(it.message ?: "")
            // 确保在连接失败时清理资源
            disconnect()
        }
    }

    override suspend fun disconnect() {
        println("$TAG disconnect...")
        // 先取消订阅任务
        cleanupSubscriptions()
        session?.let {
            it.disconnect()
            onClosed()
            session = null
        } ?: run {
            println("$TAG already disconnected!")
            onFailure("already disconnected!")
        }
    }

    /**
     * 清理订阅相关的协程任务
     */
    private fun cleanupSubscriptions() {
        privateSubscriptionJob?.cancel()
        topicSubscriptionJob?.cancel()
        privateSubscriptionJob = null
        topicSubscriptionJob = null
    }

    override suspend fun send(message: STOMPMessage) {
        val jsonMessage = kotlinxJson.encodeToString(message)
        println("$TAG send message... --> $jsonMessage")
        session?.let {
            runCatching {
                val receipt = it.sendText(SEND_PRIVATE, jsonMessage)
                println("$TAG 收到receipt: $receipt")
                onSend(jsonMessage)
            }.onFailure { exception ->
                onFailure("send error: ${exception::class.simpleName} ${exception.message}")
            }
        } ?: run {
            println("$TAG session is null.")
            onFailure("session is null")
        }
    }

    override suspend fun send(bytes: ByteArray) {
        TODO("Not yet implemented")
    }

    override suspend fun reconnect() {
        disconnect()
        delay(1_000)
        connect()
    }

    /**
     * 设置外部监听器
     */
    override fun setOuterListener(listener: IOuterListener) {
        this.outListener = listener
    }

    override fun onOpen() {
        logE("$TAG onOpen")
        outListener?.onOpen()
    }

    override fun onSend(message: String) {
        println("$TAG onSend... $message")
        outListener?.onSend(message)
    }

    override fun onSend(bytes: ByteArray) {
        logE("$TAG onSend2... $bytes")
        TODO("Not yet implemented")
    }

    override fun onMessage(message: String) {
        println("$TAG onMessage... $message")
        outListener?.onMessage(message)
    }

    override fun onMessage(bytes: ByteArray) {
        logE("$TAG onMessage2... $bytes")
        TODO("Not yet implemented")
    }

    override fun onFailure(errorMessage: String) {
        logE("$TAG onFailure: $errorMessage")
        outListener?.onFailure("Error: $errorMessage.")
    }

    override fun onClosed() {
        logE("$TAG onClosed")
        outListener?.onClosed()
    }
}