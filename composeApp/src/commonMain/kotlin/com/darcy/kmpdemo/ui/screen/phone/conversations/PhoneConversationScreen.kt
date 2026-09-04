package com.darcy.kmpdemo.ui.screen.phone.conversations

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.darcy.kmpdemo.bean.http.response.ConversationResponse
import com.darcy.kmpdemo.ui.base.impl.fetch.FetchIntent
import com.darcy.kmpdemo.ui.base.impl.screenstatus.ScreenState
import com.darcy.kmpdemo.ui.base.impl.screenstatus.ScreenStateIntent
import com.darcy.kmpdemo.ui.base.impl.tips.TipsIntent
import com.darcy.kmpdemo.ui.colors.AppColors
import com.darcy.kmpdemo.ui.components.structure.TipsDialog
import com.darcy.kmpdemo.ui.screen.phone.conversations.event.ConversationEvent
import com.darcy.kmpdemo.ui.screen.phone.conversations.intent.ConversationIntent
import com.darcy.kmpdemo.ui.screen.phone.conversations.state.ConversationState
import com.darcy.kmpdemo.ui.screen.phone.navigation.AppNavigation
import com.darcy.kmpdemo.ui.screen.phone.navigation.PhoneRoute
import com.darcy.kmpdemo.ui.screen.phone.navigation.customNavigate
import darcykmp_im.composeapp.generated.resources.Res
import darcykmp_im.composeapp.generated.resources.icon_header_default
import org.jetbrains.compose.resources.painterResource

@Composable
fun PhoneConversationScreen() {
    val viewModel: ConversationViewModel = viewModel(factory = ConversationViewModel.Factory)
    val appNavController = AppNavigation.navController()
    LaunchedEffect(Unit) {
        viewModel.dispatch(FetchIntent.ActionFetchData())
        viewModel.event.collect {
            when (it) {
                is ConversationEvent.GoChatPage -> {
                    appNavController.customNavigate(
                        route = PhoneRoute.Chat(
                            conversationId = it.conversationId,
                            userId = it.userId,
                            userName = it.userName,
                            userAvatar = it.userAvatar
                        ), clearStack = false, includeRoot = true
                    )
                }
            }
        }
    }
    PhoneChatListInnerPage(viewModel, Modifier.fillMaxSize())
}

@Composable
fun PhoneChatListInnerPage(
    viewModel: ConversationViewModel,
    modifier: Modifier = Modifier
) {
    val uiState: ConversationState by viewModel.uiState.collectAsStateWithLifecycle()
    Box(modifier = Modifier.fillMaxSize()) {
        when (uiState.screenState) {
            is ScreenState.Loading -> {
                Text(text = "加载中")
            }

            is ScreenState.Success -> {
                ShowSuccessPage(uiState, viewModel, modifier)
            }

            is ScreenState.Error -> {
                val errorState = uiState.screenState as ScreenState.Error
                TipsDialog(
                    titleStr = "错误",
                    contentStr = errorState.errorMessage,
                    code = errorState.errorCode,
                    confirmStr = "确定",
                    onDismissRequest = {
                        viewModel.dispatch(ScreenStateIntent.ScreenStateChange(ScreenState.Success))
                    },
                    onConfirm = {
                        viewModel.dispatch(ScreenStateIntent.ScreenStateChange(ScreenState.Success))
                    }
                )
            }

            else -> {
                Text(text = "其他")
            }
        }
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

@Composable
private fun ShowSuccessPage(
    uiState: ConversationState,
    viewModel: ConversationViewModel,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
            items(uiState.items, key = { it.id }) { item ->
                ChatListItem(item, Modifier,
                    onItemClick = {
                        viewModel.dispatch(ConversationIntent.GoChatPage(item))
                    })
            }
        }
    }
}

@Composable
private fun ChatListItem(
    bean: ConversationResponse = ConversationResponse(),
    modifier: Modifier,
    onItemClick: () -> Unit = {}
) {

    Column(modifier = Modifier.fillMaxWidth().height(68.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f)
                .clickable(onClick = onItemClick)
                .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.Top
        ) {
            AsyncImage(
                model = bean.target.avatar,
                placeholder = painterResource(Res.drawable.icon_header_default),
                error = painterResource(Res.drawable.icon_header_default),
                contentDescription = null,
                modifier = Modifier.size(48.dp).clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Box(modifier = Modifier.fillMaxSize()) {
                Column {
                    Text(
                        text = bean.target.username,
                        fontSize = 16.sp,
                        color = AppColors.color_102c56,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Start,
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(
                        text = bean.target.nickname,
                        fontSize = 14.sp,
                        color = AppColors.color_102c56,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Start,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Surface(
                    modifier = Modifier.align(Alignment.BottomEnd)
                        .size(22.dp)
                        .offset(x = 0.dp, y = (-4).dp),
                    color = AppColors.text_color_red_fb6363,
                    shape = CircleShape
                ) {
                    Text(
                        text = "12",
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = AppColors.bg_color_white,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier.wrapContentSize()
                            .align(Alignment.Center)
                            .offset(x = 0.dp, y = (-1).dp)
                    )
                }
            }
        }
        Spacer(
            modifier = Modifier.fillMaxWidth().height(0.5.dp)
                .background(AppColors.bg_color_green_13ce66)
        )
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xffffffff
)
@Composable
private fun ChatListItemPreview() {
    ChatListItem(
        modifier = Modifier.fillMaxWidth()
    )
}