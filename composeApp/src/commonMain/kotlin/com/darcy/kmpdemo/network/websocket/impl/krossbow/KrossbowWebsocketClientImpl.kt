package com.darcy.kmpdemo.network.websocket.impl.krossbow

import com.darcy.kmpdemo.crypto.JsonCryptoHelper
import com.darcy.kmpdemo.log.logD
import com.darcy.kmpdemo.log.logE
import com.darcy.kmpdemo.log.logI
import com.darcy.kmpdemo.log.logV
import com.darcy.kmpdemo.log.logW
import com.darcy.kmpdemo.network.http.impl.ktor.ktorClient
import com.darcy.kmpdemo.network.websocket.IWebSocketClient
import com.darcy.kmpdemo.network.websocket.frame.toJsonString
import com.darcy.kmpdemo.network.websocket.listener.IOuterListener
import io.ktor.utils.io.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
//import kotlinx.coroutines.newSingleThreadContext
//import kotlinx.coroutines.runBlocking
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.stomp.WebSocketClosedUnexpectedly
import org.hildan.krossbow.stomp.config.HeartBeat
import org.hildan.krossbow.stomp.config.HeartBeatTolerance
import org.hildan.krossbow.stomp.frame.StompFrame
import org.hildan.krossbow.stomp.headers.ExperimentalHeadersApi
import org.hildan.krossbow.stomp.instrumentation.KrossbowInstrumentation
import org.hildan.krossbow.websocket.WebSocketClient
import org.hildan.krossbow.websocket.WebSocketFrame
import org.hildan.krossbow.websocket.ktor.KtorWebSocketClient
import kotlin.concurrent.Volatile
import kotlin.time.Duration.Companion.milliseconds
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
    private var session: ISessionWrap? = null
    private var privateSubscriptionJob: Job? = null
    private var topicSubscriptionJob: Job? = null
    private var statusSubscriptionJob: Job? = null

    @Volatile
    private var isConnected = false

    @OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class)
//    private val dispatcher: CoroutineDispatcher = newSingleThreadContext("websocket-stomp")
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
    private val exceptionHandler: CoroutineExceptionHandler =
        CoroutineExceptionHandler { _, throwable ->
            logE("$TAG exceptionHandler: ${throwable.message}")
            throwable.printStackTrace()
            dispatchException(throwable)
//            onClosed()
        }

    fun dispatchException(throwable: Throwable) {
        logE("$TAG dispatchException: ${throwable::class.simpleName} ${throwable.message}")
        when (throwable) {
            // 连接已断开 手动调用 disconnect
            is WebSocketClosedUnexpectedly,
            is CancellationException -> {
                logE("阻塞异常 需要断开websocket连接")
                // 注意在异常handler中 不能使用scope开启协程
                CoroutineScope(Dispatchers.Default).launch {
                    disconnect()
                }
            }

            else -> {
                logW("非阻塞异常")
                outListener?.onFailure(throwable.message ?: "")
            }
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
                    logD("$TAG STOMP发送帧 --> ${frame.toString()}")
                }

                override suspend fun onFrameDecoded(
                    originalFrame: WebSocketFrame,
                    decodedFrame: StompFrame
                ) {
                    super.onFrameDecoded(originalFrame, decodedFrame)
                    logD("$TAG 解码为STOMP帧 --> ${decodedFrame.toString()}")
                }

                override suspend fun onWebSocketClientError(exception: Throwable) {
                    super.onWebSocketClientError(exception)
                    logD("$TAG 错误: ${exception.message}")
                    onFailure(exception.message ?: "")
                    exception.printStackTrace()
                }

                override suspend fun onWebSocketClosed(cause: Throwable?) {
                    super.onWebSocketClosed(cause)
                    logD("$TAG 关闭: --> ${cause?.message}")
                    cause?.printStackTrace()
                }

                override suspend fun onWebSocketFrameReceived(frame: WebSocketFrame) {
                    super.onWebSocketFrameReceived(frame)
                    logV("$TAG 接收到原始帧 --> ${frame::class.simpleName} ${frame.toString()}")
                }
            }
        }
    }

    @OptIn(ExperimentalHeadersApi::class)
    override suspend fun connect() {
        logV("$TAG 连接中...")
        if (outListener == null) {
            throw NullPointerException("outListener为空. 请先调用setOutListener().")
        }
        if (isConnected) {
            logW("$TAG 已连接")
            return
        }
        if (session != null) {
            session?.disconnect()
            session = null
        }
        runCatching {
            session = stompClient.connect(
                this.url,
                // 设置认证token header
                customStompConnectHeaders = mapOf(
                    "Authorization" to fromUser,
//                    "accept-version" to "1.2,1.1,1.0"
                )
            ).cryptoWrap()

            onOpen()
            session?.let { session ->
                // 启动私有消息订阅
                privateSubscriptionJob = scope.launch {
                    session.subscribe(RECEIVE_PRIVATE, onMessage = { body, headers ->
                        logD("$TAG 收到私聊消息")
                        onMessage(body, headers)
                    }, onFailure = { errorMessage ->
                        onFailure(errorMessage)
                    })

                }
                // 启动主题消息订阅
                topicSubscriptionJob = scope.launch {
                    session.subscribe(RECEIVE_TOPIC, onMessage = { body, headers ->
                        logD("$TAG 收到主题消息")
                        onMessage(body, headers)
                    }, onFailure = { errorMessage ->
                        onFailure(errorMessage)
                    })
                }
                // 启动消息状态订阅
                statusSubscriptionJob = scope.launch {
                    session.subscribe(RECEIVE_MESSAGE_READ_STATUS, onMessage = { body, headers ->
                        logD("$TAG 收到消息状态消息")
                        onMessageReadStatus(body, headers)
                    }, onFailure = { errorMessage ->
                        onFailure(errorMessage)
                    })
                }
            } ?: run {
                logD("$TAG session为空.")
                onFailure("session为空")
            }
        }.onFailure {
            logD("$TAG 连接错误: ${it::class.simpleName} ${it.message}")
            it.printStackTrace()
            onFailure(it.message ?: "")
            // 确保在连接失败时清理资源
            disconnect()
        }.onSuccess {
            logD("$TAG 连接成功.")
        }
    }

    override suspend fun disconnect() {
        logV("$TAG 断开连接...")
        if (isConnected.not()) {
            logW("$TAG 已断开连接")
            return
        }
        runCatching {
            // 先取消订阅任务
            cleanupSubscriptions()
            onClosed()
            runCatching {
                session?.disconnect()
            }.onFailure {
                logE("$TAG session断开错误: ${it.message}")
                it.printStackTrace()
            }.onSuccess {
                logD("$TAG session断开成功")
            }
            session = null
            isConnected = false
        }.onFailure {
            logE("$TAG 断开失败: $it")
            it.printStackTrace()
        }.onSuccess {
            logV("$TAG 断开成功")
        }
    }

    override suspend fun reconnect() {
        runCatching {
            logD("$TAG 重连中...")
            disconnect()
            delay(1_000.milliseconds)
            connect()
        }.onFailure {
            logE("$TAG 重连失败: ${it::class.simpleName} ${it.message}")
            it.printStackTrace()
        }.onSuccess {
            logD("$TAG 重连成功")
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

    override suspend fun sendText(
        message: String,
        destination: String,
        headers: Map<String, String>
    ) {
        logD("$TAG 发送消息...")
        if (isConnected.not()) {
            logE("$TAG 无法发送消息，当前未连接")
            onFailure("无法发送消息，当前未连接")
            return
        }
        session?.sendText(
            message, destination, headers, onSuccess = {
                onSend(message)
            },
            onFailure = { errorMessage ->
                onFailure(errorMessage)
            })
    }


    override suspend fun send(bytes: ByteArray) {
        TODO("Not yet implemented")
    }

    /**
     * 设置外部监听器
     */
    override fun setOuterListener(listener: IOuterListener) {
        this.outListener = listener
    }

    override fun onOpen() {
        logI("$TAG onOpen回调")
        if (isConnected.not()) {
            isConnected = true
            outListener?.onOpen()
        }
    }

    override fun onSend(message: String) {
        logD("$TAG onSend回调... text")
        outListener?.onSend(message)
    }

    override fun onSend(bytes: ByteArray) {
        logE("$TAG onSend回调... bytes")
    }

    override fun onMessage(body: String, headers: Map<String, String>) {
        logD("$TAG onMessage回调... text")
        outListener?.onMessage(body, headers)
    }

    override fun onMessage(bytes: ByteArray, headers: Map<String, String>) {
        logE("$TAG onMessage回调... bytes")
    }

    override fun onMessageReadStatus(
        body: String,
        headers: Map<String, String>
    ) {
        logD("$TAG onMessageReadStatus回调...")
        outListener?.onMessageReadStatus(body, headers)
    }

    override fun onFailure(errorMessage: String) {
        logE("$TAG onFailure回调: $errorMessage")
        outListener?.onFailure("错误: $errorMessage.")
    }

    override fun onClosed() {
        logE("$TAG onClosed回调")
        if (isConnected) {
            outListener?.onClosed()
        }
    }
}