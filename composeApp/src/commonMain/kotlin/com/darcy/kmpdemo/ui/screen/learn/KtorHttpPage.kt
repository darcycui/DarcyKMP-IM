package com.darcy.kmpdemo.ui.screen.learn

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.darcy.kmpdemo.bean.http.response.JuHeIPResponse
import com.darcy.kmpdemo.bean.http.response.DarcyServerResponse
import com.darcy.kmpdemo.log.logD
import com.darcy.kmpdemo.log.logE
import com.darcy.kmpdemo.log.logI
import com.darcy.kmpdemo.network.http.HttpManager
import com.darcy.kmpdemo.network.http.urls.JuHe.IP_URL
//import com.darcy.kmpdemo.network.ssl.SslSettings
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.serializer
import org.example.library.SharedRes

@Composable
fun ShowKtorHttp() {
    // 保存状态
    val content = remember { mutableStateOf("") }
    // 获取协程作用域
    val composeScope: CoroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        Button(onClick = {
            content.value = "getJuHe"
            doGetJuHe(composeScope, content)
        }) {
            Text(text = stringResource(SharedRes.strings.http_get))
        }
        Button(onClick = {
            content.value = "getDarcy"
            doGetDarcy(composeScope, content)
        }) {
            Text(text = stringResource(SharedRes.strings.http_get_darcy))
        }
        Button(onClick = {
            content.value = "post"
            doPost(composeScope, content)
        }) {
            Text(text = stringResource(SharedRes.strings.http_post))
        }
        Text(text = content.value)

    }
}


// FIXME: CertPathValidatorException: Trust anchor for certification path not found.
const val urlDarcy = "https://darcycui.com.cn/users/all"
//const val urlDarcy = "https://10.0.0.241/api/users/all-"

private fun doGetJuHe(scope: CoroutineScope, content: MutableState<String>) {
    HttpManager.doGetRequest(
        serializer<JuHeIPResponse>(),
        IP_URL,
        mapOf(
            "key" to "f128bfc760193c5762c5c3be2a6051d8",
            "ip" to "114.215.154.101"
        ),
        needRetry = true,
        needCache = true,
        success = {
            logI("success: itClazz=${it.result::class}")
            updateText(scope, content, it.toString())
        },
        successList = {},
        errors = {
            logE("error: it=$it")
            updateText(scope, content, "${it.status} ${it.message}")
        })
}

private fun doGetDarcy(scope: CoroutineScope, content: MutableState<String>) {
    HttpManager.doGetRequest(
        serializer<DarcyServerResponse>(),
        urlDarcy,
        mapOf(),
        needRetry = true,
        needCache = true,
        success = {},
        successList = {
            logD("success: itClazz=${it.result::class}")
            updateText(scope, content, it.toString())
        },
        errors = {
            logD("error: it=$it")
            updateText(scope, content, "${it.status} ${it.message}")
        })
}

private fun doPost(scope: CoroutineScope, content: MutableState<String>) {
    HttpManager.doPostRequest(
        serializer<JuHeIPResponse>(),
        IP_URL,
        mapOf(
            "key" to "f128bfc760193c5762c5c3be2a6051d8",
            "ip" to "114.215.154.101"
        ),
        needRetry = true,
        needCache = true,
        success = {
            logD("success: itClazz=${it.result::class}")
            updateText(scope, content, it.toString())
        },
        successList = {},
        errors = {
            logD("error: it=$it")
            updateText(scope, content, "${it.status} ${it.message}")
        })
}

private fun updateText(scope: CoroutineScope, content: MutableState<String>, text: String) {
    scope.launch(Dispatchers.Main) {
        logD("text:$text")
        content.value += "\n$text"
    }
}
