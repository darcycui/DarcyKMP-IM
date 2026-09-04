package com.darcy.kmpdemo.ui.screen.learn.navigation

import com.darcy.kmpdemo.log.logD
import darcykmp_im.composeapp.generated.resources.Res
import darcykmp_im.composeapp.generated.resources.custom_draw
import darcykmp_im.composeapp.generated.resources.download_image
import darcykmp_im.composeapp.generated.resources.encrypt_file
import darcykmp_im.composeapp.generated.resources.encrypt_text
import darcykmp_im.composeapp.generated.resources.home
import darcykmp_im.composeapp.generated.resources.ktor_http
import darcykmp_im.composeapp.generated.resources.ktor_websocket
import darcykmp_im.composeapp.generated.resources.ktor_websocket_stomp
import darcykmp_im.composeapp.generated.resources.load_data
import darcykmp_im.composeapp.generated.resources.load_moko_resource
import darcykmp_im.composeapp.generated.resources.load_resource
import darcykmp_im.composeapp.generated.resources.navigation_rail
import darcykmp_im.composeapp.generated.resources.number_card
import darcykmp_im.composeapp.generated.resources.unknown
import darcykmp_im.composeapp.generated.resources.upload_image
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource

@Serializable
enum class LearnPages(
    val title: StringResource,
    val routeName: String,
) {
    UnknownPage(title = Res.string.unknown, routeName = "UnknownPage"),
    HomePage(title = Res.string.home, routeName = "Home"),
    EncryptTextPage(
        title = Res.string.encrypt_text,
        routeName = "EncryptText"
    ),
    EncryptFilePage(
        title = Res.string.encrypt_file,
        routeName = "EncryptFile"
    ),
    LoadResourcePage(
        title = Res.string.load_resource,
        routeName = "LoadResource"
    ),
    LoadMokoResourcePage(
        title = Res.string.load_moko_resource,
        routeName = "LoadMokoResource"
    ),
    KtorHttpPage(
        title = Res.string.ktor_http,
        routeName = "KtorHttp"
    ),
    KtorWebsocketPage(
        title = Res.string.ktor_websocket,
        routeName = "KtorWebsocket"
    ),
    DownloadImagePage(
        title = Res.string.download_image,
        routeName = "DownloadImage"
    ),
    UploadImagePage(
        title = Res.string.upload_image,
        routeName = "UploadImage"
    ),
    KtorWebSocketSTMOPPage(
        title = Res.string.ktor_websocket_stomp,
        routeName = "KtorWebSocketSTMOP"
    ),
    NavigationRailPage(
        title = Res.string.navigation_rail,
        routeName = "NavigationRail"
    ),
    CustomDrawPage(
        title = Res.string.custom_draw,
        routeName = "CustomDraw"
    ),
    NumberCardPage(
        title = Res.string.number_card,
        routeName = "NumberCard"
    ),
    LoadDataPage(
        title = Res.string.load_data,
        routeName = "LoadData"
    ),
    ;

    companion object {
        fun findPageByRoute(routeName: String): LearnPages {
            // com.darcy.kmpdemo.ui.navigation.AppRoute.Home
            val route = routeName.substringAfterLast(".").split("/")[0]
            logD("findPageByRoute $routeName")
            return entries.firstOrNull() {
                it.routeName == route
            } ?: UnknownPage
        }
    }
}


sealed class LearnRoute {
    @Serializable
    data object Unknown : LearnRoute()

    @Serializable
    data object Home : LearnRoute()

    @Serializable
    data class EncryptText(val text: String) : LearnRoute()

    @Serializable
    data object EncryptFile : LearnRoute()

    @Serializable
    data object LoadResource : LearnRoute()

    @Serializable
    data object LoadMokoResource : LearnRoute()

    @Serializable
    data object KtorHttp : LearnRoute()

    @Serializable
    data object KtorWebsocket : LearnRoute()

    @Serializable
    data object DownloadImage : LearnRoute()

    @Serializable
    data object UploadImage : LearnRoute()

    @Serializable
    data object KtorWebSocketSTMOP : LearnRoute()

    @Serializable
    data object NavigationRail : LearnRoute()

    @Serializable
    data object CustomDraw : LearnRoute()

    @Serializable
    data object NumberCard : LearnRoute()

    @Serializable
    data object LoadData : LearnRoute()
}

