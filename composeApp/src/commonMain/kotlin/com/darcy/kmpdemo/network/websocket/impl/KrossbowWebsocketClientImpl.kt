package com.darcy.kmpdemo.network.websocket.impl

import com.darcy.kmpdemo.log.logD
import com.darcy.kmpdemo.log.logE
import com.darcy.kmpdemo.log.logI
import com.darcy.kmpdemo.network.http.impl.ktor.ktorClient
import com.darcy.kmpdemo.network.websocket.IWebSocketClient
import com.darcy.kmpdemo.network.websocket.frame.toJsonString
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
import org.hildan.krossbow.stomp.WebSocketClosedUnexpectedly
import org.hildan.krossbow.stomp.config.HeartBeat
import org.hildan.krossbow.stomp.config.HeartBeatTolerance
import org.hildan.krossbow.stomp.frame.FrameBody
import org.hildan.krossbow.stomp.frame.StompFrame
import org.hildan.krossbow.stomp.headers.ExperimentalHeadersApi
import org.hildan.krossbow.stomp.headers.StompSendHeaders
import org.hildan.krossbow.stomp.instrumentation.KrossbowInstrumentation
import org.hildan.krossbow.stomp.subscribe
import org.hildan.krossbow.websocket.WebSocketClient
import org.hildan.krossbow.websocket.WebSocketFrame
import org.hildan.krossbow.websocket.ktor.KtorWebSocketClient
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class KrossbowWebsocketClientImpl : IWebSocketClient, IOuterListener {
    companion object {
        private val TAG = KrossbowWebsocketClientImpl::class.simpleName
        const val SEND_PRIVATE = "/app/sendPrivateMessage"
        const val SEND_TARGET_GROUP = "/app/sendTargetGroupMessage"
        const val SEND_TOPIC = "/app/sendGroupAllMessage"
        const val SEND_MESSAGE_READ_STATUS = "/app/markMessageRead"

        private const val RECEIVE_PRIVATE = "/user/queue/message"
        private const val RECEIVE_TARGET_GROUP = "/topic/group/" // + groupId
        private const val RECEIVE_TOPIC = "/topic/message"
        private const val RECEIVE_MESSAGE_READ_STATUS = "/user/queue/message/read"
    }

    private var url: String = ""
    private var fromUser: String = ""
    private var outListener: IOuterListener? = null
    private var session: StompSession? = null
    private var privateSubscriptionJob: Job? = null
    private var topicSubscriptionJob: Job? = null
    private var statusSubscriptionJob: Job? = null

    @OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class)
    private val dispatcher: CoroutineDispatcher = newSingleThreadContext("websocket-stomp")
    private val exceptionHandler: CoroutineExceptionHandler =
        CoroutineExceptionHandler { _, throwable ->
            logE("$TAG exceptionHandler: ${throwable.message}")
            throwable.printStackTrace()
            dispatchException(throwable)
        }

    fun dispatchException(throwable: Throwable) {
        outListener?.onFailure(throwable.message ?: "")
        when (throwable) {
            // 连接已断开 手动调用 disconnect
            is WebSocketClosedUnexpectedly -> {
                scope.launch {
                    disconnect()
                }
            }

            else -> {}
        }
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
            instrumentation = object : KrossbowInstrumentation {
                override suspend fun onStompFrameSent(frame: StompFrame) {
                    super.onStompFrameSent(frame)
                    logD("$TAG onStompFrameSent --> ${frame.toString()}")
                }

                override suspend fun onFrameDecoded(
                    originalFrame: WebSocketFrame,
                    decodedFrame: StompFrame
                ) {
                    super.onFrameDecoded(originalFrame, decodedFrame)
                    //logD("$TAG originalFrame --> ${originalFrame.toString()}")
                    logD("$TAG onFrameDecoded --> ${decodedFrame.toString()}")
                }

                override suspend fun onWebSocketClientError(exception: Throwable) {
                    super.onWebSocketClientError(exception)
                    logD("$TAG onWebSocketClientError --> ${exception.message}")
                    onFailure(exception.message ?: "")
                    exception.printStackTrace()
                }

                override suspend fun onWebSocketClosed(cause: Throwable?) {
                    super.onWebSocketClosed(cause)
                    logD("$TAG onWebSocketClosed --> ${cause?.message}")
                }

                override suspend fun onWebSocketFrameReceived(frame: WebSocketFrame) {
                    super.onWebSocketFrameReceived(frame)
                    //logD("$TAG onWebSocketFrameReceived <-- ${frame.toString()}")
                }

            }
        }
    }

    @OptIn(ExperimentalHeadersApi::class)
    override suspend fun connect() {
        if (outListener == null) {
            throw NullPointerException("outListener is null. please call setOutListener() first.")
        }
        if (session != null) {
            session = null
        }
        runCatching {
            logD("$TAG connect...")
            session = stompClient.connect(
                this.url,
                // 设置认证token header
                customStompConnectHeaders = mapOf(
                    "Authorization" to fromUser,
//                    "accept-version" to "1.2,1.1,1.0"
                )
            )
            onOpen()
            session?.let {
                // 启动私有消息订阅
                privateSubscriptionJob = scope.launch {
                    it.subscribe(RECEIVE_PRIVATE).collect { frame ->
                        logD("$TAG onReceive privateMessage")
                        val headers = frame.headers.asMap() // 获取消息 headers
                        val body = frame.body // 获取消息体
                        // 将消息体和 headers 一起传递给外部监听器
                        onMessage(body.toJsonString(), headers)
                    }

                }
                // 启动主题消息订阅
                topicSubscriptionJob = scope.launch {
                    it.subscribe(RECEIVE_TOPIC).collect { frame: StompFrame ->
                        val headers = frame.headers.asMap() // 获取消息 headers
                        val body = frame.body // 获取消息体
                        logD("$TAG onReceive topicMessage")
                        // 将消息体和 headers 一起传递给外部监听器
                        onMessage(body.toJsonString(), headers)
                    }
                }
                // 启动消息状态订阅
                statusSubscriptionJob = scope.launch {
                    it.subscribe(RECEIVE_MESSAGE_READ_STATUS).collect { frame: StompFrame ->
                        val headers = frame.headers.asMap() // 获取消息 headers
                        val body = frame.body // 获取消息体
                        logD("$TAG onReceive messageReadStatus")
                        // 将消息体和 headers 一起传递给外部监听器
                        onMessageReadStatus(body.toJsonString(), headers)
                    }
                }
            } ?: run {
                logD("$TAG session is null.")
                onFailure("session is null")
            }
        }.onFailure {
            logD("$TAG connect error: ${it::class.simpleName} ${it.message}")
            it.printStackTrace()
            onFailure(it.message ?: "")
            // 确保在连接失败时清理资源
            disconnect()
        }
    }

    override suspend fun disconnect() {
        logD("$TAG disconnect...")
        // 先取消订阅任务
        cleanupSubscriptions()
        session?.let {
            it.disconnect()
            onClosed()
            session = null
        } ?: run {
            logD("$TAG already disconnected!")
            onFailure("already disconnected!")
        }
    }

    /**
     * 清理订阅相关的协程任务
     */
    private fun cleanupSubscriptions() {
        privateSubscriptionJob?.cancel()
        topicSubscriptionJob?.cancel()
        statusSubscriptionJob?.cancel()
        privateSubscriptionJob = null
        topicSubscriptionJob = null
        statusSubscriptionJob = null
    }

    override suspend fun send(message: String, destination: String, headers: Map<String, String>) {
        logD("$TAG send message... --> $message")
        val jsonMessage = (message)
        session?.let {
            runCatching {
                // val receipt = it.sendText(SEND_PRIVATE, jsonMessage)
                val receipt = it.send(
                    headers = StompSendHeaders(destination) { setAll(headers) },
                    body = FrameBody.Text(jsonMessage)
                )
                logD("$TAG 收到receipt: $receipt")
                onSend(jsonMessage)
            }.onFailure { exception ->
                onFailure("send error: ${exception::class.simpleName} ${exception.message}")
            }
        } ?: run {
            logD("$TAG session is null.")
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
        logI("$TAG onOpen")
        outListener?.onOpen()
    }

    override fun onSend(message: String) {
        //logD("$TAG onSend... $message")
        outListener?.onSend(message)
    }

    override fun onSend(bytes: ByteArray) {
        logE("$TAG onSend2... $bytes")
        TODO("Not yet implemented")
    }

    override fun onMessage(body: String, headers: Map<String, String>) {
        //logD("$TAG onMessage... $headers $body")
        outListener?.onMessage(body, headers)
    }

    override fun onMessage(bytes: ByteArray, headers: Map<String, String>) {
        logE("$TAG onMessage2... $bytes")
        TODO("Not yet implemented")
    }

    override fun onMessageReadStatus(
        body: String,
        headers: Map<String, String>
    ) {
        logD("$TAG onMessageReadStatus...")
        outListener?.onMessageReadStatus(body, headers)
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