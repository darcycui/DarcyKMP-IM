package com.darcy.kmpdemo.ui.screen.phone.chat.privatechat

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.darcy.kmpdemo.bean.http.response.PrivateMessageResponse
import com.darcy.kmpdemo.bean.http.response.isSelfSent
import com.darcy.kmpdemo.storage.memory.IMGlobalStorage
import com.darcy.kmpdemo.ui.base.impl.fetch.FetchIntent
import com.darcy.kmpdemo.ui.base.impl.tips.TipsIntent
import com.darcy.kmpdemo.ui.colors.AppColors
import com.darcy.kmpdemo.ui.components.structure.TipsDialog
import com.darcy.kmpdemo.ui.screen.phone.chat.privatechat.intent.ChatIntent
import kmpdarcydemo.composeapp.generated.resources.Res
import kmpdarcydemo.composeapp.generated.resources.check
import kmpdarcydemo.composeapp.generated.resources.icon_header_default
import org.jetbrains.compose.resources.painterResource

@Composable
fun PhoneChatScreen(
    conversationId: Long,
    userId: Long,
    userName: String,
    userAvatar: String
) {
    val viewModel: ChatViewModel = viewModel(factory = ChatViewModel.Factory)
//    val viewModel: ChatViewModel = viewModel()
    LaunchedEffect(Unit) {
        viewModel.dispatch(
            FetchIntent.ActionFetchData(
                params = mapOf(
                    "targetId" to userId.toString(),
                    "conversationId" to conversationId.toString(),
                )
            )
        )
        viewModel.dispatch(ChatIntent.ActionRegisterReceiveMessage)
    }
    PhoneChatInnerPage(viewModel, conversationId, userId, userName, userAvatar)
}

@Composable
private fun PhoneChatInnerPage(
    viewModel: ChatViewModel,
    conversationId: Long,
    userId: Long,
    userName: String,
    userAvatar: String
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = uiState.webSocketConnectionState.message,
                modifier = Modifier.align(Alignment.Start)
            )
            Text(text = userName, modifier = Modifier.align(Alignment.CenterHorizontally))
            PrivateMessageListComponent(
                messageList = uiState.items,
                userId = userId,
                userName = userName,
                userAvatar = userAvatar,
                modifier = Modifier.fillMaxWidth().weight(1f)
            )
            SendComponent(onSendClick = { text ->
                val self = IMGlobalStorage.getCurrentUser()
                viewModel.dispatch(
                    ChatIntent.ActionSendMessage(
                        PrivateMessageResponse(
                            senderId = self.id,
                            senderName = self.username,
                            receiverId = userId,
                            receiverName = userName,
                            content = text,
                            msgType = "TEXT"
                        )
                    )
                )
            }, modifier = Modifier.fillMaxWidth())
            if (uiState.tipsState.showTips) {
                TipsDialog(
                    titleStr = uiState.tipsState.title,
                    contentStr = uiState.tipsState.tips,
                    code = uiState.tipsState.code,
                    confirmStr = uiState.tipsState.middleButtonText,
                    onDismissRequest = {
                        viewModel.dispatch(TipsIntent.DismissTips)
                    },
                    onConfirm = {
                        viewModel.dispatch(TipsIntent.DismissTips)
                    }
                )
            }
        }
    }
}

@Composable
fun SendComponent(
    onSendClick: (String) -> Unit = {},
    modifier: Modifier,
) {
    val textState = TextFieldState("")
    Row(modifier = modifier.height(60.dp), verticalAlignment = Alignment.CenterVertically) {
        TextField(
            state = textState,
            placeholder = { Text("请输入内容") },
            modifier = Modifier.weight(1f)
                .onPreviewKeyEvent { keyEvent ->
                    // 监听回车键
                    if (keyEvent.key == Key.Enter && keyEvent.type == KeyEventType.KeyUp) {
                        val text = textState.text.toString().trim()
                        if (text.isNotEmpty()) {
                            onSendClick(text)
                            // 发送以后清空输入框
                            textState.clearText()
                        }
                        true
                    } else {
                        false
                    }
                }
        )
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            modifier = Modifier,
            onClick = {
                onSendClick(textState.text.toString())
                // 发送以后清空输入框
                textState.clearText()
            }) {
            Text("发送")
        }
    }
}

@Composable
fun PrivateMessageListComponent(
    messageList: List<PrivateMessageResponse>,
    userId: Long,
    userName: String,
    userAvatar: String,
    modifier: Modifier,
) {
    LazyColumn(modifier = modifier) {
        items(
            messageList,
            key = { it.msgId }
        ) { item ->
            if (item.isSelfSent()) {
                SendMessageComponent(item)
            } else {
                ReceiveMessageComponent(item, userId, userName, userAvatar)
            }
        }
    }

}

@Composable
fun ReceiveMessageComponent(
    item: PrivateMessageResponse,
    userId: Long,
    userName: String,
    userAvatar: String
) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
        Row(modifier = Modifier.width(300.dp), verticalAlignment = Alignment.Top) {
            AsyncImage(
                model = userAvatar.ifEmpty { Res.drawable.icon_header_default },
                contentDescription = null,
                placeholder = painterResource(Res.drawable.icon_header_default),
                error = painterResource(Res.drawable.icon_header_default),
                modifier = Modifier.size(40.dp).clip(CircleShape).border(
                    width = 1.dp,
                    color = AppColors.bg_color_gray_f0f0f0,
                    shape = CircleShape
                )
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                Spacer(modifier = Modifier.height(11.dp))
                Text(text = item.content)
            }
        }
    }
}

@Composable
fun SendMessageComponent(item: PrivateMessageResponse) {
    val selfAvatar = IMGlobalStorage.getCurrentUser().avatar
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
        Row(modifier = Modifier.width(300.dp), verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f)) {
                Spacer(modifier = Modifier.height(11.dp))
                Text(
                    text = item.content,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
                // 显示消息已读
                if (item.isRead) {
                    Image(painter = painterResource(Res.drawable.check), contentDescription = null,
                        modifier = Modifier.align(Alignment.End))
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            AsyncImage(
                model = selfAvatar.ifEmpty { Res.drawable.icon_header_default },
                contentDescription = null,
                placeholder = painterResource(Res.drawable.icon_header_default),
                error = painterResource(Res.drawable.icon_header_default),
                modifier = Modifier.size(40.dp).clip(CircleShape).border(
                    width = 1.dp,
                    color = AppColors.bg_color_gray_f0f0f0,
                    shape = CircleShape
                )
            )
        }
    }
}


@Preview
@Composable
fun ReceiveMessageComponentPreview() {
    ReceiveMessageComponent(
        item = PrivateMessageResponse(
            senderId = 1,
            senderName = "张三",
            receiverId = 2,
            receiverName = "李四",
            content = "这是消息内容",
            msgType = "TEXT"
        ),
        userId = 2,
        userName = "张三",
        userAvatar = ""
    )
}

@Preview
@Composable
fun SendMessageComponentPreview() {
    SendMessageComponent(
        item = PrivateMessageResponse(
            senderId = 1,
            senderName = "张三",
            receiverId = 2,
            receiverName = "李四",
            content = "这是消息内容",
            msgType = "TEXT"
        ),
    )
}

