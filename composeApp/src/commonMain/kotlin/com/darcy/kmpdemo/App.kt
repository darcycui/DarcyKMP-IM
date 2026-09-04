package com.darcy.kmpdemo

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.darcy.kmpdemo.log.DarcyLogger
import com.darcy.kmpdemo.ui.theme.AppTheme
//import com.darcy.kmpdemo.network.ssl.SslSettings
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.darcy.kmpdemo.platform.SSLPlatform
import com.darcy.kmpdemo.storage.database.getDarcyIMDatabase
import com.darcy.kmpdemo.ui.screen.phone.navigation.AppNavigationNavHost
import com.darcy.kmpdemo.ui.screen.phone.navigation.PhoneRoute
import darcykmp_im.composeapp.generated.resources.Res

// resources中内置证书路径
const val KEYSTORE_PATH_IP = "files/ssl/test2IPSelf241.p12"
const val KEYSTORE_PATH_SERVER = "files/ssl/test2ServerSelf.p12"
@Composable
@Preview
fun App() {
    // init actions
    LaunchedEffect(Unit) {
        DarcyLogger.initLogger()

        val bytesIP = Res.readBytes(KEYSTORE_PATH_IP)
        val bytesServer = Res.readBytes(KEYSTORE_PATH_SERVER)
        SSLPlatform.sslCertsConfig(listOf(bytesIP, bytesServer))

        getDarcyIMDatabase() // 预初始化数据库，提前预热 Web Worker (WASM + OPFS)
    }
    val navController = rememberNavController()
    val startDestination = PhoneRoute.Login
    AppTheme {
        AppNavigationNavHost(navController, startDestination, Modifier.fillMaxSize())
    }

}