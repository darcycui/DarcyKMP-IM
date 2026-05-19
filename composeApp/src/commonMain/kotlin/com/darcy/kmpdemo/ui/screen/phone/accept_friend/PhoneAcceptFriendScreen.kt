package com.darcy.kmpdemo.ui.screen.phone.accept_friend

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.darcy.kmpdemo.bean.http.response.ApplyFriendResponse
import com.darcy.kmpdemo.ui.base.impl.fetch.FetchIntent
import com.darcy.kmpdemo.ui.base.impl.tips.TipsIntent
import com.darcy.kmpdemo.ui.colors.AppColors
import com.darcy.kmpdemo.ui.components.structure.TipsDialog
import com.darcy.kmpdemo.ui.screen.phone.accept_friend.intent.AcceptFriendIntent
import com.darcy.kmpdemo.ui.screen.phone.accept_friend.state.AcceptFriendState
import kmpdarcydemo.composeapp.generated.resources.Res
import kmpdarcydemo.composeapp.generated.resources.icon_header_default
import kmpdarcydemo.composeapp.generated.resources.page_mine
import org.jetbrains.compose.resources.stringResource
import kotlin.text.ifEmpty

@Composable
fun PhoneAcceptFriendScreen() {
    val viewModel: AcceptFriendViewModel = viewModel(
        factory = AcceptFriendViewModel.Factory
    )
    LaunchedEffect(viewModel){
        viewModel.dispatch(FetchIntent.ActionFetchData())
    }
    PhoneAcceptFriendInnerPage(viewModel)
}

@Composable
fun PhoneAcceptFriendInnerPage(viewModel: AcceptFriendViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val nameTextFieldState: TextFieldState by remember { mutableStateOf(TextFieldState("")) }
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            ApplyListComponent(uiState, viewModel)
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
private fun ApplyListComponent(uiState: AcceptFriendState, viewModel: AcceptFriendViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        items(uiState.applys, key = { it.id}) {
            ApplyItemComponent(it, viewModel)
        }
    }
}

@Composable
fun ApplyItemComponent(bean: ApplyFriendResponse, viewModel: AcceptFriendViewModel) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Spacer(modifier = Modifier.width(8.dp))
        AsyncImage(
            model = bean.fromUser.avatar.ifEmpty { Res.drawable.icon_header_default },
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
        Text(text = bean.fromUser.username, modifier = Modifier.weight(1f))
        Button(onClick = {
            viewModel.dispatch(AcceptFriendIntent.ActionAcceptFriend(bean.id, bean.fromUser.id))
        }) {
            Text(text = ApplyFriendResponse.RequestStatus.fromCode(bean.status).name)
        }
        Spacer(modifier = Modifier.width(8.dp))
    }
}