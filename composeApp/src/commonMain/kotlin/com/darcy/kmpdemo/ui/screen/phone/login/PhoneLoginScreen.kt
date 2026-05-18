package com.darcy.kmpdemo.ui.screen.phone.login

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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.darcy.kmpdemo.bean.ui.LoginBean
import com.darcy.kmpdemo.ui.base.impl.tips.TipsIntent
import com.darcy.kmpdemo.ui.components.structure.TipsDialog
import com.darcy.kmpdemo.ui.screen.phone.login.event.LoginEvent
import com.darcy.kmpdemo.ui.screen.phone.login.intent.LoginIntent
import com.darcy.kmpdemo.ui.screen.phone.navigation.AppNavigation
import com.darcy.kmpdemo.ui.screen.phone.navigation.PhoneRoute
import com.darcy.kmpdemo.ui.screen.phone.navigation.customNavigate
import com.darcy.kmpdemo.utils.HashHelper
import kmpdarcydemo.composeapp.generated.resources.Res
import kmpdarcydemo.composeapp.generated.resources.go_register
import kmpdarcydemo.composeapp.generated.resources.page_login
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun PhoneLoginScreen() {
    val viewModel: LoginViewModel = viewModel(factory = LoginViewModel.Factory)
    val appNavController = AppNavigation.navController()
    LaunchedEffect(Unit) {
        launch {
            viewModel.event.collect {
                when (it) {
                    is LoginEvent.LoginSuccessEvent -> {
                        appNavController.customNavigate(
                            route = PhoneRoute.AppMain, clearStack = true, includeRoot = true
                        )
                    }
                    is LoginEvent.GoRegisterEvent -> {
                        appNavController.customNavigate(
                            route = PhoneRoute.Register, clearStack = false, includeRoot = true
                        )
                    }
                }
            }
        }

    }
    PhoneLoginInnerPage(viewModel)
}

@Composable
fun PhoneLoginInnerPage(viewModel: LoginViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val nameTextFieldState: TextFieldState by remember { mutableStateOf(TextFieldState("156000111222")) }
    val passwordTextFieldState: TextFieldState by remember { mutableStateOf(TextFieldState("123456")) }
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            LoginComponent(nameTextFieldState, passwordTextFieldState, viewModel)
            GoRegisterComponent(viewModel)
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
private fun GoRegisterComponent(viewModel: LoginViewModel) {
    Button(modifier = Modifier.fillMaxWidth(), onClick = {
        viewModel.dispatch(LoginIntent.ActionGoRegister)
    }) {
        Text(stringResource(Res.string.go_register))
    }
}

@Composable
private fun LoginComponent(
    nameTextFieldState: TextFieldState,
    passwordTextFieldState: TextFieldState,
    viewModel: LoginViewModel
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        TextField(
            state = nameTextFieldState,
            placeholder = { Text("手机号") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            state = passwordTextFieldState,
            placeholder = { Text("密码") },
            modifier = Modifier.fillMaxWidth()
        )
        Button(modifier = Modifier.fillMaxWidth(), onClick = {
            val phone = nameTextFieldState.text.toString()
            val password = passwordTextFieldState.text.toString()
            viewModel.dispatch(
                LoginIntent.ActionLogin(
                    LoginBean(
                        phone = phone,
                        password = HashHelper.sha256Str(password)
                    )
                )
            )
        }) {
            Text(stringResource(Res.string.page_login))
        }
    }
}

