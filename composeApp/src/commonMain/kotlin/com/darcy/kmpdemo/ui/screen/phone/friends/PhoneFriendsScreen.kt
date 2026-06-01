package com.darcy.kmpdemo.ui.screen.phone.friends

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.darcy.kmpdemo.bean.http.response.FriendshipResponse
import com.darcy.kmpdemo.ui.base.impl.fetch.FetchIntent
import com.darcy.kmpdemo.ui.base.impl.screenstatus.ScreenState
import com.darcy.kmpdemo.ui.colors.AppColors
import com.darcy.kmpdemo.ui.screen.phone.friends.intent.FriendsIntent
import com.darcy.kmpdemo.ui.screen.phone.friends.state.FriendsState
import com.darcy.kmpdemo.ui.screen.phone.navigation.AppNavigation
import com.darcy.kmpdemo.ui.screen.phone.navigation.BottomBarNavigation
import com.darcy.kmpdemo.ui.screen.phone.navigation.PhoneRoute
import com.darcy.kmpdemo.ui.screen.phone.navigation.customNavigate
import io.ktor.http.encodeURLPath
import kmpdarcydemo.composeapp.generated.resources.Res
import kmpdarcydemo.composeapp.generated.resources.icon_header_default
import org.jetbrains.compose.resources.painterResource

@Composable
fun PhoneFriendsScreen() {
    val viewModel: FriendsViewModel = viewModel(factory = FriendsViewModel.Factory)
    val appNavController = AppNavigation.navController()
    LaunchedEffect(Unit) {
        viewModel.dispatch(FetchIntent.ActionFetchData())
        viewModel.event.collect {
            when (it) {
                FriendsEvent.GoAddFriend -> {
                    appNavController.customNavigate(
                        route = PhoneRoute.AddFriend, clearStack = false, includeRoot = true
                    )
                }

                FriendsEvent.GoAcceptFriend -> {
                    appNavController.customNavigate(
                        route = PhoneRoute.AcceptFriend, clearStack = false, includeRoot = true
                    )
                }

                is FriendsEvent.GoChat -> {
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
    PhoneFriendsInnerPage(viewModel, modifier = Modifier.fillMaxSize())
}

@Composable
fun PhoneFriendsInnerPage(
    viewModel: FriendsViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    Box(modifier = Modifier.fillMaxSize()) {
        when (uiState.screenState) {
            is ScreenState.Loading -> {
                // 加载中
                Text(text = "加载中")
            }

            is ScreenState.Success -> {
                // 成功
                ShowSuccessPage(uiState, viewModel)
            }

            is ScreenState.Error -> {
                // 错误
                Text(text = "错误")
            }

            else -> {
                Text(text = "其他")
            }
        }
    }
}

@Composable
private fun ShowSuccessPage(
    uiState: FriendsState,
    viewModel: FriendsViewModel,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        Row {
            Button(modifier = Modifier.weight(1f), onClick = {
                viewModel.dispatch(FriendsIntent.GoAddFriendPage)
            }) {
                Text(text = "申请好友")
            }
            Button(modifier = Modifier.weight(1f), onClick = {
                viewModel.dispatch(FriendsIntent.GoAcceptFriendPage)
            }) {
                Text(text = "同意好友")
            }
        }
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(uiState.items, key = { it.id }) { item ->
                FriendsItem(
                    item,
                    onItemClick = {
                        viewModel.dispatch(FriendsIntent.GoChatPage(item))

                    }, onItemLongClick = {
                        // todo 显示上下文菜单 删除
                        viewModel.dispatch(
                            FriendsIntent.ActionDeleteFriend(
                                item.user.id,
                                item.friend.id
                            )
                        )
                    })
            }
        }
    }
}

@Composable
private fun FriendsItem(
    bean: FriendshipResponse = FriendshipResponse(),
    modifier: Modifier = Modifier,
    onItemClick: () -> Unit = {},
    onItemLongClick: () -> Unit = {},
) {
    Column(modifier = Modifier.fillMaxWidth().height(50.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f)
                .pointerInput(Unit) {
                    detectTapGestures(
                        // 点击事件
                        onTap = {
                            onItemClick()
                        },
                        // 长按事件
                        onLongPress = {
                            onItemLongClick()
                        }
                    )
                }
                .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.Top
        ) {
            AsyncImage(
                model = bean.friend.avatar.ifEmpty { Res.drawable.icon_header_default },
                placeholder = painterResource(Res.drawable.icon_header_default),
                error = painterResource(Res.drawable.icon_header_default),
                contentDescription = null,
                modifier = Modifier.size(40.dp).clip(CircleShape).border(
                    width = 1.dp,
                    color = AppColors.bg_color_gray_f0f0f0,
                    shape = CircleShape
                )
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "${bean.id}: ${bean.friend.username} ",
                fontSize = 16.sp,
                color = AppColors.color_102c56,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Start,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth().align(Alignment.CenterVertically)
            )
        }
        Spacer(
            modifier = Modifier.fillMaxWidth().height(0.5.dp)
                .background(AppColors.bg_color_gray_f0f0f0)
        )
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xffffffff
)
@Composable
private fun FriendsItemPreview() {
    FriendsItem()
}