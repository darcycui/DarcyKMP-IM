package com.darcy.kmpdemo.ui.screen.phone.register

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.darcy.kmpdemo.bean.ui.RegisterBean
import com.darcy.kmpdemo.ui.base.impl.tips.TipsIntent
import com.darcy.kmpdemo.ui.components.structure.TipsDialog
import com.darcy.kmpdemo.ui.screen.phone.navigation.AppNavigation
import com.darcy.kmpdemo.ui.screen.phone.navigation.PhoneRoute
import com.darcy.kmpdemo.ui.screen.phone.navigation.customNavigate
import com.darcy.kmpdemo.ui.screen.phone.register.event.RegisterEvent
import com.darcy.kmpdemo.ui.screen.phone.register.intent.RegisterIntent
import com.darcy.kmpdemo.utils.HashHelper
import kmpdarcydemo.composeapp.generated.resources.Res
import kmpdarcydemo.composeapp.generated.resources.page_register
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun PhoneRegisterScreen() {
    val viewModel: RegisterViewModel = viewModel(factory = RegisterViewModel.Factory)
    val appNavController = AppNavigation.navController()
    LaunchedEffect(Unit) {
        launch {
            viewModel.event.collect {
                when (it) {
                    is RegisterEvent.RegisterSuccessEvent -> {
                        appNavController.customNavigate(
                            route = PhoneRoute.AppMain, clearStack = true, includeRoot = true
                        )
                    }
                }
            }
        }
    }
    PhoneRegisterInnerPage(viewModel)
}

@Composable
fun PhoneRegisterInnerPage(viewModel: RegisterViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val nameTextFieldState: TextFieldState by remember { mutableStateOf(TextFieldState("")) }
    val nickNameTextFieldState: TextFieldState by remember { mutableStateOf(TextFieldState("")) }
    val passwordTextFieldState: TextFieldState by remember { mutableStateOf(TextFieldState("")) }
    val phoneTextFieldState: TextFieldState by remember { mutableStateOf(TextFieldState("")) }
    val emailTextFieldState: TextFieldState by remember { mutableStateOf(TextFieldState("")) }
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            RegisterComponent(
                nameTextFieldState,
                nickNameTextFieldState,
                passwordTextFieldState,
                phoneTextFieldState,
                emailTextFieldState,
                viewModel
            )
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

@Preview
@Composable
private fun RegisterComponent(
    nameTextFieldState: TextFieldState = TextFieldState(""),
    nickNameTextFieldState: TextFieldState = TextFieldState(""),
    passwordTextFieldState: TextFieldState = TextFieldState("123456"),
    phoneTextFieldState: TextFieldState = TextFieldState(""),
    emailTextFieldState: TextFieldState = TextFieldState(""),
    viewModel: RegisterViewModel? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        TextField(
            state = nameTextFieldState,
            placeholder = { Text("用户名") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            state = nickNameTextFieldState,
            placeholder = { Text("昵称") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            state = passwordTextFieldState,
            placeholder = { Text("密码") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            state = phoneTextFieldState,
            placeholder = { Text("手机号") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            state = emailTextFieldState,
            placeholder = { Text("邮箱") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )
        Button(modifier = Modifier.fillMaxWidth(), onClick = {
            val name = nameTextFieldState.text.toString()
            val password = passwordTextFieldState.text.toString()
            val nickname = nickNameTextFieldState.text.toString()
            val phone = phoneTextFieldState.text.toString()
            val email = emailTextFieldState.text.toString()
            viewModel?.dispatch(
                RegisterIntent.ActionRegister(
                    RegisterBean(
                        username = name,
                        passwordHash = HashHelper.sha256Str(password),
                        nickname = nickname,
                        phone = phone,
                        avatar = "https://avatars.githubusercontent.com/u/3814078?v=4",
                        email = email,
                        gender = "male",
                        signature = "",
                        status = 1, // 1: 正常 2: 禁用 3: 删除
                        lastActiveTime = null,
                        deletedAt = null,
                        settings = emptyMap(),
                        roles = "admin",
                        token = "",
                    )
                )
            )
        }) {
            Text(stringResource(Res.string.page_register))
        }
    }
}