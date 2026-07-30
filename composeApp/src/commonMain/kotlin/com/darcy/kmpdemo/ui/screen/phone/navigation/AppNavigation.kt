package com.darcy.kmpdemo.ui.screen.phone.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.darcy.kmpdemo.platform.Platform
import com.darcy.kmpdemo.ui.screen.desktop.DesktopAppMainScreen
import com.darcy.kmpdemo.ui.screen.phone.PhoneAppMainScreen
import com.darcy.kmpdemo.ui.screen.phone.accept_friend.PhoneAcceptFriendScreen
import com.darcy.kmpdemo.ui.screen.phone.apply_friend.PhoneAddFriendScreen
import com.darcy.kmpdemo.ui.screen.phone.chat.privatechat.PhoneChatScreen
import com.darcy.kmpdemo.ui.screen.phone.login.PhoneLoginScreen
import com.darcy.kmpdemo.ui.screen.phone.register.PhoneRegisterScreen

object AppNavigation {

    // 定义全局 AppNavController
    private val appLocalNavController = staticCompositionLocalOf<NavHostController> {
        error("AppNavController not provided")
    }

    /**
     * 获取 CompositionLocal 用于初始化 AppNavController
     */
    fun getCompositionLocal(): ProvidableCompositionLocal<NavHostController> {
        return appLocalNavController
    }

    /**
     * 获取 AppNavController 用于页面跳转
     */
    @Composable
    fun navController(): NavHostController {
        return appLocalNavController.current
    }
}

@Composable
fun AppNavigationNavHost(
    navController: NavHostController,
    startDestination: PhoneRoute,
    modifier: Modifier = Modifier
) {
    CompositionLocalProvider(AppNavigation.getCompositionLocal() provides navController) {
        NavHost(
            navController,
            startDestination = startDestination
        ) {
            composable<PhoneRoute.Login> {
                PhoneLoginScreen()
            }
            composable<PhoneRoute.Register> {
                PhoneRegisterScreen()
            }
            composable<PhoneRoute.AddFriend> {
                PhoneAddFriendScreen()
            }
            composable<PhoneRoute.AcceptFriend> {
                PhoneAcceptFriendScreen()
            }
            composable<PhoneRoute.Chat> {backStackEntry->
                val chatRoute = backStackEntry.toRoute<PhoneRoute.Chat>()
                PhoneChatScreen(
                    conversationId = chatRoute.conversationId,
                    userId = chatRoute.userId,
                    userName = chatRoute.userName,
                    userAvatar = chatRoute.userAvatar
                )
            }
            composable<PhoneRoute.AppMain> {
                AppMainScreen()
//                LearnNavigation()
            }
        }
    }
}

@Composable
private fun AppMainScreen() {
    if (Platform.isPhonePlatform()) {
        PhoneAppMainScreen()
    } else {
        DesktopAppMainScreen()
    }
}