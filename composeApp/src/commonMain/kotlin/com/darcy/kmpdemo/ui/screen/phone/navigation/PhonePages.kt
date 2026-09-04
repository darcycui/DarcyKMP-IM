package com.darcy.kmpdemo.ui.screen.phone.navigation

import darcykmp_im.composeapp.generated.resources.Res
import darcykmp_im.composeapp.generated.resources.icon_home
import darcykmp_im.composeapp.generated.resources.icon_mine
import darcykmp_im.composeapp.generated.resources.icon_notification
import darcykmp_im.composeapp.generated.resources.icon_search
import darcykmp_im.composeapp.generated.resources.page_chat_list
import darcykmp_im.composeapp.generated.resources.page_dynamic
import darcykmp_im.composeapp.generated.resources.page_friends
import darcykmp_im.composeapp.generated.resources.page_mine
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

enum class PhonePages(
    val title: StringResource,
    val icon: DrawableResource,
    val route: PhoneRoute = PhoneRoute.Default
) {
    ChatList(Res.string.page_chat_list, Res.drawable.icon_home, PhoneRoute.ChatList),
    Friends(Res.string.page_friends, Res.drawable.icon_search, PhoneRoute.Friends),
    Dynamic(Res.string.page_dynamic, Res.drawable.icon_notification, PhoneRoute.Dynamic),
    Mine(Res.string.page_mine, Res.drawable.icon_mine, PhoneRoute.Mine)
}

@Serializable
sealed class PhoneRoute() {
    @Serializable
    data object Default : PhoneRoute()

    @Serializable
    data object Login : PhoneRoute()

    @Serializable
    data object Register : PhoneRoute()

    @Serializable
    data object AddFriend : PhoneRoute()

    @Serializable
    data object AcceptFriend : PhoneRoute()

    @Serializable
    data object AppMain : PhoneRoute()

    @Serializable
    data object ChatList : PhoneRoute()

    @Serializable
    data object Friends : PhoneRoute()

    @Serializable
    data object Dynamic : PhoneRoute()

    @Serializable
    data object Mine : PhoneRoute()

    @Serializable
    data class Chat (
        val conversationId: Long,
        val userId: Long,
        val userName: String,
        val userAvatar: String
    ) : PhoneRoute()
}