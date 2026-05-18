package com.darcy.kmpdemo.ui.screen.learn

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.darcy.kmpdemo.log.logD
import com.darcy.kmpdemo.network.websocket.WebSocketManager
import com.darcy.kmpdemo.network.websocket.listener.IOuterListener
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.example.library.SharedRes

@Composable
fun ShowKtorWebsocketSTOMP() {
    // 滚动状态
    val scrollState = rememberScrollState()
    // 文本状态
    val content = remember { mutableStateOf("") }
    // 获取协程作用域
    val composeScope: CoroutineScope = rememberCoroutineScope()
    DisposableEffect(key1 = "") {
        onDispose {
            disconnectWS(composeScope, content)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Button(onClick = { connectWS(composeScope, content) }) {
            Text(text = stringResource(SharedRes.strings.websocket_connect_stomp))
        }
        Button(onClick = { sendWS(composeScope, content) }) {
            Text(text = stringResource(SharedRes.strings.websocket_send_stomp))
        }
        Button(onClick = { disconnectWS(composeScope, content) }) {
            Text(text = stringResource(SharedRes.strings.websocket_disconnect_stomp))
        }
        Column(modifier = Modifier.verticalScroll(scrollState)) {
            Text(text = content.value)
        }

    }
}

private fun connectWS(scope: CoroutineScope, content: MutableState<String>) {
    updateText(scope, content, "connect")
    scope.launch(Dispatchers.Default) {
        WebSocketManager.init(url = "ws://localhost:7443/stomp-ws", userToken = "test2")
        WebSocketManager.setOuterListener(object : IOuterListener {
            override fun onOpen() {
                updateText(scope, content, "onOpen")
            }

            override fun onSend(message: String) {
                updateText(scope, content, "onSend:$message")
            }

            override fun onSend(bytes: ByteArray) {
                TODO("Not yet implemented")
            }

            override fun onMessage(message: String) {
                updateText(scope, content, "onMessage:$message")
            }

            override fun onMessage(bytes: ByteArray) {
//                updateText(scope, content, "onMessage:${bytes.toString(Charsets.UTF_8)}")
            }

            override fun onFailure(errorMessage: String) {
                updateText(scope, content, "onFailure:$errorMessage")
            }

            override fun onClosed() {
                updateText(scope, content, "onClosed")
            }
        })
        WebSocketManager.connect()
    }
}

private fun updateText(scope: CoroutineScope, content: MutableState<String>, text: String) {
    scope.launch(Dispatchers.Main) {
        logD("text:$text")
        content.value += "\n$text"
    }
}

private var count = 0
private fun sendWS(
    scope: CoroutineScope,
    content: MutableState<String>
) {
    scope.launch(Dispatchers.Default) {
        count++
//        WebSocketManager.send("hello-$count")
    }
}

private fun disconnectWS(
    scope: CoroutineScope,
    content: MutableState<String>
) {
    updateText(scope, content, "disconnect")
    scope.launch(Dispatchers.Default) {
        WebSocketManager.disconnect()
    }
}
