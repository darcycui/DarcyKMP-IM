package com.darcy.kmpdemo.ui.screen.phone.mine

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.darcy.kmpdemo.ui.base.impl.fetch.FetchIntent
import com.darcy.kmpdemo.ui.colors.AppColors
import com.darcy.kmpdemo.ui.screen.phone.mine.event.MineEvent
import com.darcy.kmpdemo.ui.screen.phone.mine.intent.MineIntent
import com.darcy.kmpdemo.ui.screen.phone.navigation.AppNavigation
import com.darcy.kmpdemo.ui.screen.phone.navigation.PhoneRoute
import com.darcy.kmpdemo.ui.screen.phone.navigation.customNavigate
import darcykmp_im.composeapp.generated.resources.Res
import darcykmp_im.composeapp.generated.resources.icon_header_default
import darcykmp_im.composeapp.generated.resources.page_mine
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun PhoneMineScreen() {
    val viewModel: MineViewModel = viewModel(factory = MineViewModel.Factory)
    val appNavController = AppNavigation.navController()
    LaunchedEffect(Unit) {
        launch {
            viewModel.event.collect {
                when (it) {
                    is MineEvent.ActionLogout -> {
                        appNavController.customNavigate(PhoneRoute.Login)
                    }
                }
            }
        }
        viewModel.dispatch(FetchIntent.ActionFetchData())
    }
    PhoneMineInnerPage(viewModel, Modifier.fillMaxSize())
}

@Composable
fun PhoneMineInnerPage(
    viewModel: MineViewModel,
    modifier: Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.size(16.dp))
            AsyncImage(
                model = uiState.bean.avatar.ifEmpty { Res.drawable.icon_header_default },
                contentDescription = stringResource(Res.string.page_mine),
                modifier = Modifier.size(120.dp).align(Alignment.CenterHorizontally)
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
                    .clickable {
                        viewModel.dispatch(MineIntent.ActionPickImage)
                    }
            )
            Spacer(modifier = Modifier.size(16.dp))
            Text(
                text = uiState.bean.name,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            Text(
                text = uiState.bean.phone,
                fontSize = 20.sp,
                fontWeight = FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            Spacer(modifier = Modifier.size(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth().height(40.dp).align(Alignment.CenterHorizontally)
            ) {
                Button(modifier = Modifier.fillMaxHeight().weight(1f), onClick = {
                    viewModel.dispatch(MineIntent.ActionLogout)
                }) {
                    Text(text = "退出登录")
                }
            }
        }
    }
}