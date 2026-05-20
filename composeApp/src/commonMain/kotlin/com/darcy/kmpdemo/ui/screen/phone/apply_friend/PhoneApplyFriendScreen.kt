package com.darcy.kmpdemo.ui.screen.phone.apply_friend

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.darcy.kmpdemo.bean.http.response.ApplyFriendResponse
import com.darcy.kmpdemo.bean.http.response.UserResponse
import com.darcy.kmpdemo.ui.base.impl.fetch.FetchIntent
import com.darcy.kmpdemo.ui.base.impl.tips.TipsIntent
import com.darcy.kmpdemo.ui.colors.AppColors
import com.darcy.kmpdemo.ui.components.structure.TipsDialog
import com.darcy.kmpdemo.ui.screen.phone.apply_friend.intent.ApplyFriendIntent
import com.darcy.kmpdemo.ui.screen.phone.apply_friend.state.ApplyFriendState
import kmpdarcydemo.composeapp.generated.resources.Res
import kmpdarcydemo.composeapp.generated.resources.icon_header_default
import kmpdarcydemo.composeapp.generated.resources.page_mine
import org.jetbrains.compose.resources.stringResource

@Composable
fun PhoneAddFriendScreen() {
    val viewModel: ApplyFriendViewModel = viewModel(
        factory = ApplyFriendViewModel.Factory
    )
    LaunchedEffect(viewModel) {
        viewModel.dispatch(FetchIntent.ActionFetchData())
    }
    PhoneAddFriendInnerPage(viewModel)
}

@Composable
fun PhoneAddFriendInnerPage(viewModel: ApplyFriendViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val nameTextFieldState: TextFieldState by remember { mutableStateOf(TextFieldState("")) }
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            SearchUserComponent(
                nameTextFieldState,
                viewModel
            )
            UserSearchComponent(uiState.userInfo, uiState.statusText, viewModel)
            // 分隔线 指定背景颜色
            HorizontalDivider(
                modifier = Modifier.height(4.dp).background(AppColors.bg_color_blue_409eff)
            )
            UserApplyListComponent(uiState, viewModel)
        }

        if (uiState.tipsState.showTips) {
            uiState.tipsState.apply {
                TipsDialog(
                    titleStr = title,
                    contentStr = tips,
                    code = code,
                    confirmStr = middleButtonText,
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
fun UserApplyListComponent(uiState: ApplyFriendState, viewModel: ApplyFriendViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        items(uiState.applys, key = { it.id }) { item ->
            UserItemComponent(item, viewModel)
            Text("")
        }
    }
}

@Preview
@Composable
private fun SearchUserComponent(
    nameTextFieldState: TextFieldState = TextFieldState(""),
    viewModel: ApplyFriendViewModel? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        TextField(
            state = nameTextFieldState,
            placeholder = {
                Text(text = "请输入手机号")
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                val phone = nameTextFieldState.text.toString()
                viewModel?.dispatch(ApplyFriendIntent.ActionSearchUser(phone))
            }) {
            Text(text = "搜索")
        }
    }
}


@Preview
@Composable
fun UserItemComponent(
    bean: ApplyFriendResponse = ApplyFriendResponse(),
    viewModel: ApplyFriendViewModel? = null
) {
    val user = bean.toUser
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Spacer(modifier = Modifier.width(8.dp))
        AsyncImage(
            model = user.avatar.ifEmpty { Res.drawable.icon_header_default },
            contentDescription = stringResource(Res.string.page_mine),
            modifier = Modifier.size(40.dp)
                .clip(CircleShape)
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            AppColors.color_9e82f0,
                            AppColors.color_42a5f5,
                        )
                    ),
                    shape = CircleShape
                )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = user.username, modifier = Modifier.weight(1f))
        Button(onClick = {
            viewModel?.dispatch(ApplyFriendIntent.ActionApplyFriend(user.id))
        }) {
            Text(text = ApplyFriendResponse.RequestStatus.fromCode(bean.status).name)
        }
        Spacer(modifier = Modifier.width(8.dp))
    }
}


@Preview
@Composable
fun UserSearchComponent(
    user: UserResponse = UserResponse(),
    statusText: String,
    viewModel: ApplyFriendViewModel? = null
) {
    if (user.id == 0L) {
        return
    }
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Spacer(modifier = Modifier.width(8.dp))
        AsyncImage(
            model = user.avatar.ifEmpty { Res.drawable.icon_header_default },
            contentDescription = stringResource(Res.string.page_mine),
            modifier = Modifier.size(40.dp)
                .clip(CircleShape)
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            AppColors.color_9e82f0,
                            AppColors.color_42a5f5,
                        )
                    ),
                    shape = CircleShape
                )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = user.username, modifier = Modifier.weight(1f))
        Button(onClick = {
            viewModel?.dispatch(ApplyFriendIntent.ActionApplyFriend(user.id))
        }) {
            Text(text = statusText)
        }
        Spacer(modifier = Modifier.width(8.dp))
    }
}